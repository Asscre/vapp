package com.lody.virtual.hooks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.lody.virtual.HookWrapper;
import com.lody.virtual.VirtualCore;

/**
 * 包管理Hook类
 * 实现包信息虚拟化和权限控制
 */
public class PackageManagerHooks {
    
    private static final String TAG = "PackageManagerHooks";
    
    /**
     * Hook PackageManager.getPackageInfo()
     * 返回虚拟化的包信息
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "getPackageInfo",
        targetParameterTypes = {"java.lang.String", "int"},
        priority = 100
    )
    public static PackageInfo hookGetPackageInfo(Object packageManager, String packageName, int flags) {
        try {
            Log.d(TAG, "Hook getPackageInfo: " + packageName + ", flags: " + flags);
            
            // 检查是否是虚拟应用
            if (VirtualCore.getInstance().isVirtualApp(packageName)) {
                // 返回虚拟化的包信息
                PackageInfo virtualInfo = VirtualCore.getInstance().getVirtualPackageInfo(packageName);
                if (virtualInfo != null) {
                    Log.d(TAG, "Returning virtual package info for: " + packageName);
                    return virtualInfo;
                }
            }
            
            // 对于非虚拟应用，调用原始方法
            Log.d(TAG, "Calling original getPackageInfo for: " + packageName);
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getPackageInfo hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.getPackageInfo() - 重载版本
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "getPackageInfo",
        targetParameterTypes = {"java.lang.String", "int", "int"},
        priority = 100
    )
    public static PackageInfo hookGetPackageInfoWithUserId(Object packageManager, String packageName, int flags, int userId) {
        try {
            Log.d(TAG, "Hook getPackageInfo with userId: " + packageName + ", flags: " + flags + ", userId: " + userId);
            
            // 检查是否是虚拟应用
            if (VirtualCore.getInstance().isVirtualApp(packageName)) {
                // 返回虚拟化的包信息
                PackageInfo virtualInfo = VirtualCore.getInstance().getVirtualPackageInfo(packageName);
                if (virtualInfo != null) {
                    Log.d(TAG, "Returning virtual package info for: " + packageName);
                    return virtualInfo;
                }
            }
            
            // 对于非虚拟应用，调用原始方法
            Log.d(TAG, "Calling original getPackageInfo for: " + packageName);
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getPackageInfo with userId hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.getInstalledPackages()
     * 返回虚拟化的已安装包列表
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "getInstalledPackages",
        targetParameterTypes = {"int"},
        priority = 100
    )
    public static Object hookGetInstalledPackages(Object packageManager, int flags) {
        try {
            Log.d(TAG, "Hook getInstalledPackages, flags: " + flags);
            
            // 获取原始包列表
            // 这里需要调用原始方法获取基础列表
            
            // 添加虚拟应用到列表中
            Object virtualPackages = VirtualCore.getInstance().getVirtualInstalledPackages(flags);
            if (virtualPackages != null) {
                Log.d(TAG, "Added virtual packages to installed packages list");
                // 合并虚拟包到原始列表中
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getInstalledPackages hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.getInstalledPackages() - 重载版本
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "getInstalledPackages",
        targetParameterTypes = {"int", "int"},
        priority = 100
    )
    public static Object hookGetInstalledPackagesWithUserId(Object packageManager, int flags, int userId) {
        try {
            Log.d(TAG, "Hook getInstalledPackages with userId, flags: " + flags + ", userId: " + userId);
            
            // 获取原始包列表
            // 这里需要调用原始方法获取基础列表
            
            // 添加虚拟应用到列表中
            Object virtualPackages = VirtualCore.getInstance().getVirtualInstalledPackages(flags);
            if (virtualPackages != null) {
                Log.d(TAG, "Added virtual packages to installed packages list");
                // 合并虚拟包到原始列表中
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getInstalledPackages with userId hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.resolveActivity()
     * 解析虚拟应用的活动
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "resolveActivity",
        targetParameterTypes = {"android.content.Intent", "int"},
        priority = 100
    )
    public static Object hookResolveActivity(Object packageManager, Object intent, int flags) {
        try {
            Log.d(TAG, "Hook resolveActivity, flags: " + flags);
            
            // 检查Intent是否指向虚拟应用
            String packageName = getIntentPackage(intent);
            if (packageName != null && VirtualCore.getInstance().isVirtualApp(packageName)) {
                // 解析虚拟应用的活动
                Object virtualResolveInfo = VirtualCore.getInstance().resolveVirtualActivity(intent, flags);
                if (virtualResolveInfo != null) {
                    Log.d(TAG, "Returning virtual resolve info for: " + packageName);
                    return virtualResolveInfo;
                }
            }
            
            // 对于非虚拟应用，调用原始方法
            Log.d(TAG, "Calling original resolveActivity");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in resolveActivity hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.queryIntentActivities()
     * 查询虚拟应用的活动列表
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "queryIntentActivities",
        targetParameterTypes = {"android.content.Intent", "int"},
        priority = 100
    )
    public static Object hookQueryIntentActivities(Object packageManager, Object intent, int flags) {
        try {
            Log.d(TAG, "Hook queryIntentActivities, flags: " + flags);
            
            // 获取原始活动列表
            // 这里需要调用原始方法获取基础列表
            
            // 添加虚拟应用的活动到列表中
            Object virtualActivities = VirtualCore.getInstance().queryVirtualIntentActivities(intent, flags);
            if (virtualActivities != null) {
                Log.d(TAG, "Added virtual activities to intent activities list");
                // 合并虚拟活动到原始列表中
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in queryIntentActivities hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.queryIntentServices()
     * 查询虚拟应用的服务列表
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "queryIntentServices",
        targetParameterTypes = {"android.content.Intent", "int"},
        priority = 100
    )
    public static Object hookQueryIntentServices(Object packageManager, Object intent, int flags) {
        try {
            Log.d(TAG, "Hook queryIntentServices, flags: " + flags);
            
            // 获取原始服务列表
            // 这里需要调用原始方法获取基础列表
            
            // 添加虚拟应用的服务到列表中
            Object virtualServices = VirtualCore.getInstance().queryVirtualIntentServices(intent, flags);
            if (virtualServices != null) {
                Log.d(TAG, "Added virtual services to intent services list");
                // 合并虚拟服务到原始列表中
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in queryIntentServices hook", e);
        }
        
        return null;
    }
    
    /**
     * Hook PackageManager.checkPermission()
     * 检查虚拟应用的权限
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "checkPermission",
        targetParameterTypes = {"java.lang.String", "java.lang.String"},
        priority = 100
    )
    public static int hookCheckPermission(Object packageManager, String permission, String packageName) {
        try {
            Log.d(TAG, "Hook checkPermission: " + permission + " for " + packageName);
            
            // 检查是否是虚拟应用
            if (VirtualCore.getInstance().isVirtualApp(packageName)) {
                // 检查虚拟应用的权限
                int result = VirtualCore.getInstance().checkVirtualPermission(permission, packageName);
                Log.d(TAG, "Virtual permission check result: " + result);
                return result;
            }
            
            // 对于非虚拟应用，调用原始方法
            Log.d(TAG, "Calling original checkPermission");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in checkPermission hook", e);
        }
        
        return PackageManager.PERMISSION_DENIED;
    }
    
    /**
     * Hook PackageManager.getApplicationInfo()
     * 获取虚拟应用的应用信息
     */
    @HookWrapper.Hook(
        targetClass = "android.app.ApplicationPackageManager",
        targetMethod = "getApplicationInfo",
        targetParameterTypes = {"java.lang.String", "int"},
        priority = 100
    )
    public static Object hookGetApplicationInfo(Object packageManager, String packageName, int flags) {
        try {
            Log.d(TAG, "Hook getApplicationInfo: " + packageName + ", flags: " + flags);
            
            // 检查是否是虚拟应用
            if (VirtualCore.getInstance().isVirtualApp(packageName)) {
                // 返回虚拟化的应用信息
                Object virtualAppInfo = VirtualCore.getInstance().getVirtualApplicationInfo(packageName, flags);
                if (virtualAppInfo != null) {
                    Log.d(TAG, "Returning virtual application info for: " + packageName);
                    return virtualAppInfo;
                }
            }
            
            // 对于非虚拟应用，调用原始方法
            Log.d(TAG, "Calling original getApplicationInfo for: " + packageName);
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getApplicationInfo hook", e);
        }
        
        return null;
    }
    
    /**
     * 获取Intent包名的辅助方法
     */
    private static String getIntentPackage(Object intent) {
        try {
            // 通过反射获取Intent的包名
            java.lang.reflect.Method getPackageMethod = intent.getClass().getMethod("getPackage");
            return (String) getPackageMethod.invoke(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get intent package", e);
            return null;
        }
    }
} 