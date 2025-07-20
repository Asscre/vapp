package com.lody.virtual.hooks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.lody.virtual.HookWrapper;
import com.lody.virtual.VirtualCore;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 系统API Hook类
 * 实现关键系统API的Hook功能
 */
public class SystemApiHooks {
    
    private static final String TAG = "SystemApiHooks";
    private static SystemApiHooks sInstance;
    
    private boolean mIsInitialized = false;
    private HookWrapper mHookWrapper;
    
    private SystemApiHooks() {
        // 私有构造函数，实现单例模式
    }
    
    public static SystemApiHooks getInstance() {
        if (sInstance == null) {
            synchronized (SystemApiHooks.class) {
                if (sInstance == null) {
                    sInstance = new SystemApiHooks();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化系统API Hook
     * @param hookWrapper Hook包装器实例
     * @return 初始化是否成功
     */
    public boolean initialize(HookWrapper hookWrapper) {
        if (mIsInitialized) {
            Log.w(TAG, "SystemApiHooks already initialized");
            return true;
        }
        
        if (hookWrapper == null) {
            Log.e(TAG, "HookWrapper instance is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing SystemApiHooks...");
            
            mHookWrapper = hookWrapper;
            
            // 注册所有Hook类
            registerAllHooks();
            
            mIsInitialized = true;
            Log.d(TAG, "SystemApiHooks initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SystemApiHooks", e);
            return false;
        }
    }
    
    /**
     * 注册所有Hook类
     */
    private void registerAllHooks() {
        try {
            // 注册文件系统Hook
            registerFileSystemHooks();
            
            // 注册包管理Hook
            registerPackageManagerHooks();
            
            // 注册进程Hook
            registerProcessHooks();
            
            // 注册网络Hook
            registerNetworkHooks();
            
            // 注册设备信息Hook
            registerDeviceInfoHooks();
            
            Log.d(TAG, "All system API hooks registered");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to register all hooks", e);
        }
    }
    
    /**
     * 注册文件系统Hook
     */
    private void registerFileSystemHooks() {
        try {
            Log.d(TAG, "Registering file system hooks...");
            
            // 注册FileSystemHooks类
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(FileSystemHooks.class);
            if (result.success) {
                Log.d(TAG, "File system hooks registered: " + result.count + " methods");
            } else {
                Log.e(TAG, "Failed to register file system hooks: " + result.message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception registering file system hooks", e);
        }
    }
    
    /**
     * 注册包管理Hook
     */
    private void registerPackageManagerHooks() {
        try {
            Log.d(TAG, "Registering package manager hooks...");
            
            // 注册PackageManagerHooks类
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(PackageManagerHooks.class);
            if (result.success) {
                Log.d(TAG, "Package manager hooks registered: " + result.count + " methods");
            } else {
                Log.e(TAG, "Failed to register package manager hooks: " + result.message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception registering package manager hooks", e);
        }
    }
    
    /**
     * 注册进程Hook
     */
    private void registerProcessHooks() {
        try {
            Log.d(TAG, "Registering process hooks...");
            
            // 注册ProcessHooks类
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(ProcessHooks.class);
            if (result.success) {
                Log.d(TAG, "Process hooks registered: " + result.count + " methods");
            } else {
                Log.e(TAG, "Failed to register process hooks: " + result.message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception registering process hooks", e);
        }
    }
    
    /**
     * 注册网络Hook
     */
    private void registerNetworkHooks() {
        try {
            Log.d(TAG, "Registering network hooks...");
            
            // 注册NetworkHooks类
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(NetworkHooks.class);
            if (result.success) {
                Log.d(TAG, "Network hooks registered: " + result.count + " methods");
            } else {
                Log.e(TAG, "Failed to register network hooks: " + result.message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception registering network hooks", e);
        }
    }
    
    /**
     * 注册设备信息Hook
     */
    private void registerDeviceInfoHooks() {
        try {
            Log.d(TAG, "Registering device info hooks...");
            
            // 注册DeviceInfoHooks类
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(DeviceInfoHooks.class);
            if (result.success) {
                Log.d(TAG, "Device info hooks registered: " + result.count + " methods");
            } else {
                Log.e(TAG, "Failed to register device info hooks: " + result.message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception registering device info hooks", e);
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up SystemApiHooks...");
            
            // 取消注册所有Hook类
            unregisterAllHooks();
            
            mIsInitialized = false;
            Log.d(TAG, "SystemApiHooks cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup SystemApiHooks", e);
        }
    }
    
    /**
     * 取消注册所有Hook类
     */
    private void unregisterAllHooks() {
        try {
            // 取消注册文件系统Hook
            mHookWrapper.unregisterHookClass(FileSystemHooks.class);
            
            // 取消注册包管理Hook
            mHookWrapper.unregisterHookClass(PackageManagerHooks.class);
            
            // 取消注册进程Hook
            mHookWrapper.unregisterHookClass(ProcessHooks.class);
            
            // 取消注册网络Hook
            mHookWrapper.unregisterHookClass(NetworkHooks.class);
            
            // 取消注册设备信息Hook
            mHookWrapper.unregisterHookClass(DeviceInfoHooks.class);
            
            Log.d(TAG, "All system API hooks unregistered");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister all hooks", e);
        }
    }
    
    /**
     * 启用特定Hook
     * @param hookClass Hook类
     * @return 是否成功
     */
    public boolean enableHook(Class<?> hookClass) {
        if (!mIsInitialized) {
            Log.e(TAG, "SystemApiHooks not initialized");
            return false;
        }
        
        try {
            HookWrapper.RegisterResult result = mHookWrapper.registerHookClass(hookClass);
            return result.success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable hook", e);
            return false;
        }
    }
    
    /**
     * 禁用特定Hook
     * @param hookClass Hook类
     * @return 是否成功
     */
    public boolean disableHook(Class<?> hookClass) {
        if (!mIsInitialized) {
            Log.e(TAG, "SystemApiHooks not initialized");
            return false;
        }
        
        try {
            HookWrapper.UnregisterResult result = mHookWrapper.unregisterHookClass(hookClass);
            return result.success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable hook", e);
            return false;
        }
    }
    
    /**
     * 获取Hook状态信息
     */
    public String getHookStatus() {
        if (!mIsInitialized) {
            return "SystemApiHooks not initialized";
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SystemApiHooks Status:\n");
            
            // 获取所有Hook信息
            java.util.concurrent.ConcurrentHashMap<String, com.lody.virtual.HookWrapper.HookInfo> allHooks = mHookWrapper.getAllHookInfo();
            sb.append("Total hooks: ").append(allHooks.size()).append("\n");
            
            for (java.util.Map.Entry<String, com.lody.virtual.HookWrapper.HookInfo> entry : allHooks.entrySet()) {
                com.lody.virtual.HookWrapper.HookInfo hookInfo = entry.getValue();
                sb.append("- ").append(hookInfo.targetClass).append(".")
                  .append(hookInfo.targetMethod).append(" (priority: ")
                  .append(hookInfo.priority).append(")\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get hook status", e);
            return "Error getting hook status: " + e.getMessage();
        }
    }
} 