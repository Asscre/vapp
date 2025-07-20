#include "SystemCallHook.h"
#include "../utils/LogUtils.h"
#include <jni.h>

#define TAG "SystemCallHook"

namespace VirtualSpace {

SystemCallHook* SystemCallHook::sInstance = nullptr;
std::mutex SystemCallHook::sMutex;

SystemCallHook::SystemCallHook() : mIsInitialized(false) {
    LOGD(TAG, "SystemCallHook constructor");
}

SystemCallHook::~SystemCallHook() {
    LOGD(TAG, "SystemCallHook destructor");
    cleanupImpl();
}

SystemCallHook* SystemCallHook::getInstance() {
    if (sInstance == nullptr) {
        std::lock_guard<std::mutex> lock(sMutex);
        if (sInstance == nullptr) {
            sInstance = new SystemCallHook();
        }
    }
    return sInstance;
}

// 静态方法实现 - 委托给实例方法
bool SystemCallHook::initialize() {
    return getInstance()->initializeImpl();
}

void SystemCallHook::cleanup() {
    if (sInstance) {
        sInstance->cleanupImpl();
    }
}

// 实例方法实现
bool SystemCallHook::initializeImpl() {
    if (mIsInitialized) {
        LOGW(TAG, "SystemCallHook already initialized");
        return true;
    }
    
    try {
        LOGD(TAG, "Initializing SystemCallHook...");
        
        // TODO: 初始化系统调用Hook功能
        
        mIsInitialized = true;
        LOGD(TAG, "SystemCallHook initialized successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during SystemCallHook initialization: %s", e.what());
        return false;
    }
}

void SystemCallHook::cleanupImpl() {
    if (!mIsInitialized) {
        return;
    }
    
    try {
        LOGD(TAG, "Cleaning up SystemCallHook...");
        
        // TODO: 清理系统调用Hook资源
        
        mIsInitialized = false;
        LOGD(TAG, "SystemCallHook cleanup completed");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during SystemCallHook cleanup: %s", e.what());
    }
}

} // namespace VirtualSpace

// JNI接口函数
extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SystemCallHook_nativeInitialize(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    return VirtualSpace::SystemCallHook::initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lody_virtual_SystemCallHook_nativeCleanup(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    VirtualSpace::SystemCallHook::cleanup();
} 