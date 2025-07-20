#include "ProcessManager.h"
#include "../utils/LogUtils.h"
#include <jni.h>
#include <string>

#define TAG "ProcessManager"

namespace VirtualSpace {

ProcessManager* ProcessManager::sInstance = nullptr;
std::mutex ProcessManager::sMutex;

ProcessManager::ProcessManager() : mIsInitialized(false) {
    LOGD(TAG, "ProcessManager constructor");
}

ProcessManager::~ProcessManager() {
    LOGD(TAG, "ProcessManager destructor");
    cleanupImpl();
}

ProcessManager* ProcessManager::getInstance() {
    if (sInstance == nullptr) {
        std::lock_guard<std::mutex> lock(sMutex);
        if (sInstance == nullptr) {
            sInstance = new ProcessManager();
        }
    }
    return sInstance;
}

// 静态方法实现 - 委托给实例方法
bool ProcessManager::initialize() {
    return getInstance()->initializeImpl();
}

void ProcessManager::cleanup() {
    if (sInstance) {
        sInstance->cleanupImpl();
    }
}

// 实例方法实现
bool ProcessManager::initializeImpl() {
    if (mIsInitialized) {
        LOGW(TAG, "ProcessManager already initialized");
        return true;
    }
    
    try {
        LOGD(TAG, "Initializing ProcessManager...");
        
        // TODO: 初始化进程管理功能
        
        mIsInitialized = true;
        LOGD(TAG, "ProcessManager initialized successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during ProcessManager initialization: %s", e.what());
        return false;
    }
}

void ProcessManager::cleanupImpl() {
    if (!mIsInitialized) {
        return;
    }
    
    try {
        LOGD(TAG, "Cleaning up ProcessManager...");
        
        // TODO: 清理进程管理资源
        
        mIsInitialized = false;
        LOGD(TAG, "ProcessManager cleanup completed");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during ProcessManager cleanup: %s", e.what());
    }
}

} // namespace VirtualSpace

// JNI接口函数
extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_ProcessManager_nativeInitialize(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    return VirtualSpace::ProcessManager::initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lody_virtual_ProcessManager_nativeCleanup(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    VirtualSpace::ProcessManager::cleanup();
} 