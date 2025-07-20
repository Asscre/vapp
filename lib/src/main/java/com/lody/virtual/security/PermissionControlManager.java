package com.lody.virtual.security;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.lody.virtual.VirtualCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 权限控制管理器
 * 负责虚拟应用的权限申请、授权和检查
 */
public class PermissionControlManager {
    
    private static final String TAG = "PermissionControlManager";
    private static PermissionControlManager sInstance;
    
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, PermissionPolicy> mPermissionPolicies = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Map<String, Integer>> mPermissionStates = new ConcurrentHashMap<>();
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    
    // 权限状态常量
    public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;
    public static final int PERMISSION_ASK = 2;
    public static final int PERMISSION_NEVER_ASK = 3;
    
    // 权限策略常量
    public static final int POLICY_ALLOW_ALL = 0;
    public static final int POLICY_DENY_ALL = 1;
    public static final int POLICY_WHITELIST = 2;
    public static final int POLICY_BLACKLIST = 3;
    public static final int POLICY_ASK_USER = 4;
    
    // 常用权限列表
    public static final String[] COMMON_PERMISSIONS = {
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_PHONE_STATE",
        "android.permission.CALL_PHONE",
        "android.permission.SEND_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR"
    };
    
    private PermissionControlManager() {
        // 私有构造函数，实现单例模式
    }
    
    public static PermissionControlManager getInstance() {
        if (sInstance == null) {
            synchronized (PermissionControlManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionControlManager();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化权限控制管理器
     * @param context 上下文
     * @param virtualCore 虚拟核心实例
     * @return 初始化是否成功
     */
    public boolean initialize(Context context, VirtualCore virtualCore) {
        if (mIsInitialized.get()) {
            Log.w(TAG, "PermissionControlManager already initialized");
            return true;
        }
        
        if (context == null || virtualCore == null) {
            Log.e(TAG, "Context or VirtualCore is null");
            return false;
        }
        
        try {
            Log.d(TAG, "Initializing PermissionControlManager...");
            
            mContext = context.getApplicationContext();
            mVirtualCore = virtualCore;
            
            // 清理策略和状态
            mPermissionPolicies.clear();
            mPermissionStates.clear();
            
            mIsInitialized.set(true);
            Log.d(TAG, "PermissionControlManager initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize PermissionControlManager", e);
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
     * 设置虚拟应用的权限策略
     * @param packageName 包名
     * @param policy 权限策略
     * @return 设置是否成功
     */
    public boolean setPermissionPolicy(String packageName, PermissionPolicy policy) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "PermissionControlManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Setting permission policy for " + packageName + ": " + policy.policyType);
            mPermissionPolicies.put(packageName, policy);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set permission policy", e);
            return false;
        }
    }
    
    /**
     * 获取虚拟应用的权限策略
     * @param packageName 包名
     * @return 权限策略
     */
    public PermissionPolicy getPermissionPolicy(String packageName) {
        if (!mIsInitialized.get()) {
            return null;
        }
        
        return mPermissionPolicies.get(packageName);
    }
    
    /**
     * 检查权限
     * @param packageName 包名
     * @param permission 权限名
     * @return 权限状态
     */
    public int checkPermission(String packageName, String permission) {
        if (!mIsInitialized.get()) {
            return PERMISSION_DENIED;
        }
        
        try {
            // 获取权限策略
            PermissionPolicy policy = mPermissionPolicies.get(packageName);
            if (policy == null) {
                return PERMISSION_DENIED; // 默认拒绝
            }
            
            // 获取当前权限状态
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState != null && permissionState.containsKey(permission)) {
                return permissionState.get(permission);
            }
            
            // 根据策略决定权限状态
            switch (policy.policyType) {
                case POLICY_ALLOW_ALL:
                    return PERMISSION_GRANTED;
                    
                case POLICY_DENY_ALL:
                    return PERMISSION_DENIED;
                    
                case POLICY_WHITELIST:
                    return policy.whitelist.contains(permission) ? PERMISSION_GRANTED : PERMISSION_DENIED;
                    
                case POLICY_BLACKLIST:
                    return policy.blacklist.contains(permission) ? PERMISSION_DENIED : PERMISSION_GRANTED;
                    
                case POLICY_ASK_USER:
                    return PERMISSION_ASK;
                    
                default:
                    return PERMISSION_DENIED;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check permission", e);
            return PERMISSION_DENIED;
        }
    }
    
    /**
     * 授予权限
     * @param packageName 包名
     * @param permission 权限名
     * @return 是否成功
     */
    public boolean grantPermission(String packageName, String permission) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "PermissionControlManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Granting permission " + permission + " to " + packageName);
            
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState == null) {
                permissionState = new HashMap<>();
                mPermissionStates.put(packageName, permissionState);
            }
            
