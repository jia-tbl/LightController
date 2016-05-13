package com.yf.android.simpledome.zxing.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.Result;
import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.fragments.ScanResultFragment;
import com.yf.android.simpledome.zxing.camera.CameraManager;
import com.yf.android.simpledome.zxing.decode.DecodeThread;
import com.yf.android.simpledome.zxing.utils.BeepManager;
import com.yf.android.simpledome.zxing.utils.CaptureActivityHandler;
import com.yf.android.simpledome.zxing.utils.InactivityTimer;

import java.io.IOException;
import java.lang.reflect.Field;

public class InterFragment extends Fragment implements SurfaceHolder.Callback {
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    public SurfaceView scanPreview = null;
    public RelativeLayout scanContainer;
    public RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.aptrue_iner_view, container, false);


        scanPreview = (SurfaceView) rootView.findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) rootView.findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) rootView.findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) rootView.findViewById(R.id.capture_scan_line);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);

        inactivityTimer = new InactivityTimer(getActivity());
        beepManager = new BeepManager(getActivity());

        return rootView;
    }

    // for bind cont
    private Controller mController;
    private CustomDialog dialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                mController.setContType(msg.arg1);
                // 初始化位屏操作数据，冷暖切换相关
                if (msg.arg1 == 4) {
                    mController.setOptData("0000000000000000");
                } else if (msg.arg1 == 3) {
                    mController.setOptData("0000000000000064");
                } else if (msg.arg1 == 2) {
                    mController.setOptData("0000000000640064");
                } else if (msg.arg1 == 1) {
                    mController.setOptData("0000000000640064");
                }
                dialog.cancel();
                toResultView(mController);
            } else if (msg.what == R.id.addNC) {
                // wrong callback: CONT_TYPE not an nub
                Toast.makeText(getActivity(), "操作失败\n请确保控制器在线且没被用户添加过", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                getActivity().finish();
            } else if (msg.what == R.id.errorTip) {
                // bind 失败
                Toast.makeText(getActivity(), "控制器信息错误", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                getActivity().finish();
            }
        }
    };

    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();

        beepManager.playBeepSoundAndVibrate();
        String string = rawResult.getText();

        if (string.length() == 8 && string.matches("[0-9[a-fA-F]]{8}")) {
            mController = new Controller();
            mController.setContName("Controller");
            mController.setContCode(string.toUpperCase());
            mController.setContData("6464646464646464");

            mController.setBtnName1("Btn_A");
            mController.setBtnName2("Btn_B");
            mController.setBtnName3("Btn_C");
            mController.setBtnName4("Btn_D");

            mController.setBtnScene1(0);
            mController.setBtnScene2(0);
            mController.setBtnScene3(0);
            mController.setBtnScene4(0);

            dialog = new CustomDialog(getActivity(), "提交数据...", mHandler, 3 * 1000);
            Connection.getInstance().cmdBindCont(string);
        } else {
            getActivity().finish();
        }
    }

    private void toResultView(Controller cont) {
        ScanResultFragment fragment = new ScanResultFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ScanResultFragment.TYPE, ScanResultFragment.TYPE_BIND);
        bundle.putSerializable(ScanResultFragment.CONTROLLER_KEY, cont);
        fragment.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.scanActFrag, fragment)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getActivity().getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w("", "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            displayFrameworkBugMessageAndExit();
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().finish();
            }
        });
        builder.show();
    }

    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Rect getCropRect() {
        return mCropRect;
    }
}
