package com.lody.virtual.hooks;

import android.os.Build;
import android.util.Log;
import com.lody.virtual.HookWrapper;
import com.lody.virtual.VirtualCore;

/**
 * 设备信息Hook类
 * 实现设备信息虚拟化
 */
public class DeviceInfoHooks {
    
    private static final String TAG = "DeviceInfoHooks";
    
    /**
     * Hook Build.MODEL
     * 虚拟化设备型号
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getModel",
        priority = 100
    )
    public static String hookGetModel() {
        try {
            Log.d(TAG, "Hook Build.getModel()");
            
            // 返回虚拟设备型号
            String virtualModel = VirtualCore.getInstance().getVirtualDeviceModel();
            if (virtualModel != null && !virtualModel.isEmpty()) {
                Log.d(TAG, "Returning virtual device model: " + virtualModel);
                return virtualModel;
            }
            
            // 如果没有虚拟型号，调用原始方法
            Log.d(TAG, "Calling original getModel()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getModel hook", e);
        }
        
        return Build.MODEL;
    }
    
    /**
     * Hook Build.MANUFACTURER
     * 虚拟化设备制造商
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getManufacturer",
        priority = 100
    )
    public static String hookGetManufacturer() {
        try {
            Log.d(TAG, "Hook Build.getManufacturer()");
            
            // 返回虚拟设备制造商
            String virtualManufacturer = VirtualCore.getInstance().getVirtualDeviceManufacturer();
            if (virtualManufacturer != null && !virtualManufacturer.isEmpty()) {
                Log.d(TAG, "Returning virtual device manufacturer: " + virtualManufacturer);
                return virtualManufacturer;
            }
            
            // 如果没有虚拟制造商，调用原始方法
            Log.d(TAG, "Calling original getManufacturer()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getManufacturer hook", e);
        }
        
        return Build.MANUFACTURER;
    }
    
    /**
     * Hook Build.BRAND
     * 虚拟化设备品牌
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getBrand",
        priority = 100
    )
    public static String hookGetBrand() {
        try {
            Log.d(TAG, "Hook Build.getBrand()");
            
            // 返回虚拟设备品牌
            String virtualBrand = VirtualCore.getInstance().getVirtualDeviceBrand();
            if (virtualBrand != null && !virtualBrand.isEmpty()) {
                Log.d(TAG, "Returning virtual device brand: " + virtualBrand);
                return virtualBrand;
            }
            
            // 如果没有虚拟品牌，调用原始方法
            Log.d(TAG, "Calling original getBrand()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getBrand hook", e);
        }
        
        return Build.BRAND;
    }
    
    /**
     * Hook Build.PRODUCT
     * 虚拟化设备产品名
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getProduct",
        priority = 100
    )
    public static String hookGetProduct() {
        try {
            Log.d(TAG, "Hook Build.getProduct()");
            
            // 返回虚拟设备产品名
            String virtualProduct = VirtualCore.getInstance().getVirtualDeviceProduct();
            if (virtualProduct != null && !virtualProduct.isEmpty()) {
                Log.d(TAG, "Returning virtual device product: " + virtualProduct);
                return virtualProduct;
            }
            
            // 如果没有虚拟产品名，调用原始方法
            Log.d(TAG, "Calling original getProduct()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getProduct hook", e);
        }
        
        return Build.PRODUCT;
    }
    
    /**
     * Hook Build.DEVICE
     * 虚拟化设备名
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getDevice",
        priority = 100
    )
    public static String hookGetDevice() {
        try {
            Log.d(TAG, "Hook Build.getDevice()");
            
            // 返回虚拟设备名
            String virtualDevice = VirtualCore.getInstance().getVirtualDeviceName();
            if (virtualDevice != null && !virtualDevice.isEmpty()) {
                Log.d(TAG, "Returning virtual device name: " + virtualDevice);
                return virtualDevice;
            }
            
            // 如果没有虚拟设备名，调用原始方法
            Log.d(TAG, "Calling original getDevice()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getDevice hook", e);
        }
        
        return Build.DEVICE;
    }
    
    /**
     * Hook Build.FINGERPRINT
     * 虚拟化设备指纹
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getFingerprint",
        priority = 100
    )
    public static String hookGetFingerprint() {
        try {
            Log.d(TAG, "Hook Build.getFingerprint()");
            
            // 返回虚拟设备指纹
            String virtualFingerprint = VirtualCore.getInstance().getVirtualDeviceFingerprint();
            if (virtualFingerprint != null && !virtualFingerprint.isEmpty()) {
                Log.d(TAG, "Returning virtual device fingerprint: " + virtualFingerprint);
                return virtualFingerprint;
            }
            
            // 如果没有虚拟指纹，调用原始方法
            Log.d(TAG, "Calling original getFingerprint()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getFingerprint hook", e);
        }
        
        return Build.FINGERPRINT;
    }
    
    /**
     * Hook Build.SERIAL
     * 虚拟化设备序列号
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getSerial",
        priority = 100
    )
    public static String hookGetSerial() {
        try {
            Log.d(TAG, "Hook Build.getSerial()");
            
            // 返回虚拟设备序列号
            String virtualSerial = VirtualCore.getInstance().getVirtualDeviceSerial();
            if (virtualSerial != null && !virtualSerial.isEmpty()) {
                Log.d(TAG, "Returning virtual device serial: " + virtualSerial);
                return virtualSerial;
            }
            
            // 如果没有虚拟序列号，调用原始方法
            Log.d(TAG, "Calling original getSerial()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getSerial hook", e);
        }
        
        return Build.SERIAL;
    }
    
    /**
     * Hook Build.HARDWARE
     * 虚拟化硬件信息
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getHardware",
        priority = 100
    )
    public static String hookGetHardware() {
        try {
            Log.d(TAG, "Hook Build.getHardware()");
            
            // 返回虚拟硬件信息
            String virtualHardware = VirtualCore.getInstance().getVirtualDeviceHardware();
            if (virtualHardware != null && !virtualHardware.isEmpty()) {
                Log.d(TAG, "Returning virtual device hardware: " + virtualHardware);
                return virtualHardware;
            }
            
            // 如果没有虚拟硬件信息，调用原始方法
            Log.d(TAG, "Calling original getHardware()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getHardware hook", e);
        }
        
        return Build.HARDWARE;
    }
    
    /**
     * Hook Build.HOST
     * 虚拟化构建主机
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getHost",
        priority = 100
    )
    public static String hookGetHost() {
        try {
            Log.d(TAG, "Hook Build.getHost()");
            
            // 返回虚拟构建主机
            String virtualHost = VirtualCore.getInstance().getVirtualBuildHost();
            if (virtualHost != null && !virtualHost.isEmpty()) {
                Log.d(TAG, "Returning virtual build host: " + virtualHost);
                return virtualHost;
            }
            
            // 如果没有虚拟构建主机，调用原始方法
            Log.d(TAG, "Calling original getHost()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getHost hook", e);
        }
        
        return Build.HOST;
    }
    
    /**
     * Hook Build.TAGS
     * 虚拟化构建标签
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getTags",
        priority = 100
    )
    public static String hookGetTags() {
        try {
            Log.d(TAG, "Hook Build.getTags()");
            
            // 返回虚拟构建标签
            String virtualTags = VirtualCore.getInstance().getVirtualBuildTags();
            if (virtualTags != null && !virtualTags.isEmpty()) {
                Log.d(TAG, "Returning virtual build tags: " + virtualTags);
                return virtualTags;
            }
            
            // 如果没有虚拟构建标签，调用原始方法
            Log.d(TAG, "Calling original getTags()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getTags hook", e);
        }
        
        return Build.TAGS;
    }
    
    /**
     * Hook Build.TYPE
     * 虚拟化构建类型
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getType",
        priority = 100
    )
    public static String hookGetType() {
        try {
            Log.d(TAG, "Hook Build.getType()");
            
            // 返回虚拟构建类型
            String virtualType = VirtualCore.getInstance().getVirtualBuildType();
            if (virtualType != null && !virtualType.isEmpty()) {
                Log.d(TAG, "Returning virtual build type: " + virtualType);
                return virtualType;
            }
            
            // 如果没有虚拟构建类型，调用原始方法
            Log.d(TAG, "Calling original getType()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getType hook", e);
        }
        
        return Build.TYPE;
    }
    
    /**
     * Hook Build.USER
     * 虚拟化构建用户
     */
    @HookWrapper.Hook(
        targetClass = "android.os.Build",
        targetMethod = "getUser",
        priority = 100
    )
    public static String hookGetUser() {
        try {
            Log.d(TAG, "Hook Build.getUser()");
            
            // 返回虚拟构建用户
            String virtualUser = VirtualCore.getInstance().getVirtualBuildUser();
            if (virtualUser != null && !virtualUser.isEmpty()) {
                Log.d(TAG, "Returning virtual build user: " + virtualUser);
                return virtualUser;
            }
            
            // 如果没有虚拟构建用户，调用原始方法
            Log.d(TAG, "Calling original getUser()");
            // 这里需要调用原始方法
            
        } catch (Exception e) {
            Log.e(TAG, "Exception in getUser hook", e);
        }
        
        return Build.USER;
    }
} 