package com.lody.virtual;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IO统一器
 * 负责在Java层管理IO重定向功能
 */
public class IOUniformer {
    
    private static final String TAG = "IOUniformer";
    private static IOUniformer sInstance;
    
    private Context mContext;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, String> mPathMappings = new ConcurrentHashMap<>();
    
    // Native方法声明
    private native boolean nativeInitialize();
    private native void nativeCleanup();
    private native boolean nativeAddPathMapping(String originalPath, String virtualPath);
    private native boolean nativeRemovePathMapping(String originalPath);
    private native String nativeRedirectPath(String originalPath);
    
    static {
        try {
            System.loadLibrary("virtualspace");
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
    
    private IOUniformer() {
        // 私有构造函数，实现单例模式
    }
    
    public static IOUniformer getInstance() {
        if (sInstance == null) {
            synchronized (IOUniformer.class) {
                if (sInstance == null) {
                    sInstance = new IOUniformer();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化IO统一器
     * @param context 应用上下文
     * @return 初始化是否成功
     */
    public boolean initialize(Context context) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "IOUniformer already initialized");
            return true;
        }
        
        try {
            Log.d(TAG, "Initializing IOUniformer...");
            
            mContext = context.getApplicationContext();
            
            // 初始化Native层
            if (!nativeInitialize()) {
                Log.e(TAG, "Failed to initialize native layer");
                return false;
            }
            
            // 设置默认路径映射
            setupDefaultPathMappings();
            
            mIsInitialized.set(true);
            Log.d(TAG, "IOUniformer initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize IOUniformer", e);
            return false;
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return mIsInitialized.get();
    }
    
    /**
     * 添加路径映射
     * @param originalPath 原始路径
     * @param virtualPath 虚拟路径
     * @return 是否成功
     */
    public boolean addPathMapping(String originalPath, String virtualPath) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "IOUniformer not initialized");
            return false;
        }
        
        if (originalPath == null || virtualPath == null) {
            Log.e(TAG, "Invalid path mapping");
            return false;
        }
        
        try {
            // 规范化路径
            String normalizedOriginal = normalizePath(originalPath);
            String normalizedVirtual = normalizePath(virtualPath);
            
            // 添加到Java层缓存
            mPathMappings.put(normalizedOriginal, normalizedVirtual);
            
            // 添加到Native层
            boolean success = nativeAddPathMapping(normalizedOriginal, normalizedVirtual);
            if (success) {
                Log.d(TAG, "Added path mapping: " + normalizedOriginal + " -> " + normalizedVirtual);
            } else {
                Log.e(TAG, "Failed to add path mapping to native layer");
                mPathMappings.remove(normalizedOriginal);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception adding path mapping", e);
            return false;
        }
    }
    
    /**
     * 移除路径映射
     * @param originalPath 原始路径
     * @return 是否成功
     */
    public boolean removePathMapping(String originalPath) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "IOUniformer not initialized");
            return false;
        }
        
        if (originalPath == null) {
            Log.e(TAG, "Invalid original path");
            return false;
        }
        
        try {
            String normalizedPath = normalizePath(originalPath);
            
            // 从Java层缓存移除
            String removedVirtualPath = mPathMappings.remove(normalizedPath);
            if (removedVirtualPath == null) {
                Log.w(TAG, "Path mapping not found: " + normalizedPath);
                return false;
            }
            
            // 从Native层移除
            boolean success = nativeRemovePathMapping(normalizedPath);
            if (success) {
                Log.d(TAG, "Removed path mapping: " + normalizedPath);
            } else {
                Log.e(TAG, "Failed to remove path mapping from native layer");
                // 恢复Java层缓存
                mPathMappings.put(normalizedPath, removedVirtualPath);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception removing path mapping", e);
            return false;
        }
    }
    
