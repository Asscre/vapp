package com.lody.virtual;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.service.VPackageManagerService;
import com.lody.virtual.service.VActivityManagerService;
import com.lody.virtual.service.VContentProviderService;
import com.lody.virtual.service.VPermissionManagerService;
import com.lody.virtual.service.VServiceManagerService;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟化客户端类
 * 负责在虚拟进程中初始化虚拟化环境
 */
public class VClient {
    
    private static final String TAG = "VClient";
    private static VClient sInstance;
    
    private Context mContext;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private VEnvironment mEnvironment;
    private IOUniformer mIOUniformer;
    private BinderProvider mBinderProvider;
    
    // 虚拟进程信息
    private String mVirtualPackageName;
    private int mVirtualUserId;
    private String mVirtualProcessName;
    
    private VClient() {
        // 私有构造函数，实现单例模式
    }
    
    public static VClient getInstance() {
        if (sInstance == null) {
            synchronized (VClient.class) {
                if (sInstance == null) {
                    sInstance = new VClient();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化虚拟化客户端
     * @param context 应用上下文
     * @param packageName 虚拟包名
     * @param userId 虚拟用户ID
     * @param processName 虚拟进程名
     * @return 初始化是否成功
     */
    public boolean initialize(Context context, String packageName, int userId, String processName) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "VClient already initialized");
            return true;
        }
        
        try {
            Log.d(TAG, "Initializing VClient for package: " + packageName);
            
            mContext = context.getApplicationContext();
            mVirtualPackageName = packageName;
            mVirtualUserId = userId;
            mVirtualProcessName = processName;
            
            // 初始化环境管理器
            mEnvironment = new VEnvironment(mContext);
            mEnvironment.initialize();
            
            // 初始化IO重定向器
            mIOUniformer = IOUniformer.getInstance();
            mIOUniformer.initialize(mContext);
            
            // 初始化Binder提供者
            mBinderProvider = BinderProvider.getInstance();
            mBinderProvider.initialize(mContext);
            
            // 设置虚拟环境
            setupVirtualEnvironment();
            
            // 启动IO重定向
            startIORedirection();
            
            // 注册虚拟服务
            registerVirtualServices();
            
            mIsInitialized.set(true);
            Log.d(TAG, "VClient initialized successfully for package: " + packageName);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize VClient", e);
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
     * 获取虚拟包名
     */
    public String getVirtualPackageName() {
        return mVirtualPackageName;
    }
    
    /**
     * 获取虚拟用户ID
     */
    public int getVirtualUserId() {
        return mVirtualUserId;
    }
    
    /**
     * 获取虚拟进程名
     */
    public String getVirtualProcessName() {
        return mVirtualProcessName;
    }
    
    /**
     * 获取环境管理器
     */
    public VEnvironment getEnvironment() {
        return mEnvironment;
    }
    
    /**
     * 获取IO重定向器
     */
    public IOUniformer getIOUniformer() {
        return mIOUniformer;
    }
    
    /**
     * 获取Binder提供者
     */
    public BinderProvider getBinderProvider() {
        return mBinderProvider;
    }
    
    /**
     * 设置虚拟环境
     */
    private void setupVirtualEnvironment() {
        try {
            Log.d(TAG, "Setting up virtual environment...");
            
            // 设置虚拟数据目录
            String virtualDataDir = mEnvironment.getVirtualDataPath(mVirtualPackageName);
            if (!mEnvironment.createDirectory(virtualDataDir)) {
                throw new RuntimeException("Failed to create virtual data directory");
            }
            
            // 设置虚拟缓存目录
            String virtualCacheDir = mEnvironment.getVirtualCachePath(mVirtualPackageName);
            if (!mEnvironment.createDirectory(virtualCacheDir)) {
                throw new RuntimeException("Failed to create virtual cache directory");
            }
            
            // 设置虚拟外部存储目录
            String virtualExternalDir = mEnvironment.getVirtualExternalPath(mVirtualPackageName);
            if (!mEnvironment.createDirectory(virtualExternalDir)) {
                throw new RuntimeException("Failed to create virtual external directory");
            }
            
            // 设置虚拟数据库目录
            String virtualDatabaseDir = mEnvironment.getVirtualDatabasePath(mVirtualPackageName);
            if (!mEnvironment.createDirectory(virtualDatabaseDir)) {
                throw new RuntimeException("Failed to create virtual database directory");
            }
            
            // 设置虚拟SharedPreferences目录
            String virtualPrefsDir = mEnvironment.getVirtualPrefsPath(mVirtualPackageName);
            if (!mEnvironment.createDirectory(virtualPrefsDir)) {
                throw new RuntimeException("Failed to create virtual prefs directory");
            }
            
            Log.d(TAG, "Virtual environment setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup virtual environment", e);
            throw new RuntimeException("Failed to setup virtual environment", e);
        }
    }
    
    /**
     * 启动IO重定向
     */
    private void startIORedirection() {
        try {
            Log.d(TAG, "Starting IO redirection...");
            
            // 启动Native层IO重定向
            if (!mIOUniformer.startIORedirection()) {
                throw new RuntimeException("Failed to start IO redirection");
            }
            
            // 设置文件路径映射
            setupPathMapping();
            
            Log.d(TAG, "IO redirection started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start IO redirection", e);
            throw new RuntimeException("Failed to start IO redirection", e);
        }
    }
    
    /**
     * 设置路径映射
     */
    private void setupPathMapping() {
        try {
            // 数据目录映射
            String realDataDir = mContext.getApplicationInfo().dataDir;
            String virtualDataDir = mEnvironment.getVirtualDataPath(mVirtualPackageName);
            mIOUniformer.addPathMapping(realDataDir, virtualDataDir);
            
            // 缓存目录映射
            String realCacheDir = mContext.getCacheDir().getAbsolutePath();
            String virtualCacheDir = mEnvironment.getVirtualCachePath(mVirtualPackageName);
            mIOUniformer.addPathMapping(realCacheDir, virtualCacheDir);
            
            // 外部存储目录映射
            String realExternalDir = mContext.getExternalFilesDir(null).getAbsolutePath();
            String virtualExternalDir = mEnvironment.getVirtualExternalPath(mVirtualPackageName);
            mIOUniformer.addPathMapping(realExternalDir, virtualExternalDir);
            
            // 数据库目录映射
            String realDatabaseDir = mContext.getDatabasePath("dummy").getParent();
            String virtualDatabaseDir = mEnvironment.getVirtualDatabasePath(mVirtualPackageName);
            mIOUniformer.addPathMapping(realDatabaseDir, virtualDatabaseDir);
            
            // SharedPreferences目录映射
            String realPrefsDir = mContext.getApplicationInfo().dataDir + "/shared_prefs";
            String virtualPrefsDir = mEnvironment.getVirtualPrefsPath(mVirtualPackageName);
            mIOUniformer.addPathMapping(realPrefsDir, virtualPrefsDir);
            
            Log.d(TAG, "Path mapping setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup path mapping", e);
            throw new RuntimeException("Failed to setup path mapping", e);
        }
    }
    
    /**
     * 注册虚拟服务
     */
    private void registerVirtualServices() {
        try {
            Log.d(TAG, "Registering virtual services...");
            
            // 注册虚拟包管理器服务
            mBinderProvider.registerService("package", new VPackageManagerService(mContext));
            
            // 注册虚拟活动管理器服务
            mBinderProvider.registerService("activity", new VActivityManagerService(mContext));
            
            // 注册虚拟内容提供者服务
            mBinderProvider.registerService("content", new VContentProviderService(mContext));
            
            // 注册虚拟权限管理器服务
            mBinderProvider.registerService("permission", new VPermissionManagerService(mContext));
            
            Log.d(TAG, "Virtual services registered successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to register virtual services", e);
            throw new RuntimeException("Failed to register virtual services", e);
        }
    }
    
    /**
     * 启动虚拟Activity
     * @param intent 启动意图
     * @return 启动结果
     */
    public boolean startVirtualActivity(Intent intent) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VClient not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Starting virtual activity: " + intent.getComponent());
            
            // 修改Intent的包名
            intent.setPackage(mVirtualPackageName);
            
            // 添加虚拟用户ID
            intent.putExtra("virtual_user_id", mVirtualUserId);
            
            // 通过虚拟活动管理器启动
            VActivityManagerService activityManager = 
                    (VActivityManagerService) mBinderProvider.getService("activity");
            
            if (activityManager != null) {
                return activityManager.startActivity(intent);
            } else {
                Log.e(TAG, "Activity manager service not found");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start virtual activity", e);
            return false;
        }
    }
    
    /**
     * 绑定虚拟服务
     * @param intent 服务意图
     * @return 服务连接
     */
    public IBinder bindVirtualService(Intent intent) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VClient not initialized");
            return null;
        }
        
        try {
            Log.d(TAG, "Binding virtual service: " + intent.getComponent());
            
            // 修改Intent的包名
            intent.setPackage(mVirtualPackageName);
            
            // 添加虚拟用户ID
            intent.putExtra("virtual_user_id", mVirtualUserId);
            
            // 通过虚拟服务管理器绑定
            VServiceManagerService serviceManager = 
                    (VServiceManagerService) mBinderProvider.getService("service");
            
            if (serviceManager != null) {
                return serviceManager.bindService(intent);
            } else {
                Log.e(TAG, "Service manager service not found");
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind virtual service", e);
            return null;
        }
    }
    
    /**
     * 查询虚拟内容提供者
     * @param uri 内容URI
     * @return 查询结果
     */
    public Object queryVirtualContentProvider(String uri) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VClient not initialized");
            return null;
        }
        
