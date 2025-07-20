package com.lody.virtual;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟包管理器
 * 负责虚拟应用的安装、卸载和信息管理
 */
public class VPackageManager {
    
    private static final String TAG = "VPackageManager";
    private static VPackageManager sInstance;
    
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, VAppInfo> mVirtualApps = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PackageInfo> mVirtualPackageInfos = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ApplicationInfo> mVirtualApplicationInfos = new ConcurrentHashMap<>();
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    
    private VPackageManager() {
        // 私有构造函数，实现单例模式
    }
    
    public static VPackageManager getInstance() {
        if (sInstance == null) {
            synchronized (VPackageManager.class) {
                if (sInstance == null) {
                    sInstance = new VPackageManager();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化虚拟包管理器
     * @param context 上下文
     * @param virtualCore 虚拟核心实例
     * @return 初始化是否成功
     */
    public boolean initialize(Context context, VirtualCore virtualCore) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "VPackageManager already initialized");
            return true;
        }
        
        if (context == null || virtualCore == null) {
            Log.e(TAG, "Context or VirtualCore is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing VPackageManager...");
            
            mContext = context.getApplicationContext();
            mVirtualCore = virtualCore;
            
            // 清理数据
            mVirtualApps.clear();
            mVirtualPackageInfos.clear();
            mVirtualApplicationInfos.clear();
            
            // 加载已安装的虚拟应用
            loadInstalledVirtualApps();
            
            mIsInitialized.set(true);
            Log.d(TAG, "VPackageManager initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize VPackageManager", e);
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
     * 安装虚拟应用
     * @param apkPath APK文件路径
     * @return 安装结果
     */
    public InstallResult installVirtualApp(String apkPath) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VPackageManager not initialized");
            return new InstallResult(false, "VPackageManager not initialized", null);
        }
        
        try {
            Log.d(TAG, "Installing virtual app from: " + apkPath);
            
            // 检查APK文件是否存在
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                return new InstallResult(false, "APK file not found: " + apkPath, null);
            }
            
            // 解析APK信息
            PackageInfo packageInfo = parseApkInfo(apkPath);
            if (packageInfo == null) {
                return new InstallResult(false, "Failed to parse APK info", null);
            }
            
            String packageName = packageInfo.packageName;
            
            // 检查包名是否已存在
            if (mVirtualApps.containsKey(packageName)) {
                return new InstallResult(false, "Virtual app already installed: " + packageName, null);
            }
            
            // 创建虚拟应用信息
            VAppInfo vAppInfo = createVirtualAppInfo(packageInfo, apkPath);
            if (vAppInfo == null) {
                return new InstallResult(false, "Failed to create virtual app info", null);
            }
            
            // 创建虚拟环境
            if (!createVirtualEnvironment(vAppInfo)) {
                return new InstallResult(false, "Failed to create virtual environment", null);
            }
            
            // 复制APK文件到虚拟环境
            if (!copyApkToVirtualEnvironment(vAppInfo)) {
                return new InstallResult(false, "Failed to copy APK to virtual environment", null);
            }
            
            // 注册虚拟应用
            mVirtualApps.put(packageName, vAppInfo);
            mVirtualPackageInfos.put(packageName, packageInfo);
            mVirtualApplicationInfos.put(packageName, packageInfo.applicationInfo);
            
            // 保存到持久化存储
            saveVirtualAppInfo(vAppInfo);
            
            Log.d(TAG, "Virtual app installed successfully: " + packageName);
            return new InstallResult(true, "Install successful", vAppInfo);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install virtual app", e);
            return new InstallResult(false, "Exception: " + e.getMessage(), null);
        }
    }
    
