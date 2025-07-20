package com.lody.virtual;

import android.content.Context;
import android.util.Log;

import com.lody.virtual.VirtualCore;
import com.lody.virtual.VPackageManager;
import com.lody.virtual.VEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * 虚拟空间基本功能测试
 * 测试核心组件的初始化
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicTest {
    
    private static final String TAG = "BasicTest";
    
    @Mock
    private Context mContext;
    
    private VirtualCore mVirtualCore;
    private VPackageManager mPackageManager;
    private VEnvironment mEnvironment;
    
    @Before
    public void setUp() throws Exception {
        Log.d(TAG, "Setting up BasicTest...");
        
        mVirtualCore = VirtualCore.getInstance();
        mPackageManager = VPackageManager.getInstance();
        mEnvironment = new VEnvironment(mContext);
        
        Log.d(TAG, "BasicTest setup completed");
    }
    
    /**
     * 测试VirtualCore单例模式
     */
    @Test
    public void testVirtualCoreSingleton() {
        Log.d(TAG, "Testing VirtualCore singleton...");
        
        VirtualCore instance1 = VirtualCore.getInstance();
        VirtualCore instance2 = VirtualCore.getInstance();
        
        assertNotNull("VirtualCore instance should not be null", instance1);
        assertSame("VirtualCore should be singleton", instance1, instance2);
        
        Log.d(TAG, "VirtualCore singleton test passed");
    }
    
    /**
     * 测试VPackageManager单例模式
     */
    @Test
    public void testVPackageManagerSingleton() {
        Log.d(TAG, "Testing VPackageManager singleton...");
        
        VPackageManager instance1 = VPackageManager.getInstance();
        VPackageManager instance2 = VPackageManager.getInstance();
        
        assertNotNull("VPackageManager instance should not be null", instance1);
        assertSame("VPackageManager should be singleton", instance1, instance2);
        
        Log.d(TAG, "VPackageManager singleton test passed");
    }
    
    /**
     * 测试VEnvironment创建
     */
    @Test
    public void testVEnvironmentCreation() {
        Log.d(TAG, "Testing VEnvironment creation...");
        
        VEnvironment env = new VEnvironment(mContext);
        
        assertNotNull("VEnvironment should not be null", env);
        
        Log.d(TAG, "VEnvironment creation test passed");
    }
    
    /**
     * 测试基本方法调用
     */
    @Test
    public void testBasicMethodCalls() {
        Log.d(TAG, "Testing basic method calls...");
        
        try {
            // 测试VirtualCore方法
            assertFalse("VirtualCore should not be initialized initially", mVirtualCore.isInitialized());
            
            // 测试包管理器方法
            assertNotNull("Package manager should not be null", mPackageManager);
            
            // 测试环境管理器方法
            assertNotNull("Environment should not be null", mEnvironment);
            
            Log.d(TAG, "Basic method calls test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Basic method calls test failed", e);
            fail("Basic method calls should not throw exception: " + e.getMessage());
        }
    }
} 