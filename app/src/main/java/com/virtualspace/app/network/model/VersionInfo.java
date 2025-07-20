package com.virtualspace.app.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * 版本信息模型
 */
public class VersionInfo {
    @SerializedName("versionCode")
    private int versionCode;
    
    @SerializedName("versionName")
    private String versionName;
    
    @SerializedName("downloadUrl")
    private String downloadUrl;
    
    @SerializedName("releaseNotes")
    private String releaseNotes;
    
    @SerializedName("forceUpdate")
    private boolean forceUpdate;
    
    @SerializedName("fileSize")
    private long fileSize;
    
    @SerializedName("md5")
    private String md5;
    
    public VersionInfo() {}
    
    public VersionInfo(int versionCode, String versionName, String downloadUrl, 
                      String releaseNotes, boolean forceUpdate, long fileSize, String md5) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.downloadUrl = downloadUrl;
        this.releaseNotes = releaseNotes;
        this.forceUpdate = forceUpdate;
        this.fileSize = fileSize;
        this.md5 = md5;
    }
    
    public int getVersionCode() {
        return versionCode;
    }
    
    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
    
    public String getVersionName() {
        return versionName;
    }
    
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getReleaseNotes() {
        return releaseNotes;
    }
    
    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
    
    public boolean isForceUpdate() {
        return forceUpdate;
    }
    
    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    /**
     * 检查是否有可用更新
     */
    public boolean isUpdateAvailable() {
        return versionCode > 0 && downloadUrl != null && !downloadUrl.isEmpty();
    }
    
    /**
     * 获取文件大小格式化字符串
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
} 