    /**
     * 重定向路径
     * @param originalPath 原始路径
     * @return 重定向后的路径
     */
    public String redirectPath(String originalPath) {
        if (!mIsInitialized.get()) {
            return originalPath;
        }
        
        if (originalPath == null) {
            return null;
        }
        
        try {
            // 先检查Java层缓存
            String normalizedPath = normalizePath(originalPath);
            String cachedVirtualPath = mPathMappings.get(normalizedPath);
            if (cachedVirtualPath != null) {
                return cachedVirtualPath;
            }
            
            // 查找最长匹配的路径映射
            String bestMatch = null;
            String bestVirtualPath = null;
            int bestLength = 0;
            
            for (String original : mPathMappings.keySet()) {
                if (normalizedPath.startsWith(original) && original.length() > bestLength) {
                    bestMatch = original;
                    bestVirtualPath = mPathMappings.get(original);
                    bestLength = original.length();
                }
            }
            
            if (bestMatch != null) {
                String redirectedPath = bestVirtualPath + normalizedPath.substring(bestMatch.length());
                Log.d(TAG, "Path redirected: " + normalizedPath + " -> " + redirectedPath);
                return redirectedPath;
            }
            
            // 使用Native层重定向
            String nativeRedirectedPath = nativeRedirectPath(normalizedPath);
            if (!normalizedPath.equals(nativeRedirectedPath)) {
                Log.d(TAG, "Native path redirected: " + normalizedPath + " -> " + nativeRedirectedPath);
                return nativeRedirectedPath;
            }
            
            // 没有找到映射，返回原始路径
            return originalPath;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception redirecting path", e);
            return originalPath;
        }
    }
    
    /**
     * 启动IO重定向
     * @return 是否成功
     */
    public boolean startIORedirection() {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "IOUniformer not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Starting IO redirection...");
            
            // TODO: 启动Native层的系统调用Hook
            
            Log.d(TAG, "IO redirection started successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start IO redirection", e);
            return false;
        }
    }
    
    /**
     * 停止IO重定向
     */
    public void stopIORedirection() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Stopping IO redirection...");
            
            // TODO: 停止Native层的系统调用Hook
            
            Log.d(TAG, "IO redirection stopped");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop IO redirection", e);
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up IOUniformer...");
            
            // 停止IO重定向
            stopIORedirection();
            
            // 清理Native层
            nativeCleanup();
            
            // 清理Java层缓存
            mPathMappings.clear();
            
            mIsInitialized.set(false);
            Log.d(TAG, "IOUniformer cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup IOUniformer", e);
        }
    }
    
    /**
     * 设置默认路径映射
     */
    private void setupDefaultPathMappings() {
        try {
            Log.d(TAG, "Setting up default path mappings...");
            
            // 数据目录映射
            String realDataDir = mContext.getApplicationInfo().dataDir;
            String virtualDataDir = VEnvironment.getInstance(mContext).getVirtualDataPath("default");
            addPathMapping(realDataDir, virtualDataDir);
            
            // 缓存目录映射
            String realCacheDir = mContext.getCacheDir().getAbsolutePath();
            String virtualCacheDir = VEnvironment.getInstance(mContext).getVirtualCachePath("default");
            addPathMapping(realCacheDir, virtualCacheDir);
            
            // 外部存储目录映射
            String realExternalDir = mContext.getExternalFilesDir(null).getAbsolutePath();
            String virtualExternalDir = VEnvironment.getInstance(mContext).getVirtualExternalPath("default");
            addPathMapping(realExternalDir, virtualExternalDir);
            
            Log.d(TAG, "Default path mappings setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup default path mappings", e);
        }
    }
    
    /**
     * 规范化路径
     * @param path 原始路径
     * @return 规范化后的路径
     */
    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        
        try {
            // 移除末尾的斜杠
            while (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            
            // 确保以斜杠开头
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            return path;
            
        } catch (Exception e) {
            Log.e(TAG, "Exception normalizing path", e);
            return path;
        }
    }
    
    /**
     * 获取所有路径映射
     */
    public ConcurrentHashMap<String, String> getPathMappings() {
        return new ConcurrentHashMap<>(mPathMappings);
    }
    
    /**
     * 检查路径是否被映射
     * @param path 路径
     * @return 是否被映射
     */
    public boolean isPathMapped(String path) {
        if (path == null) {
            return false;
        }
        
        String normalizedPath = normalizePath(path);
        
        // 检查精确匹配
        if (mPathMappings.containsKey(normalizedPath)) {
            return true;
        }
        
        // 检查前缀匹配
        for (String original : mPathMappings.keySet()) {
            if (normalizedPath.startsWith(original)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取映射的虚拟路径
     * @param originalPath 原始路径
     * @return 虚拟路径，如果没有映射则返回null
     */
    public String getMappedVirtualPath(String originalPath) {
        if (originalPath == null) {
            return null;
        }
        
        String redirectedPath = redirectPath(originalPath);
        if (!originalPath.equals(redirectedPath)) {
            return redirectedPath;
        }
        
        return null;
    }
} 