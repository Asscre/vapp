package com.lody.virtual;

import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hook包装器
 * 提供注解驱动的自动Hook功能
 */
public class HookWrapper {
    
    private static final String TAG = "HookWrapper";
    private static HookWrapper sInstance;
    
    private ConcurrentHashMap<String, HookInfo> mHookRegistry = new ConcurrentHashMap<>();
    private SandHook mSandHook;
    
    /**
     * Hook注解
     * 用于标记需要Hook的方法
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Hook {
        /**
         * 目标类名
         */
        String targetClass();
        
        /**
         * 目标方法名
         */
        String targetMethod();
        
        /**
         * 目标方法参数类型（可选）
         */
        String[] targetParameterTypes() default {};
        
        /**
         * Hook优先级
         */
        int priority() default 0;
        
        /**
         * 是否启用
         */
        boolean enabled() default true;
    }
    
    /**
     * Hook信息类
     */
    public static class HookInfo {
        public final String targetClass;
        public final String targetMethod;
        public final String[] targetParameterTypes;
        public final Method hookMethod;
        public final int priority;
        public final boolean enabled;
        public final long registerTime;
        
        public HookInfo(String targetClass, String targetMethod, String[] targetParameterTypes,
                       Method hookMethod, int priority, boolean enabled) {
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.targetParameterTypes = targetParameterTypes;
            this.hookMethod = hookMethod;
            this.priority = priority;
            this.enabled = enabled;
            this.registerTime = System.currentTimeMillis();
        }
    }
    
    private HookWrapper() {
        // 私有构造函数，实现单例模式
    }
    
    public static HookWrapper getInstance() {
        if (sInstance == null) {
            synchronized (HookWrapper.class) {
                if (sInstance == null) {
                    sInstance = new HookWrapper();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化Hook包装器
     * @param sandHook SandHook实例
     * @return 初始化是否成功
     */
    public boolean initialize(SandHook sandHook) {
        if (sandHook == null) {
            Log.e(TAG, "SandHook instance is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing HookWrapper...");
            
            mSandHook = sandHook;
            mHookRegistry.clear();
            
            Log.d(TAG, "HookWrapper initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize HookWrapper", e);
            return false;
        }
    }
    
    /**
     * 注册Hook类
     * @param hookClass Hook类
     * @return 注册结果
     */
    public RegisterResult registerHookClass(Class<?> hookClass) {
        if (mSandHook == null) {
            Log.e(TAG, "SandHook not initialized");
            return new RegisterResult(false, "SandHook not initialized", 0);
        }
        
        try {
            Log.d(TAG, "Registering hook class: " + hookClass.getName());
            
            int successCount = 0;
            int totalCount = 0;
            
            // 扫描类中的所有方法
            Method[] methods = hookClass.getDeclaredMethods();
            for (Method method : methods) {
                // 检查是否有Hook注解
                Hook hookAnnotation = method.getAnnotation(Hook.class);
                if (hookAnnotation != null && hookAnnotation.enabled()) {
                    totalCount++;
                    
                    // 注册Hook
                    RegisterResult result = registerHookMethod(method, hookAnnotation);
                    if (result.success) {
                        successCount++;
                    } else {
                        Log.w(TAG, "Failed to register hook method: " + method.getName() + 
                              ", reason: " + result.message);
                    }
                }
            }
            
            Log.d(TAG, "Hook class registered: " + successCount + "/" + totalCount + " methods");
            return new RegisterResult(true, "Registered " + successCount + "/" + totalCount + " methods", successCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to register hook class", e);
            return new RegisterResult(false, "Exception: " + e.getMessage(), 0);
        }
    }
    
    /**
     * 注册Hook方法
     * @param hookMethod Hook方法
     * @param hookAnnotation Hook注解
     * @return 注册结果
     */
    private RegisterResult registerHookMethod(Method hookMethod, Hook hookAnnotation) {
        try {
            String targetClass = hookAnnotation.targetClass();
            String targetMethod = hookAnnotation.targetMethod();
            String[] targetParameterTypes = hookAnnotation.targetParameterTypes();
            int priority = hookAnnotation.priority();
            
            // 获取目标类
            Class<?> targetClassObj = Class.forName(targetClass);
            if (targetClassObj == null) {
                return new RegisterResult(false, "Target class not found: " + targetClass, 0);
            }
            
            // 获取目标方法
            Method targetMethodObj = null;
            if (targetParameterTypes.length > 0) {
                // 使用指定的参数类型
                Class<?>[] parameterTypes = new Class<?>[targetParameterTypes.length];
                for (int i = 0; i < targetParameterTypes.length; i++) {
                    parameterTypes[i] = Class.forName(targetParameterTypes[i]);
                }
                targetMethodObj = targetClassObj.getDeclaredMethod(targetMethod, parameterTypes);
            } else {
                // 自动匹配参数类型
                Method[] methods = targetClassObj.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(targetMethod) && 
                        method.getParameterCount() == hookMethod.getParameterCount()) {
                        targetMethodObj = method;
                        break;
                    }
                }
            }
            
            if (targetMethodObj == null) {
                return new RegisterResult(false, "Target method not found: " + targetMethod, 0);
            }
            
            // 执行Hook
            SandHook.HookResult hookResult = mSandHook.hookMethod(targetMethodObj, hookMethod);
            if (!hookResult.success) {
                return new RegisterResult(false, "Hook failed: " + hookResult.message, 0);
            }
            
            // 注册Hook信息
            String hookKey = getHookKey(targetClass, targetMethod, targetParameterTypes);
            HookInfo hookInfo = new HookInfo(targetClass, targetMethod, targetParameterTypes,
                                           hookMethod, priority, true);
            mHookRegistry.put(hookKey, hookInfo);
            
            Log.d(TAG, "Hook method registered successfully: " + hookKey);
            return new RegisterResult(true, "Hook registered successfully", 1);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to register hook method", e);
            return new RegisterResult(false, "Exception: " + e.getMessage(), 0);
        }
    }
    
    /**
     * 取消注册Hook类
     * @param hookClass Hook类
     * @return 取消注册结果
     */
    public UnregisterResult unregisterHookClass(Class<?> hookClass) {
        if (mSandHook == null) {
            Log.e(TAG, "SandHook not initialized");
            return new UnregisterResult(false, "SandHook not initialized", 0);
        }
        
        try {
            Log.d(TAG, "Unregistering hook class: " + hookClass.getName());
            
            int successCount = 0;
            int totalCount = 0;
            
            // 查找并取消注册相关的Hook
            for (HookInfo hookInfo : mHookRegistry.values()) {
                if (hookInfo.hookMethod.getDeclaringClass().equals(hookClass)) {
                    totalCount++;
                    
                    // 取消Hook
                    boolean success = unregisterHookMethod(hookInfo);
                    if (success) {
                        successCount++;
                    }
                }
            }
            
            Log.d(TAG, "Hook class unregistered: " + successCount + "/" + totalCount + " methods");
            return new UnregisterResult(true, "Unregistered " + successCount + "/" + totalCount + " methods", successCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister hook class", e);
            return new UnregisterResult(false, "Exception: " + e.getMessage(), 0);
        }
    }
    
    /**
     * 取消注册Hook方法
     * @param hookInfo Hook信息
     * @return 是否成功
     */
    private boolean unregisterHookMethod(HookInfo hookInfo) {
        try {
            // 获取目标方法
            Class<?> targetClass = Class.forName(hookInfo.targetClass);
            Method targetMethod = null;
            
            if (hookInfo.targetParameterTypes.length > 0) {
                Class<?>[] parameterTypes = new Class<?>[hookInfo.targetParameterTypes.length];
                for (int i = 0; i < hookInfo.targetParameterTypes.length; i++) {
                    parameterTypes[i] = Class.forName(hookInfo.targetParameterTypes[i]);
                }
                targetMethod = targetClass.getDeclaredMethod(hookInfo.targetMethod, parameterTypes);
            } else {
                Method[] methods = targetClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(hookInfo.targetMethod) && 
                        method.getParameterCount() == hookInfo.hookMethod.getParameterCount()) {
                        targetMethod = method;
                        break;
                    }
                }
            }
            
            if (targetMethod == null) {
                Log.w(TAG, "Target method not found for unregister: " + hookInfo.targetMethod);
                return false;
            }
            
            // 取消Hook
            boolean success = mSandHook.unhookMethod(targetMethod);
            if (success) {
                String hookKey = getHookKey(hookInfo.targetClass, hookInfo.targetMethod, hookInfo.targetParameterTypes);
                mHookRegistry.remove(hookKey);
                Log.d(TAG, "Hook method unregistered successfully: " + hookKey);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister hook method", e);
            return false;
        }
    }
    
    /**
     * 启用Hook
     * @param targetClass 目标类名
     * @param targetMethod 目标方法名
     * @return 是否成功
     */
    public boolean enableHook(String targetClass, String targetMethod) {
        try {
            String hookKey = getHookKey(targetClass, targetMethod, new String[0]);
            HookInfo hookInfo = mHookRegistry.get(hookKey);
            if (hookInfo != null) {
                // 重新注册Hook
                return registerHookMethod(hookInfo.hookMethod, createHookAnnotation(hookInfo)).success;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable hook", e);
            return false;
        }
    }
    
    /**
     * 禁用Hook
     * @param targetClass 目标类名
     * @param targetMethod 目标方法名
     * @return 是否成功
     */
    public boolean disableHook(String targetClass, String targetMethod) {
        try {
            String hookKey = getHookKey(targetClass, targetMethod, new String[0]);
            HookInfo hookInfo = mHookRegistry.get(hookKey);
            if (hookInfo != null) {
                return unregisterHookMethod(hookInfo);
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable hook", e);
            return false;
        }
    }
    
    /**
     * 获取Hook信息
     * @param targetClass 目标类名
     * @param targetMethod 目标方法名
     * @return Hook信息
     */
    public HookInfo getHookInfo(String targetClass, String targetMethod) {
        String hookKey = getHookKey(targetClass, targetMethod, new String[0]);
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
        if (mSandHook == null) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up all hooks...");
            
            // 取消所有Hook
            for (HookInfo hookInfo : mHookRegistry.values()) {
                unregisterHookMethod(hookInfo);
            }
            
            // 清理注册表
            mHookRegistry.clear();
            
            Log.d(TAG, "All hooks cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup all hooks", e);
        }
    }
    
    /**
     * 获取Hook键值
     */
    private String getHookKey(String targetClass, String targetMethod, String[] targetParameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(targetClass).append(".").append(targetMethod);
        
        if (targetParameterTypes.length > 0) {
            sb.append("(");
            for (int i = 0; i < targetParameterTypes.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(targetParameterTypes[i]);
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * 创建Hook注解（用于内部使用）
     */
    private Hook createHookAnnotation(HookInfo hookInfo) {
        return new Hook() {
            @Override
            public String targetClass() {
                return hookInfo.targetClass;
            }
            
            @Override
            public String targetMethod() {
                return hookInfo.targetMethod;
            }
            
            @Override
            public String[] targetParameterTypes() {
                return hookInfo.targetParameterTypes;
            }
            
            @Override
            public int priority() {
                return hookInfo.priority;
            }
            
            @Override
            public boolean enabled() {
                return hookInfo.enabled;
            }
            
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Hook.class;
            }
        };
    }
    
    /**
     * 注册结果类
     */
    public static class RegisterResult {
        public final boolean success;
        public final String message;
        public final int count;
        
        public RegisterResult(boolean success, String message, int count) {
            this.success = success;
            this.message = message;
            this.count = count;
        }
    }
    
    /**
     * 取消注册结果类
     */
    public static class UnregisterResult {
        public final boolean success;
        public final String message;
        public final int count;
        
        public UnregisterResult(boolean success, String message, int count) {
            this.success = success;
            this.message = message;
            this.count = count;
        }
    }
} 