            permissionState.put(permission, PERMISSION_GRANTED);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to grant permission", e);
            return false;
        }
    }
    
    /**
     * 拒绝权限
     * @param packageName 包名
     * @param permission 权限名
     * @param neverAsk 是否不再询问
     * @return 是否成功
     */
    public boolean denyPermission(String packageName, String permission, boolean neverAsk) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "PermissionControlManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Denying permission " + permission + " to " + packageName + " (neverAsk: " + neverAsk + ")");
            
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState == null) {
                permissionState = new HashMap<>();
                mPermissionStates.put(packageName, permissionState);
            }
            
            permissionState.put(permission, neverAsk ? PERMISSION_NEVER_ASK : PERMISSION_DENIED);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to deny permission", e);
            return false;
        }
    }
    
    /**
     * 撤销权限
     * @param packageName 包名
     * @param permission 权限名
     * @return 是否成功
     */
    public boolean revokePermission(String packageName, String permission) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "PermissionControlManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Revoking permission " + permission + " from " + packageName);
            
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState != null) {
                permissionState.remove(permission);
            }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to revoke permission", e);
            return false;
        }
    }
    
    /**
     * 获取虚拟应用的所有权限状态
     * @param packageName 包名
     * @return 权限状态映射
     */
    public Map<String, Integer> getPermissionStates(String packageName) {
        if (!mIsInitialized.get()) {
            return new HashMap<>();
        }
        
        Map<String, Integer> permissionState = mPermissionStates.get(packageName);
        if (permissionState != null) {
            return new HashMap<>(permissionState);
        }
        
        return new HashMap<>();
    }
    
    /**
     * 获取需要用户确认的权限列表
     * @param packageName 包名
     * @return 权限列表
     */
    public List<String> getPermissionsNeedingUserConfirmation(String packageName) {
        if (!mIsInitialized.get()) {
            return new ArrayList<>();
        }
        
        List<String> permissions = new ArrayList<>();
        
        try {
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState != null) {
                for (Map.Entry<String, Integer> entry : permissionState.entrySet()) {
                    if (entry.getValue() == PERMISSION_ASK) {
                        permissions.add(entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get permissions needing user confirmation", e);
        }
        
        return permissions;
    }
    
    /**
     * 批量设置权限状态
     * @param packageName 包名
     * @param permissions 权限状态映射
     * @return 是否成功
     */
    public boolean setPermissionStates(String packageName, Map<String, Integer> permissions) {
        if (!mIsInitialized.get()) {
            Log.e(TAG, "PermissionControlManager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Setting permission states for " + packageName + ": " + permissions.size() + " permissions");
            
            Map<String, Integer> permissionState = mPermissionStates.get(packageName);
            if (permissionState == null) {
                permissionState = new HashMap<>();
                mPermissionStates.put(packageName, permissionState);
            }
            
            permissionState.clear();
            permissionState.putAll(permissions);
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set permission states", e);
            return false;
        }
    }
    
    /**
     * 检查权限组
     * @param packageName 包名
     * @param permissionGroup 权限组名
     * @return 权限组状态
     */
    public int checkPermissionGroup(String packageName, String permissionGroup) {
        if (!mIsInitialized.get()) {
            return PERMISSION_DENIED;
        }
        
        try {
            // 获取权限组中的所有权限
            List<String> groupPermissions = getPermissionsInGroup(permissionGroup);
            
            // 检查组内所有权限的状态
            boolean allGranted = true;
            boolean anyGranted = false;
            
            for (String permission : groupPermissions) {
                int state = checkPermission(packageName, permission);
                if (state == PERMISSION_GRANTED) {
                    anyGranted = true;
                } else {
                    allGranted = false;
                }
            }
            
            if (allGranted) {
                return PERMISSION_GRANTED;
            } else if (anyGranted) {
                return PERMISSION_ASK; // 部分授权
            } else {
                return PERMISSION_DENIED;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check permission group", e);
            return PERMISSION_DENIED;
        }
    }
    
    /**
     * 获取权限组中的权限列表
     * @param permissionGroup 权限组名
     * @return 权限列表
     */
    public List<String> getPermissionsInGroup(String permissionGroup) {
        List<String> permissions = new ArrayList<>();
        
        try {
            // 根据权限组返回对应的权限列表
            switch (permissionGroup) {
                case "android.permission-group.STORAGE":
                    permissions.add("android.permission.READ_EXTERNAL_STORAGE");
                    permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
                    break;
                    
                case "android.permission-group.CAMERA":
                    permissions.add("android.permission.CAMERA");
                    break;
                    
                case "android.permission-group.MICROPHONE":
                    permissions.add("android.permission.RECORD_AUDIO");
                    break;
                    
                case "android.permission-group.LOCATION":
                    permissions.add("android.permission.ACCESS_FINE_LOCATION");
                    permissions.add("android.permission.ACCESS_COARSE_LOCATION");
                    break;
                    
                case "android.permission-group.CONTACTS":
                    permissions.add("android.permission.READ_CONTACTS");
                    permissions.add("android.permission.WRITE_CONTACTS");
                    break;
                    
                case "android.permission-group.PHONE":
                    permissions.add("android.permission.READ_PHONE_STATE");
                    permissions.add("android.permission.CALL_PHONE");
                    permissions.add("android.permission.READ_CALL_LOG");
                    permissions.add("android.permission.WRITE_CALL_LOG");
                    break;
                    
                case "android.permission-group.SMS":
                    permissions.add("android.permission.SEND_SMS");
                    permissions.add("android.permission.READ_SMS");
                    permissions.add("android.permission.RECEIVE_SMS");
                    break;
                    
                case "android.permission-group.CALENDAR":
                    permissions.add("android.permission.READ_CALENDAR");
                    permissions.add("android.permission.WRITE_CALENDAR");
                    break;
                    
                default:
                    // 未知权限组，返回空列表
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get permissions in group", e);
        }
        
        return permissions;
    }
    
    /**
     * 创建默认权限策略
     * @param policyType 策略类型
     * @return 权限策略
     */
    public PermissionPolicy createDefaultPolicy(int policyType) {
        PermissionPolicy policy = new PermissionPolicy(policyType);
        
        switch (policyType) {
            case POLICY_WHITELIST:
                // 默认白名单包含基本权限
                policy.whitelist.add("android.permission.INTERNET");
                policy.whitelist.add("android.permission.ACCESS_NETWORK_STATE");
                policy.whitelist.add("android.permission.ACCESS_WIFI_STATE");
                break;
                
            case POLICY_BLACKLIST:
                // 默认黑名单包含敏感权限
                policy.blacklist.add("android.permission.READ_CONTACTS");
                policy.blacklist.add("android.permission.WRITE_CONTACTS");
                policy.blacklist.add("android.permission.READ_PHONE_STATE");
                policy.blacklist.add("android.permission.CALL_PHONE");
                policy.blacklist.add("android.permission.SEND_SMS");
                policy.blacklist.add("android.permission.READ_SMS");
                break;
        }
        
        return policy;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized.get()) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up PermissionControlManager...");
            
            mPermissionPolicies.clear();
            mPermissionStates.clear();
            
            mIsInitialized.set(false);
            Log.d(TAG, "PermissionControlManager cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup PermissionControlManager", e);
        }
    }
    
    /**
     * 权限策略类
     */
    public static class PermissionPolicy {
        public int policyType;
        public List<String> whitelist;
        public List<String> blacklist;
        public boolean autoGrantBasic;
        public boolean askForSensitive;
        public boolean logPermissionRequests;
        
        public PermissionPolicy() {
            this.policyType = POLICY_ASK_USER;
            this.whitelist = new ArrayList<>();
            this.blacklist = new ArrayList<>();
            this.autoGrantBasic = true;
            this.askForSensitive = true;
            this.logPermissionRequests = true;
        }
        
        public PermissionPolicy(int policyType) {
            this();
            this.policyType = policyType;
        }
    }
} 