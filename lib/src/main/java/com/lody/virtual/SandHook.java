package com.lody.virtual;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SandHook核心类
 * 提供Java层Hook功能，支持方法替换和备份管理
 */
public class SandHook {
    
    private static final String TAG = "SandHook";
    private static SandHook sInstance;
    
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, HookInfo> mHookRegistry = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> mBackupRegistry = new ConcurrentHashMap<>();
    
    // Native方法声明
    private native boolean nativeInitialize();
    private native void nativeCleanup();
    private native boolean nativeHookMethod(Method target, Method hook, Method backup);
    private native boolean nativeUnhookMethod(Method target);
    private native Object nativeCallOriginMethod(Method backup, Object receiver, Object... args);
    
    static {
        try {
            System.loadLibrary("virtualspace");
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
    
    private SandHook() {
        // 私有构造函数，实现单例模式
    }
    
    public static SandHook getInstance() {
        if (sInstance == null) {
            synchronized (SandHook.class) {
                if (sInstance == null) {
                    sInstance = new SandHook();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化SandHook
     * @return 初始化是否成功
     */
    public boolean initialize() {
        if (mIsInitialized.get()) {
            Log.w(TAG, "SandHook already initialized");
            return true;
        }
        
        try {
            Log.d(TAG, "Initializing SandHook...");
            
            // 初始化Native层
            if (!nativeInitialize()) {
                Log.e(TAG, "Failed to initialize native layer");
                return false;
            }
            
            // 清理注册表
            mHookRegistry.clear();
            mBackupRegistry.clear();
            
            mIsInitialized.set(true);
            Log.d(TAG, "SandHook initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SandHook", e);
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
     * Hook方法
     * @param targetClass 目标类
     * @param targetMethodName 目标方法名
     * @param targetParameterTypes 目标方法参数类型
     * @param hookClass Hook类
     * @param hookMethodName Hook方法名
     * @return Hook结果
     */
    public HookResult hookMethod(Class<?> targetClass, String targetMethodName, 
                                Class<?>[] targetParameterTypes, Class<?> hookClass, 
                                String hookMethodName) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "SandHook not initialized");
            return new HookResult(false, "SandHook not initialized", null);
        }
        
        try {
            Log.d(TAG, "Hooking method: " + targetClass.getName() + "." + targetMethodName);
            
            // 获取目标方法
            Method targetMethod = targetClass.getDeclaredMethod(targetMethodName, targetParameterTypes);
            if (targetMethod == null) {
                return new HookResult(false, "Target method not found", null);
            }
            
            // 获取Hook方法
            Method hookMethod = hookClass.getDeclaredMethod(hookMethodName, targetMethod.getParameterTypes());
            if (hookMethod == null) {
                return new HookResult(false, "Hook method not found", null);
            }
            
            // 创建备份方法
            Method backupMethod = createBackupMethod(targetMethod);
            if (backupMethod == null) {
                return new HookResult(false, "Failed to create backup method", null);
            }
            
            // 执行Hook
            boolean success = nativeHookMethod(targetMethod, hookMethod, backupMethod);
            if (!success) {
                return new HookResult(false, "Native hook failed", null);
            }
            
            // 注册Hook信息
            String hookKey = getHookKey(targetMethod);
            HookInfo hookInfo = new HookInfo(targetMethod, hookMethod, backupMethod);
            mHookRegistry.put(hookKey, hookInfo);
            
            // 注册备份方法
            String backupKey = getBackupKey(backupMethod);
            mBackupRegistry.put(backupKey, backupMethod);
            
            Log.d(TAG, "Method hooked successfully: " + hookKey);
            return new HookResult(true, "Hook successful", backupMethod);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to hook method", e);
            return new HookResult(false, "Exception: " + e.getMessage(), null);
        }
    }
    
    /**
     * Hook方法（重载版本）
     * @param targetMethod 目标方法
     * @param hookMethod Hook方法
     * @return Hook结果
     */
    public HookResult hookMethod(Method targetMethod, Method hookMethod) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "SandHook not initialized");
            return new HookResult(false, "SandHook not initialized", null);
        }
        
        try {
            Log.d(TAG, "Hooking method: " + targetMethod.toString());
            
            // 创建备份方法
            Method backupMethod = createBackupMethod(targetMethod);
            if (backupMethod == null) {
                return new HookResult(false, "Failed to create backup method", null);
            }
            
            // 执行Hook
            boolean success = nativeHookMethod(targetMethod, hookMethod, backupMethod);
            if (!success) {
                return new HookResult(false, "Native hook failed", null);
            }
            
            // 注册Hook信息
            String hookKey = getHookKey(targetMethod);
            HookInfo hookInfo = new HookInfo(targetMethod, hookMethod, backupMethod);
            mHookRegistry.put(hookKey, hookInfo);
            
            // 注册备份方法
            String backupKey = getBackupKey(backupMethod);
            mBackupRegistry.put(backupKey, backupMethod);
            
            Log.d(TAG, "Method hooked successfully: " + hookKey);
            return new HookResult(true, "Hook successful", backupMethod);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to hook method", e);
            return new HookResult(false, "Exception: " + e.getMessage(), null);
        }
    }
    
    /**
     * 取消Hook
     * @param targetMethod 目标方法
     * @return 是否成功
     */
    public boolean unhookMethod(Method targetMethod) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "SandHook not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Unhooking method: " + targetMethod.toString());
            
            String hookKey = getHookKey(targetMethod);
            HookInfo hookInfo = mHookRegistry.get(hookKey);
            if (hookInfo == null) {
                Log.w(TAG, "Hook not found: " + hookKey);
                return false;
            }
            
            // 执行Native层取消Hook
            boolean success = nativeUnhookMethod(targetMethod);
            if (!success) {
                Log.e(TAG, "Native unhook failed");
                return false;
            }
            
            // 清理注册信息
            mHookRegistry.remove(hookKey);
            String backupKey = getBackupKey(hookInfo.backupMethod);
            mBackupRegistry.remove(backupKey);
            
            Log.d(TAG, "Method unhooked successfully: " + hookKey);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to unhook method", e);
            return false;
        }
    }
    
    /**
     * 调用原始方法
     * @param backupMethod 备份方法
     * @param receiver 接收者对象
     * @param args 参数
     * @return 调用结果
     */
    public Object callOriginMethod(Method backupMethod, Object receiver, Object... args) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "SandHook not initialized");
            return null;
        }
        
        try {
            return nativeCallOriginMethod(backupMethod, receiver, args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to call origin method", e);
            return null;
        }
    }
    
    /**
     * 检查方法是否已Hook
     * @param targetMethod 目标方法
     * @return 是否已Hook
     */
    public boolean isMethodHooked(Method targetMethod) {
        if (!mIsInitialized.get()) {
            return false;
        }
        
        String hookKey = getHookKey(targetMethod);
        return mHookRegistry.containsKey(hookKey);
    }
    
    /**
     * 获取Hook信息
     * @param targetMethod 目标方法
     * @return Hook信息
     */
    public HookInfo getHookInfo(Method targetMethod) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        String hookKey = getHookKey(targetMethod);
        return mHookRegistry.get(hookKey);
    }
    
    /**
     * 获取所有Hook信息
     */
    public ConcurrentHashMap<String, HookInfo> getAllHookInfo() {
        return new ConcurrentHashMap<>(mHookRegistry);
    }
    
    /**
     * 清理所有Hook
     */
    public void cleanupAllHooks() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up all hooks...");
            
            // 取消所有Hook
            for (HookInfo hookInfo : mHookRegistry.values()) {
                nativeUnhookMethod(hookInfo.targetMethod);
            }
            
            // 清理注册表
            mHookRegistry.clear();
            mBackupRegistry.clear();
            
            Log.d(TAG, "All hooks cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup all hooks", e);
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
            Log.d(TAG, "Cleaning up SandHook...");
            
            // 清理所有Hook
            cleanupAllHooks();
            
            // 清理Native层
            nativeCleanup();
            
            mIsInitialized.set(false);
            Log.d(TAG, "SandHook cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup SandHook", e);
        }
    }
    
    /**
     * 创建备份方法
     * @param targetMethod 目标方法
     * @return 备份方法
     */
    private Method createBackupMethod(Method targetMethod) {
        try {
            // 创建备份方法名
            String backupMethodName = "backup_" + targetMethod.getName() + "_" + System.currentTimeMillis();
            
            // 创建备份类
            Class<?> backupClass = createBackupClass(targetMethod.getDeclaringClass());
            
            // 获取备份方法
            Method backupMethod = backupClass.getDeclaredMethod(backupMethodName, targetMethod.getParameterTypes());
            backupMethod.setAccessible(true);
            
            return backupMethod;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create backup method", e);
            return null;
        }
    }
    
    /**
     * 创建备份类
     * @param originalClass 原始类
     * @return 备份类
     */
    private Class<?> createBackupClass(Class<?> originalClass) {
        try {
            // 这里应该使用字节码生成技术创建备份类
            // 暂时返回原始类作为占位符
            return originalClass;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create backup class", e);
            return null;
        }
    }
    
    /**
     * 获取Hook键值
     * @param targetMethod 目标方法
     * @return Hook键值
     */
    private String getHookKey(Method targetMethod) {
        return targetMethod.getDeclaringClass().getName() + "." + targetMethod.getName() + 
               "(" + getParameterTypesString(targetMethod.getParameterTypes()) + ")";
    }
    
    /**
     * 获取备份键值
     * @param backupMethod 备份方法
     * @return 备份键值
     */
    private String getBackupKey(Method backupMethod) {
        return backupMethod.getDeclaringClass().getName() + "." + backupMethod.getName();
    }
    
    /**
     * 获取参数类型字符串
     * @param parameterTypes 参数类型数组
     * @return 参数类型字符串
     */
    private String getParameterTypesString(Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(parameterTypes[i].getName());
        }
        return sb.toString();
    }
    
    /**
     * Hook信息类
     */
    public static class HookInfo {
        public final Method targetMethod;
        public final Method hookMethod;
        public final Method backupMethod;
        public final long hookTime;
        
        public HookInfo(Method targetMethod, Method hookMethod, Method backupMethod) {
            this.targetMethod = targetMethod;
            this.hookMethod = hookMethod;
            this.backupMethod = backupMethod;
            this.hookTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Hook结果类
     */
    public static class HookResult {
        public final boolean success;
        public final String message;
        public final Method backupMethod;
        
        public HookResult(boolean success, String message, Method backupMethod) {
            this.success = success;
            this.message = message;
            this.backupMethod = backupMethod;
        }
    }
} 