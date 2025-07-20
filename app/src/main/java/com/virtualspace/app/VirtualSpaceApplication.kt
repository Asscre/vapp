package com.virtualspace.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.lody.virtual.VirtualCore

/**
 * 虚拟空间应用主类
 * 负责应用初始化、虚拟化核心启动等
 */
class VirtualSpaceApplication : Application() {
    
    companion object {
        private const val TAG = "VirtualSpaceApplication"
        
        @JvmStatic
        fun getInstance(): VirtualSpaceApplication {
            return instance
        }
        
        private lateinit var instance: VirtualSpaceApplication
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "VirtualSpaceApplication onCreate")
        
        instance = this
        
        try {
            // 初始化虚拟化核心
            initializeVirtualCore()
            
            // 初始化应用组件
            initializeAppComponents()
            
            Log.d(TAG, "VirtualSpaceApplication initialization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize VirtualSpaceApplication", e)
        }
    }
    
    /**
     * 初始化虚拟化核心
     */
    private fun initializeVirtualCore() {
        try {
            Log.d(TAG, "Initializing VirtualCore...")
            
            val virtualCore = VirtualCore.getInstance()
            val initResult = virtualCore.initialize(this)
            
            if (initResult) {
                Log.d(TAG, "VirtualCore initialized successfully")
            } else {
                Log.e(TAG, "Failed to initialize VirtualCore")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing VirtualCore", e)
        }
    }
    
    /**
     * 初始化应用组件
     */
    private fun initializeAppComponents() {
        try {
            Log.d(TAG, "Initializing app components...")
            
            // TODO: 初始化其他应用组件
            // - 数据库
            // - 网络配置
            // - 缓存管理
            // - 权限管理
            
            Log.d(TAG, "App components initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing app components", e)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        Log.d(TAG, "VirtualSpaceApplication onTerminate")
        
        try {
            // 清理资源
            cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * 清理资源
     */
    private fun cleanup() {
        try {
            Log.d(TAG, "Cleaning up resources...")
            
            // 清理虚拟化核心
            val virtualCore = VirtualCore.getInstance()
            virtualCore.cleanup()
            
            Log.d(TAG, "Resources cleaned up successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        
        Log.d(TAG, "VirtualSpaceApplication attachBaseContext")
        
        try {
            // 在Context附加时进行一些初始化
            // 这里可以添加一些需要在Context创建时就执行的逻辑
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in attachBaseContext", e)
        }
    }
} 