package com.lody.virtual;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 虚拟环境管理器
 * 负责管理虚拟化的文件系统环境
 */
public class VEnvironment {
    
    private static final String TAG = "VEnvironment";
    
    private Context mContext;
    private String mBaseDir;
    private String mVirtualDir;
    private String mDataDir;
    private String mCacheDir;
    private String mExternalDir;
    private String mDatabaseDir;
    private String mPrefsDir;
    private String mApkDir;
    private String mLogDir;
    private String mConfigDir;
    
    public VEnvironment(Context context) {
        mContext = context.getApplicationContext();
    }
    
    /**
     * 初始化虚拟环境
     */
    public void initialize() {
        try {
            Log.d(TAG, "Initializing virtual environment...");
            
            // 设置基础目录
            setupBaseDirectories();
            
            // 创建必要的目录
            createDirectories();
            
            Log.d(TAG, "Virtual environment initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize virtual environment", e);
            throw new RuntimeException("Failed to initialize virtual environment", e);
        }
    }
    
    /**
     * 设置基础目录
     */
    private void setupBaseDirectories() {
        // 基础目录：/data/data/com.virtualspace.app/virtual
        mBaseDir = mContext.getApplicationInfo().dataDir + "/virtual";
        
        // 虚拟空间目录
        mVirtualDir = mBaseDir + "/space";
        
        // 数据目录
        mDataDir = mBaseDir + "/data";
        
        // 缓存目录
        mCacheDir = mBaseDir + "/cache";
        
        // 外部存储目录
        mExternalDir = mBaseDir + "/external";
        
        // 数据库目录
        mDatabaseDir = mBaseDir + "/databases";
        
        // SharedPreferences目录
        mPrefsDir = mBaseDir + "/shared_prefs";
        
        // APK目录
        mApkDir = mBaseDir + "/apks";
        
        // 日志目录
        mLogDir = mBaseDir + "/logs";
        
        // 配置目录
        mConfigDir = mBaseDir + "/config";
        
        Log.d(TAG, "Base directories setup completed");
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        createDirectory(mBaseDir);
        createDirectory(mVirtualDir);
        createDirectory(mDataDir);
        createDirectory(mCacheDir);
        createDirectory(mExternalDir);
        createDirectory(mDatabaseDir);
        createDirectory(mPrefsDir);
        createDirectory(mApkDir);
        createDirectory(mLogDir);
        createDirectory(mConfigDir);
        
        Log.d(TAG, "All directories created successfully");
    }
    
