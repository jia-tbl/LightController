package com.yf.android.simpledome.choosepic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {
    //缓存
    private LruCache<String, Bitmap> mLruCache;

    // 线程池
    private ExecutorService mThreadPool;
    private final static int DEAFULT_COUNT = 1;
    private Semaphore mSemaphoreThreadPool;

    //线程队列
    private LinkedList<Runnable> mTask;

    //队列的调度策略
    private Type mType = Type.FIFO;

    public enum Type {
        FIFO, FILO
    }

    //轮询线程
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    // 默认0个
    private Semaphore mSemaphoremPoolThreadHandler = new Semaphore(0);

    private Handler mUIHandler;

    private static ImageLoader instance;

    private ImageLoader(int threadCount, Type type) {
        initImageLoader(threadCount, type);
    }

    private void initImageLoader(int threadCount, Type type) {
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //TODO:线程池取出线程执行
                        if (msg.what == 0x100) {
                            try {
                                mSemaphoreThreadPool.acquire();
                                mThreadPool.execute(getTask());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                //释放信号量
                mSemaphoremPoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        //应用最大可用内存
        int MaxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = MaxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {//每个Bitmap所占
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTask = new LinkedList<>();
        mType = type;

        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    //从任务队列取出
    public Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTask.removeFirst();
        } else if (mType == Type.FILO) {
            return mTask.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstance(int count, Type type) {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader(count, type);
                }
            }
        }
        return instance;
    }

    /**
     * @param path
     * @param imageView
     */
    public void loadeImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //TODO:获取bm显示
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    if (holder.imageView.getTag().toString().equals(holder.path)) {
                        holder.imageView.setImageBitmap(holder.bitmap);
                    }
                }
            };
        }
        Bitmap bm = getBitmapFromLruCache(path);
        if (bm != null) {
            refrushBitmap(path, bm, imageView);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //TODO:加载图片、压缩图片
                    ImageSize imageSize = getImageSize(imageView);
                    Bitmap bm = decodeSampledBitmap(path, imageSize.width, imageSize.height);
                    addBitmapToLruCache(path, bm);
                    refrushBitmap(path, bm, imageView);
                    mSemaphoreThreadPool.release();
                }
            });
        }
    }

    private void refrushBitmap(String path, Bitmap bm, ImageView imageView) {
        Message message = Message.obtain();
        ImageBeanHolder hodler = new ImageBeanHolder();
        hodler.bitmap = bm;
        hodler.path = path;
        hodler.imageView = imageView;
        message.obj = hodler;
        mUIHandler.sendMessage(message);
    }


    /**
     * 添加到缓存
     *
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 根据需要的宽高压缩图片
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = getSampleSize(options, width, height);

        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    /**
     * 根据需要的宽高获取inSampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int getSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight) {
            int widthRido = Math.round(width * 1.0f / reqWidth);
            int heightRido = Math.round(height * 1.0f / reqHeight);
            inSampleSize = Math.min(widthRido, heightRido);//压缩策略
        }
        return inSampleSize;
    }

    private ImageSize getImageSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics dm = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getWidth();
        if (width <= 0) {
            width = lp.width;
        }
        if (width <= 0) {
            width = getImageViewMaxValues(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = dm.widthPixels;
        }

        int height = imageView.getHeight();
        if (height <= 0) {
            height = lp.height;
        }
        if (height <= 0) {
            height = getImageViewMaxValues(imageView, "mMaxHeight");
        }
        if (height <= 0) {
            height = dm.heightPixels;
        }

        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 通过反射获取最大值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    private int getImageViewMaxValues(Object obj, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int feildValue = field.getInt(obj);
            if (feildValue > 0 && feildValue < Integer.MAX_VALUE) {
                value = feildValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 添加
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTask.add(runnable);
        try {
            if (mPoolThreadHandler == null)
                mSemaphoremPoolThreadHandler.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x100);
    }

    private Bitmap getBitmapFromLruCache(String path) {
        return mLruCache.get(path);
    }

    private class ImageBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize {
        int width;
        int height;
    }
}