        try {
            Log.d(TAG, "Querying virtual content provider: " + uri);
            
            // 通过虚拟内容提供者管理器查询
            VContentProviderService contentProvider = 
                    (VContentProviderService) mBinderProvider.getService("content");
            
            if (contentProvider != null) {
                return contentProvider.query(uri);
            } else {
                Log.e(TAG, "Content provider service not found");
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to query virtual content provider", e);
            return null;
        }
    }
    
    /**
     * 检查虚拟权限
     * @param permission 权限名称
     * @return 是否有权限
     */
    public boolean checkVirtualPermission(String permission) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VClient not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Checking virtual permission: " + permission);
            
            // 通过虚拟权限管理器检查
            VPermissionManagerService permissionManager = 
                    (VPermissionManagerService) mBinderProvider.getService("permission");
            
            if (permissionManager != null) {
                return permissionManager.checkPermission(permission);
            } else {
                Log.e(TAG, "Permission manager service not found");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check virtual permission", e);
            return false;
        }
    }
    
    /**
     * 清理虚拟环境
     */
    public void cleanup() {
        try {
            Log.d(TAG, "Cleaning up VClient...");
            
            // 停止IO重定向
            if (mIOUniformer != null) {
                mIOUniformer.stopIORedirection();
            }
            
            // 注销虚拟服务
            if (mBinderProvider != null) {
                mBinderProvider.cleanup();
            }
            
            mIsInitialized.set(false);
            Log.d(TAG, "VClient cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup VClient", e);
        }
    }
} 