    /**
     * 创建目录
     * @param path 目录路径
     * @return 是否创建成功
     */
    public boolean createDirectory(String path) {
        if (path == null) {
            return false;
        }
        
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (success) {
                    Log.d(TAG, "Created directory: " + path);
                } else {
                    Log.e(TAG, "Failed to create directory: " + path);
                }
                return success;
            } else {
                Log.d(TAG, "Directory already exists: " + path);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception creating directory: " + path, e);
            return false;
        }
    }
    
    /**
     * 删除目录
     * @param path 目录路径
     * @return 是否删除成功
     */
    public boolean deleteDirectory(String path) {
        if (path == null) {
            return false;
        }
        
        try {
            File dir = new File(path);
            if (dir.exists()) {
                boolean success = deleteRecursively(dir);
                if (success) {
                    Log.d(TAG, "Deleted directory: " + path);
                } else {
                    Log.e(TAG, "Failed to delete directory: " + path);
                }
                return success;
            } else {
                Log.d(TAG, "Directory does not exist: " + path);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception deleting directory: " + path, e);
            return false;
        }
    }
    
    /**
     * 递归删除目录
     * @param file 文件或目录
     * @return 是否删除成功
     */
    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }
    
    /**
     * 复制文件
     * @param sourcePath 源文件路径
     * @param destPath 目标文件路径
     * @return 是否复制成功
     */
    public boolean copyFile(String sourcePath, String destPath) {
        if (sourcePath == null || destPath == null) {
            return false;
        }
        
        try {
            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);
            
            if (!sourceFile.exists()) {
                Log.e(TAG, "Source file does not exist: " + sourcePath);
                return false;
            }
            
            // 确保目标目录存在
            File destDir = destFile.getParentFile();
            if (destDir != null && !destDir.exists()) {
                if (!destDir.mkdirs()) {
                    Log.e(TAG, "Failed to create destination directory: " + destDir.getAbsolutePath());
                    return false;
                }
            }
            
            // 使用NIO复制文件
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destFile);
                 FileChannel sourceChannel = fis.getChannel();
                 FileChannel destChannel = fos.getChannel()) {
                
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            }
            
            Log.d(TAG, "File copied successfully: " + sourcePath + " -> " + destPath);
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file: " + sourcePath + " -> " + destPath, e);
            return false;
        }
    }
    
    /**
     * 获取虚拟数据路径
     * @param packageName 包名
     * @return 虚拟数据路径
     */
    public String getVirtualDataPath(String packageName) {
        return mDataDir + "/" + packageName;
    }
    
    /**
     * 获取虚拟缓存路径
     * @param packageName 包名
     * @return 虚拟缓存路径
     */
    public String getVirtualCachePath(String packageName) {
        return mCacheDir + "/" + packageName;
    }
    
    /**
     * 获取虚拟外部存储路径
     * @param packageName 包名
     * @return 虚拟外部存储路径
     */
    public String getVirtualExternalPath(String packageName) {
        return mExternalDir + "/" + packageName;
    }
    
    /**
     * 获取虚拟数据库路径
     * @param packageName 包名
     * @return 虚拟数据库路径
     */
    public String getVirtualDatabasePath(String packageName) {
        return mDatabaseDir + "/" + packageName;
    }
    
    /**
     * 获取虚拟SharedPreferences路径
     * @param packageName 包名
     * @return 虚拟SharedPreferences路径
     */
    public String getVirtualPrefsPath(String packageName) {
        return mPrefsDir + "/" + packageName;
    }
    
    /**
     * 获取虚拟APK路径
     * @param packageName 包名
     * @return 虚拟APK路径
     */
    public String getVirtualApkPath(String packageName) {
        return mApkDir + "/" + packageName + ".apk";
    }
    
    /**
     * 获取虚拟日志路径
     * @param packageName 包名
     * @return 虚拟日志路径
     */
    public String getVirtualLogPath(String packageName) {
        return mLogDir + "/" + packageName + ".log";
    }
    
    /**
     * 获取虚拟配置路径
     * @param packageName 包名
     * @return 虚拟配置路径
     */
    public String getVirtualConfigPath(String packageName) {
        return mConfigDir + "/" + packageName + ".json";
    }
    
    /**
     * 获取基础目录
     */
    public String getBaseDir() {
        return mBaseDir;
    }
    
    /**
     * 获取虚拟空间目录
     */
    public String getVirtualDir() {
        return mVirtualDir;
    }
    
    /**
     * 获取数据目录
     */
    public String getDataDir() {
        return mDataDir;
    }
    
    /**
     * 获取缓存目录
     */
    public String getCacheDir() {
        return mCacheDir;
    }
    
    /**
     * 获取外部存储目录
     */
    public String getExternalDir() {
        return mExternalDir;
    }
    
    /**
     * 获取数据库目录
     */
    public String getDatabaseDir() {
        return mDatabaseDir;
    }
    
    /**
     * 获取SharedPreferences目录
     */
    public String getPrefsDir() {
        return mPrefsDir;
    }
    
    /**
     * 获取APK目录
     */
    public String getApkDir() {
        return mApkDir;
    }
    
    /**
     * 获取日志目录
     */
    public String getLogDir() {
        return mLogDir;
    }
    
    /**
     * 获取配置目录
     */
    public String getConfigDir() {
        return mConfigDir;
    }
    
    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    public boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }
    
    /**
     * 获取文件大小
     * @param path 文件路径
     * @return 文件大小（字节）
     */
    public long getFileSize(String path) {
        if (path == null) {
            return 0;
        }
        
        File file = new File(path);
        return file.exists() ? file.length() : 0;
    }
    
    /**
     * 获取目录大小
     * @param path 目录路径
     * @return 目录大小（字节）
     */
    public long getDirectorySize(String path) {
        if (path == null) {
            return 0;
        }
        
        return calculateDirectorySize(new File(path));
    }
    
    /**
     * 计算目录大小
     * @param dir 目录
     * @return 目录大小（字节）
     */
    private long calculateDirectorySize(File dir) {
        long size = 0;
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    }
                }
            }
        }
        
        return size;
    }
    
    /**
     * 清理缓存
     * @return 清理的字节数
     */
    public long cleanupCache() {
        long cleanedSize = getDirectorySize(mCacheDir);
        if (deleteDirectory(mCacheDir)) {
            createDirectory(mCacheDir);
            Log.d(TAG, "Cache cleaned up, freed " + cleanedSize + " bytes");
        }
        return cleanedSize;
    }
    
    /**
     * 获取可用空间
     * @return 可用空间（字节）
     */
    public long getAvailableSpace() {
        try {
            File baseDir = new File(mBaseDir);
            return baseDir.getUsableSpace();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get available space", e);
            return 0;
        }
    }
    
    /**
     * 获取总空间
     * @return 总空间（字节）
     */
    public long getTotalSpace() {
        try {
            File baseDir = new File(mBaseDir);
            return baseDir.getTotalSpace();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get total space", e);
            return 0;
        }
    }
    
    /**
     * 获取虚拟数据目录（静态方法）
     * @param packageName 包名
     * @return 虚拟数据目录路径
     */
    public static String getVirtualDataDir(String packageName) {
        return "/data/data/com.virtualspace.app/virtual/data/" + packageName;
    }
    
    /**
     * 获取虚拟库目录（静态方法）
     * @param packageName 包名
     * @return 虚拟库目录路径
     */
    public static String getVirtualLibDir(String packageName) {
        return "/data/data/com.virtualspace.app/virtual/data/" + packageName + "/lib";
    }
    
    /**
     * 获取虚拟缓存目录（静态方法）
     * @param packageName 包名
     * @return 虚拟缓存目录路径
     */
    public static String getVirtualCacheDir(String packageName) {
        return "/data/data/com.virtualspace.app/virtual/cache/" + packageName;
    }
    
    /**
     * 获取重定向路径（静态方法）
     * @param originalPath 原始路径
     * @return 重定向路径
     */
    public static String getRedirectedPath(String originalPath) {
        // 这里应该实现路径重定向逻辑
        // 暂时返回原始路径
        return originalPath;
    }
    
    /**
     * 获取虚拟路径（静态方法）
     * @param originalPath 原始路径
     * @return 虚拟路径
     */
    public static String getVirtualPath(String originalPath) {
        // 这里应该实现路径虚拟化逻辑
        // 暂时返回原始路径
        return originalPath;
    }
    
    /**
     * 获取实例（静态方法）
     * @param context 上下文
     * @return VEnvironment实例
     */
    public static VEnvironment getInstance(Context context) {
        return new VEnvironment(context);
    }
} 