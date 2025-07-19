package com.virtualspace.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Binder提供者
 * 负责管理虚拟服务的Binder连接
 */
public class BinderProvider extends ContentProvider {
    
    private static final String TAG = "BinderProvider";
    private static final String AUTHORITY = "com.virtualspace.app.binder";
    
    // 虚拟服务缓存
    private static final ConcurrentHashMap<String, IBinder> sVirtualServices = new ConcurrentHashMap<>();
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "BinderProvider onCreate");
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // 不实现查询功能
        return null;
    }
    
    @Override
    public String getType(Uri uri) {
        return "application/octet-stream";
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 不实现插入功能
        return null;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // 不实现删除功能
        return 0;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // 不实现更新功能
        return 0;
    }
    
    /**
     * 注册虚拟服务
     * @param serviceName 服务名称
     * @param binder 服务Binder
     */
    public static void registerService(String serviceName, IBinder binder) {
        if (serviceName == null || binder == null) {
            Log.e(TAG, "Invalid service name or binder");
            return;
        }
        
        sVirtualServices.put(serviceName, binder);
        Log.d(TAG, "Registered virtual service: " + serviceName);
    }
    
    /**
     * 注销虚拟服务
     * @param serviceName 服务名称
     */
    public static void unregisterService(String serviceName) {
        if (serviceName == null) {
            Log.e(TAG, "Invalid service name");
            return;
        }
        
        sVirtualServices.remove(serviceName);
        Log.d(TAG, "Unregistered virtual service: " + serviceName);
    }
    
    /**
     * 获取虚拟服务
     * @param serviceName 服务名称
     * @return 服务Binder
     */
    public static IBinder getService(String serviceName) {
        if (serviceName == null) {
            Log.e(TAG, "Invalid service name");
            return null;
        }
        
        IBinder binder = sVirtualServices.get(serviceName);
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
    public static boolean hasService(String serviceName) {
        return serviceName != null && sVirtualServices.containsKey(serviceName);
    }
    
    /**
     * 获取所有服务名称
     * @return 服务名称列表
     */
    public static String[] getAllServiceNames() {
        return sVirtualServices.keySet().toArray(new String[0]);
    }
    
    /**
     * 清理所有服务
     */
    public static void cleanup() {
        sVirtualServices.clear();
        Log.d(TAG, "All virtual services cleaned up");
    }
} 