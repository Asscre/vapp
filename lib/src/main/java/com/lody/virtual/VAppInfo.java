package com.lody.virtual;

/**
 * 虚拟应用信息类
 * 存储虚拟应用的详细信息
 */
public class VAppInfo {
    
    /**
     * 包名
     */
    public String packageName;
    
    /**
     * 应用名称
     */
    public String appName;
    
    /**
     * 版本名称
     */
    public String versionName;
    
    /**
     * 版本号
     */
    public int versionCode;
    
    /**
     * 原始APK路径
     */
    public String apkPath;
    
    /**
     * 虚拟APK路径
     */
    public String virtualApkPath;
    
    /**
     * 虚拟数据目录
     */
    public String dataDir;
    
    /**
     * 虚拟库目录
     */
    public String libDir;
    
    /**
     * 虚拟缓存目录
     */
    public String cacheDir;
    
    /**
     * 安装时间
     */
    public long installTime;
    
    /**
     * 是否启用
     */
    public boolean enabled = true;
    
    /**
     * 是否启用（别名）
     */
    public boolean isEnabled = true;
    
    /**
     * 应用图标路径
     */
    public String iconPath;
    
    /**
     * 应用大小（字节）
     */
    public long appSize;
    
    /**
     * 数据大小（字节）
     */
    public long dataSize;
    
    /**
     * 最后启动时间
     */
    public long lastLaunchTime;
    
    /**
     * 启动次数
     */
    public int launchCount;
    
    /**
     * 权限列表
     */
    public String[] permissions;
    
    /**
     * 主Activity类名
     */
    public String mainActivity;
    
    /**
     * 目标SDK版本
     */
    public int targetSdkVersion;
    
    /**
     * 最小SDK版本
     */
    public int minSdkVersion;
    
    /**
     * 是否系统应用
     */
    public boolean isSystemApp;
    
    /**
     * 是否已加密
     */
    public boolean isEncrypted;
    
    /**
     * 加密密钥
     */
    public String encryptionKey;
    
    /**
     * 网络隔离策略
     */
    public String networkPolicy;
    
    /**
     * 数据隔离策略
     */
    public String dataIsolationPolicy;
    
    /**
     * 权限策略
     */
    public String permissionPolicy;
    
    /**
     * 自定义配置
     */
    public String customConfig;
    
    public VAppInfo() {
        // 默认构造函数
    }
    
    /**
     * 复制构造函数
     */
    public VAppInfo(VAppInfo other) {
        this.packageName = other.packageName;
        this.appName = other.appName;
        this.versionName = other.versionName;
        this.versionCode = other.versionCode;
        this.apkPath = other.apkPath;
        this.virtualApkPath = other.virtualApkPath;
        this.dataDir = other.dataDir;
        this.installTime = other.installTime;
        this.enabled = other.enabled;
        this.iconPath = other.iconPath;
        this.appSize = other.appSize;
        this.dataSize = other.dataSize;
        this.lastLaunchTime = other.lastLaunchTime;
        this.launchCount = other.launchCount;
        this.permissions = other.permissions != null ? other.permissions.clone() : null;
        this.mainActivity = other.mainActivity;
        this.targetSdkVersion = other.targetSdkVersion;
        this.minSdkVersion = other.minSdkVersion;
        this.isSystemApp = other.isSystemApp;
        this.isEncrypted = other.isEncrypted;
        this.encryptionKey = other.encryptionKey;
        this.networkPolicy = other.networkPolicy;
        this.dataIsolationPolicy = other.dataIsolationPolicy;
        this.permissionPolicy = other.permissionPolicy;
        this.customConfig = other.customConfig;
    }
    
    /**
     * 获取应用标识符
     */
    public String getAppId() {
        return packageName + "_" + versionCode;
    }
    
    /**
     * 检查应用是否可启动
     */
    public boolean canLaunch() {
        return enabled && virtualApkPath != null && dataDir != null;
    }
    
    /**
     * 获取应用大小（格式化）
     */
    public String getFormattedAppSize() {
        return formatFileSize(appSize);
    }
    
    /**
     * 获取数据大小（格式化）
     */
    public String getFormattedDataSize() {
        return formatFileSize(dataSize);
    }
    
    /**
     * 获取总大小（格式化）
     */
    public String getFormattedTotalSize() {
        return formatFileSize(appSize + dataSize);
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) {
            return false;
        }
        
        for (String perm : permissions) {
            if (permission.equals(perm)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查权限（返回PackageManager权限常量）
     */
    public int checkPermission(String permission) {
        if (hasPermission(permission)) {
            return android.content.pm.PackageManager.PERMISSION_GRANTED;
        } else {
            return android.content.pm.PackageManager.PERMISSION_DENIED;
        }
    }
    
    /**
     * 增加启动次数
     */
    public void incrementLaunchCount() {
        launchCount++;
        lastLaunchTime = System.currentTimeMillis();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VAppInfo vAppInfo = (VAppInfo) obj;
        return packageName != null ? packageName.equals(vAppInfo.packageName) : vAppInfo.packageName == null;
    }
    
    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "VAppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", enabled=" + enabled +
                ", launchCount=" + launchCount +
                '}';
    }
} 