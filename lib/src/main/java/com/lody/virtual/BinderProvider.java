package com.lody.virtual;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Binder提供者
 * 负责管理虚拟服务的Binder连接
 */
public class BinderProvider {
    
    private static final String TAG = "BinderProvider";
    private static BinderProvider sInstance;
    
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, IBinder> mVirtualServices = new ConcurrentHashMap<>();
    
    private Context mContext;
    
    private BinderProvider() {
        // 私有构造函数，实现单例模式
    }
    
    public static BinderProvider getInstance() {
        if (sInstance == null) {
            synchronized (BinderProvider.class) {
                if (sInstance == null) {
                    sInstance = new BinderProvider();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化Binder提供者
     * @param context 上下文
     * @return 初始化是否成功
     */
    public boolean initialize(Context context) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "BinderProvider already initialized");
            return true;
        }
        
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing BinderProvider...");
            
            mContext = context.getApplicationContext();
            
            // 清理服务缓存
            mVirtualServices.clear();
            
            mIsInitialized.set(true);
            Log.d(TAG, "BinderProvider initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize BinderProvider", e);
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
     * 注册虚拟服务
     * @param serviceName 服务名称
     * @param binder 服务Binder
     */
    public void registerService(String serviceName, IBinder binder) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "BinderProvider not initialized");
            return;
        }
        
        if (serviceName == null || binder == null) {
            Log.e(TAG, "Invalid service name or binder");
            return;
        }
        
        mVirtualServices.put(serviceName, binder);
        Log.d(TAG, "Registered virtual service: " + serviceName);
    }
    
    /**
     * 注销虚拟服务
     * @param serviceName 服务名称
     */
    public void unregisterService(String serviceName) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "BinderProvider not initialized");
            return;
        }
        
        if (serviceName == null) {
            Log.e(TAG, "Invalid service name");
            return;
        }
        
        mVirtualServices.remove(serviceName);
        Log.d(TAG, "Unregistered virtual service: " + serviceName);
    }
    
    /**
     * 获取虚拟服务
     * @param serviceName 服务名称
     * @return 服务Binder
     */
    public IBinder getService(String serviceName) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "BinderProvider not initialized");
            return null;
        }
        
        if (serviceName == null) {
            Log.e(TAG, "Invalid service name");
            return null;
        }
        
        IBinder binder = mVirtualServices.get(serviceName);
        if (binder == null) {
            Log.w(TAG, "Service not found: " + serviceName);
        }
        
        return binder;
    }
    
    /**
     * 检查服务是否存在
     * @param serviceName 服务名称
     * @return 是否存在
     */
    public boolean hasService(String serviceName) {
        if (!mIsInitialized.get()) {
            return false;
        }
        
        return serviceName != null && mVirtualServices.containsKey(serviceName);
    }
    
    /**
     * 获取所有服务名称
     * @return 服务名称列表
     */
    public String[] getAllServiceNames() {
        if (!mIsInitialized.get()) {
            return new String[0];
        }
        
        return mVirtualServices.keySet().toArray(new String[0]);
    }
    
    /**
     * 清理所有服务
     */
    public void cleanup() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up BinderProvider...");
            
            mVirtualServices.clear();
            mIsInitialized.set(false);
            
            Log.d(TAG, "BinderProvider cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup BinderProvider", e);
        }
    }
} 