    /**
     * 卸载虚拟应用
     * @param packageName 包名
     * @return 卸载是否成功
     */
    public boolean uninstallVirtualApp(String packageName) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "VPackageManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Uninstalling virtual app: " + packageName);
            
            // 检查虚拟应用是否存在
            VAppInfo vAppInfo = mVirtualApps.get(packageName);
            if (vAppInfo == null) {
                Log.w(TAG, "Virtual app not found: " + packageName);
                return true; // 不存在也算成功
            }
            
            // 停止虚拟应用进程
            mVirtualCore.stopVirtualApp(packageName);
            
            // 删除虚拟环境
            if (!deleteVirtualEnvironment(vAppInfo)) {
                Log.w(TAG, "Failed to delete virtual environment for: " + packageName);
            }
            
            // 从内存中移除
            mVirtualApps.remove(packageName);
            mVirtualPackageInfos.remove(packageName);
            mVirtualApplicationInfos.remove(packageName);
            
            // 从持久化存储中删除
            deleteVirtualAppInfo(packageName);
            
            Log.d(TAG, "Virtual app uninstalled successfully: " + packageName);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall virtual app", e);
            return false;
        }
    }
    
    /**
     * 获取虚拟应用信息
     * @param packageName 包名
     * @return 虚拟应用信息
     */
    public VAppInfo getVirtualAppInfo(String packageName) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        return mVirtualApps.get(packageName);
    }
    
    /**
     * 获取所有虚拟应用信息
     */
    public List<VAppInfo> getAllVirtualApps() {
        if (!mIsInitialized.get()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(mVirtualApps.values());
    }
    
    /**
     * 检查是否是虚拟应用
     * @param packageName 包名
     * @return 是否是虚拟应用
     */
    public boolean isVirtualApp(String packageName) {
        if (!mIsInitialized.get()) {
            return false;
        }
        
        return mVirtualApps.containsKey(packageName);
    }
    
    /**
     * 获取虚拟包信息
     * @param packageName 包名
     * @return 包信息
     */
    public PackageInfo getVirtualPackageInfo(String packageName) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        return mVirtualPackageInfos.get(packageName);
    }
    
    /**
     * 获取虚拟应用信息
     * @param packageName 包名
     * @param flags 标志
     * @return 应用信息
     */
    public ApplicationInfo getVirtualApplicationInfo(String packageName, int flags) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        ApplicationInfo appInfo = mVirtualApplicationInfos.get(packageName);
        if (appInfo != null) {
            // 根据flags过滤信息
            return filterApplicationInfo(appInfo, flags);
        }
        
        return null;
    }
    
    /**
     * 获取虚拟已安装包列表
     * @param flags 标志
     * @return 包信息列表
     */
    public List<PackageInfo> getVirtualInstalledPackages(int flags) {
        if (!mIsInitialized.get()) {
            return new ArrayList<>();
        }
        
        List<PackageInfo> result = new ArrayList<>();
        for (PackageInfo packageInfo : mVirtualPackageInfos.values()) {
            // 根据flags过滤
            if (filterPackageInfo(packageInfo, flags)) {
                result.add(packageInfo);
            }
        }
        
        return result;
    }
    
    /**
     * 解析虚拟活动
     * @param intent Intent
     * @param flags 标志
     * @return 解析信息
     */
    public ResolveInfo resolveVirtualActivity(Intent intent, int flags) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        try {
            String packageName = intent.getPackage();
            if (packageName != null && isVirtualApp(packageName)) {
                // 创建虚拟解析信息
                return createVirtualResolveInfo(intent, packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to resolve virtual activity", e);
        }
        
        return null;
    }
    
    /**
     * 查询虚拟意图活动
     * @param intent Intent
     * @param flags 标志
     * @return 活动列表
     */
    public List<ResolveInfo> queryVirtualIntentActivities(Intent intent, int flags) {
        if (!mIsInitialized.get()) {
            return new ArrayList<>();
        }
        
        List<ResolveInfo> result = new ArrayList<>();
        try {
            String packageName = intent.getPackage();
            if (packageName != null && isVirtualApp(packageName)) {
                ResolveInfo resolveInfo = createVirtualResolveInfo(intent, packageName);
                if (resolveInfo != null) {
                    result.add(resolveInfo);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query virtual intent activities", e);
        }
        
        return result;
    }
    
    /**
     * 查询虚拟意图服务
     * @param intent Intent
     * @param flags 标志
     * @return 服务列表
     */
    public List<ResolveInfo> queryVirtualIntentServices(Intent intent, int flags) {
        if (!mIsInitialized.get()) {
            return new ArrayList<>();
        }
        
        List<ResolveInfo> result = new ArrayList<>();
        try {
            String packageName = intent.getPackage();
            if (packageName != null && isVirtualApp(packageName)) {
                ResolveInfo resolveInfo = createVirtualResolveInfo(intent, packageName);
                if (resolveInfo != null) {
                    result.add(resolveInfo);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query virtual intent services", e);
        }
        
        return result;
    }
    
    /**
     * 检查虚拟权限
     * @param permission 权限名
     * @param packageName 包名
     * @return 权限检查结果
     */
    public int checkVirtualPermission(String permission, String packageName) {
        if (!mIsInitialized.get()) {
            return PackageManager.PERMISSION_DENIED;
        }
        
        try {
            VAppInfo vAppInfo = getVirtualAppInfo(packageName);
            if (vAppInfo != null) {
                // 检查虚拟应用的权限
                return vAppInfo.checkPermission(permission);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to check virtual permission", e);
        }
        
        return PackageManager.PERMISSION_DENIED;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up VPackageManager...");
            
            // 清理数据
            mVirtualApps.clear();
            mVirtualPackageInfos.clear();
            mVirtualApplicationInfos.clear();
            
            mIsInitialized.set(false);
            Log.d(TAG, "VPackageManager cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup VPackageManager", e);
        }
    }
    
    /**
     * 加载已安装的虚拟应用
     */
    private void loadInstalledVirtualApps() {
        try {
            Log.d(TAG, "Loading installed virtual apps...");
            
            // 从持久化存储加载虚拟应用信息
            List<VAppInfo> installedApps = loadVirtualAppInfos();
            for (VAppInfo vAppInfo : installedApps) {
                mVirtualApps.put(vAppInfo.packageName, vAppInfo);
                
                // 重新解析包信息
                PackageInfo packageInfo = parseApkInfo(vAppInfo.apkPath);
                if (packageInfo != null) {
                    mVirtualPackageInfos.put(vAppInfo.packageName, packageInfo);
                    mVirtualApplicationInfos.put(vAppInfo.packageName, packageInfo.applicationInfo);
                }
            }
            
            Log.d(TAG, "Loaded " + installedApps.size() + " virtual apps");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load installed virtual apps", e);
        }
    }
    
    /**
     * 解析APK信息
     */
    private PackageInfo parseApkInfo(String apkPath) {
        try {
            return mContext.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES | 
                                                                              PackageManager.GET_SERVICES | 
                                                                              PackageManager.GET_PROVIDERS |
                                                                              PackageManager.GET_PERMISSIONS);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse APK info", e);
            return null;
        }
    }
    
    /**
     * 创建虚拟应用信息
     */
    private VAppInfo createVirtualAppInfo(PackageInfo packageInfo, String apkPath) {
        try {
            VAppInfo vAppInfo = new VAppInfo();
            vAppInfo.packageName = packageInfo.packageName;
            vAppInfo.apkPath = apkPath;
            vAppInfo.versionName = packageInfo.versionName;
            vAppInfo.versionCode = packageInfo.versionCode;
            vAppInfo.installTime = System.currentTimeMillis();
            vAppInfo.isEnabled = true;
            
            // 设置虚拟路径
            vAppInfo.dataDir = VEnvironment.getVirtualDataDir(packageInfo.packageName);
            vAppInfo.libDir = VEnvironment.getVirtualLibDir(packageInfo.packageName);
            vAppInfo.cacheDir = VEnvironment.getVirtualCacheDir(packageInfo.packageName);
            
            return vAppInfo;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create virtual app info", e);
            return null;
        }
    }
    
    /**
     * 创建虚拟环境
     */
    private boolean createVirtualEnvironment(VAppInfo vAppInfo) {
        try {
            // 创建数据目录
            File dataDir = new File(vAppInfo.dataDir);
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                Log.e(TAG, "Failed to create data directory: " + vAppInfo.dataDir);
                return false;
            }
            
            // 创建库目录
            File libDir = new File(vAppInfo.libDir);
            if (!libDir.exists() && !libDir.mkdirs()) {
                Log.e(TAG, "Failed to create lib directory: " + vAppInfo.libDir);
                return false;
            }
            
            // 创建缓存目录
            File cacheDir = new File(vAppInfo.cacheDir);
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                Log.e(TAG, "Failed to create cache directory: " + vAppInfo.cacheDir);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create virtual environment", e);
            return false;
        }
    }
    
    /**
     * 复制APK到虚拟环境
     */
    private boolean copyApkToVirtualEnvironment(VAppInfo vAppInfo) {
        try {
            String virtualApkPath = VEnvironment.getVirtualApkPath(vAppInfo.packageName);
            File sourceFile = new File(vAppInfo.apkPath);
            File targetFile = new File(virtualApkPath);
            
            // 确保目标目录存在
            targetFile.getParentFile().mkdirs();
            
            // 复制文件
            java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(), 
                                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 更新APK路径
            vAppInfo.apkPath = virtualApkPath;
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy APK to virtual environment", e);
            return false;
        }
    }
    
    /**
     * 删除虚拟环境
     */
    private boolean deleteVirtualEnvironment(VAppInfo vAppInfo) {
        try {
            // 删除数据目录
            File dataDir = new File(vAppInfo.dataDir);
            if (dataDir.exists()) {
                deleteDirectory(dataDir);
            }
            
            // 删除库目录
            File libDir = new File(vAppInfo.libDir);
            if (libDir.exists()) {
                deleteDirectory(libDir);
            }
            
            // 删除缓存目录
            File cacheDir = new File(vAppInfo.cacheDir);
            if (cacheDir.exists()) {
                deleteDirectory(cacheDir);
            }
            
            // 删除APK文件
            File apkFile = new File(vAppInfo.apkPath);
            if (apkFile.exists()) {
                apkFile.delete();
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete virtual environment", e);
            return false;
        }
    }
    
    /**
     * 删除目录
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
    
    /**
     * 保存虚拟应用信息
     */
    private void saveVirtualAppInfo(VAppInfo vAppInfo) {
        try {
            // TODO: 实现持久化存储
            Log.d(TAG, "Saving virtual app info: " + vAppInfo.packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save virtual app info", e);
        }
    }
    
    /**
     * 删除虚拟应用信息
     */
    private void deleteVirtualAppInfo(String packageName) {
        try {
            // TODO: 实现持久化存储删除
            Log.d(TAG, "Deleting virtual app info: " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete virtual app info", e);
        }
    }
    
    /**
     * 加载虚拟应用信息
     */
    private List<VAppInfo> loadVirtualAppInfos() {
        try {
            // TODO: 实现持久化存储加载
            Log.d(TAG, "Loading virtual app infos");
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load virtual app infos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 过滤应用信息
     */
    private ApplicationInfo filterApplicationInfo(ApplicationInfo appInfo, int flags) {
        // TODO: 根据flags过滤应用信息
        return appInfo;
    }
    
    /**
     * 过滤包信息
     */
    private boolean filterPackageInfo(PackageInfo packageInfo, int flags) {
        // TODO: 根据flags过滤包信息
        return true;
    }
    
    /**
     * 创建虚拟解析信息
     */
    private ResolveInfo createVirtualResolveInfo(Intent intent, String packageName) {
        try {
            // TODO: 创建虚拟解析信息
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create virtual resolve info", e);
            return null;
        }
    }
    
    /**
     * 安装结果类
     */
    public static class InstallResult {
        public final boolean success;
        public final String message;
        public final VAppInfo vAppInfo;
        
        public InstallResult(boolean success, String message, VAppInfo vAppInfo) {
            this.success = success;
            this.message = message;
            this.vAppInfo = vAppInfo;
        }
    }
    
    /**
     * 卸载结果类
     */
    public static class UninstallResult {
        public final boolean success;
        public final String message;
        
        public UninstallResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
} 