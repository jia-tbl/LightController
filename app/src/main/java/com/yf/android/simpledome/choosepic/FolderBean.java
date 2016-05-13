package com.yf.android.simpledome.choosepic;

public class FolderBean {
    private String dir;
    private String firstImagPath;
    private String fileName;
    private int fileCount;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndex = dir.lastIndexOf("/");
        this.fileName = dir.substring(lastIndex);
    }

    public String getFirstImagPath() {
        return firstImagPath;
    }

    public void setFirstImagPath(String firstImagPath) {
        this.firstImagPath = firstImagPath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}
