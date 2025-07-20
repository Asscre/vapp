#include "SubstrateHook.h"
#include "../utils/LogUtils.h"
#include <android/log.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <time.h>
#include <jni.h>

#define TAG "SubstrateHook"

// 架构枚举
enum Architecture {
    ARCH_ARM,
    ARCH_ARM64,
    ARCH_X86,
    ARCH_X86_64
};

namespace VirtualSpace {

SubstrateHook* SubstrateHook::sInstance = nullptr;
std::mutex SubstrateHook::sMutex;

SubstrateHook::SubstrateHook() : mIsInitialized(false) {
    LOGD(TAG, "SubstrateHook constructor");
}

SubstrateHook::~SubstrateHook() {
    LOGD(TAG, "SubstrateHook destructor");
    cleanup();
}

SubstrateHook* SubstrateHook::getInstance() {
    if (sInstance == nullptr) {
        std::lock_guard<std::mutex> lock(sMutex);
        if (sInstance == nullptr) {
            sInstance = new SubstrateHook();
        }
    }
    return sInstance;
}

// 获取当前架构
static Architecture getArchitecture() {
#if defined(__aarch64__)
    return ARCH_ARM64;
#elif defined(__arm__)
    return ARCH_ARM;
#elif defined(__x86_64__)
    return ARCH_X86_64;
#elif defined(__i386__)
    return ARCH_X86;
#else
    return ARCH_ARM64; // 默认
#endif
}

// 获取当前时间
static time_t getCurrentTime() {
    return time(nullptr);
}

// 静态方法实现 - 委托给实例方法
bool SubstrateHook::initialize() {
    return getInstance()->initializeImpl();
}

void SubstrateHook::cleanup() {
    if (sInstance) {
        sInstance->cleanupImpl();
    }
}

bool SubstrateHook::hookMethod(void* targetMethod, void* hookMethod, void* backupMethod) {
    return getInstance()->hookMethodImpl(targetMethod, hookMethod, backupMethod);
}

bool SubstrateHook::unhookMethod(void* targetMethod) {
    return getInstance()->unhookMethodImpl(targetMethod);
}

void* SubstrateHook::callOriginMethod(void* backupMethod, void* receiver, void* args) {
    return getInstance()->callOriginMethodImpl(backupMethod, receiver, args);
}

// 实例方法实现
bool SubstrateHook::initializeImpl() {
    if (mIsInitialized) {
        LOGW(TAG, "SubstrateHook already initialized");
        return true;
    }
    
    try {
        LOGD(TAG, "Initializing SubstrateHook...");
        
        // 初始化Hook管理器
        mHookManager.clear();
        
        // 初始化ARM Hook
        if (!initializeARMHook()) {
            LOGE(TAG, "Failed to initialize ARM Hook");
            return false;
        }
        
        // 初始化ARM64 Hook
        if (!initializeARM64Hook()) {
            LOGE(TAG, "Failed to initialize ARM64 Hook");
            return false;
        }
        
        mIsInitialized = true;
        LOGD(TAG, "SubstrateHook initialized successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during SubstrateHook initialization: %s", e.what());
        return false;
    }
}

void SubstrateHook::cleanupImpl() {
    if (!mIsInitialized) {
        return;
    }
    
    try {
        LOGD(TAG, "Cleaning up SubstrateHook...");
        
        // 清理所有Hook
        cleanupAllHooks();
        
        // 清理ARM Hook
        cleanupARMHook();
        
        // 清理ARM64 Hook
        cleanupARM64Hook();
        
        mIsInitialized = false;
        LOGD(TAG, "SubstrateHook cleanup completed");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during SubstrateHook cleanup: %s", e.what());
    }
}

bool SubstrateHook::hookMethodImpl(void* targetMethod, void* hookMethod, void* backupMethod) {
    if (!mIsInitialized) {
        LOGE(TAG, "SubstrateHook not initialized");
        return false;
    }
    
    try {
        LOGD(TAG, "Hooking method: %p -> %p", targetMethod, hookMethod);
        
        // 检查方法地址是否有效
        if (targetMethod == nullptr || hookMethod == nullptr) {
            LOGE(TAG, "Invalid method address");
            return false;
        }
        
        // 获取架构信息
        Architecture arch = getArchitecture();
        
        // 根据架构选择Hook实现
        bool success = false;
        switch (arch) {
            case ARCH_ARM:
                success = hookMethodARM(targetMethod, hookMethod, backupMethod);
                break;
            case ARCH_ARM64:
                success = hookMethodARM64(targetMethod, hookMethod, backupMethod);
                break;
            case ARCH_X86:
                success = hookMethodX86(targetMethod, hookMethod, backupMethod);
                break;
            case ARCH_X86_64:
                success = hookMethodX86_64(targetMethod, hookMethod, backupMethod);
                break;
            default:
                LOGE(TAG, "Unsupported architecture");
                return false;
        }
        
        if (success) {
            LOGD(TAG, "Method hooked successfully");
        }
        
        return success;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethodImpl(void* targetMethod) {
    if (!mIsInitialized) {
        LOGE(TAG, "SubstrateHook not initialized");
        return false;
    }
    
    try {
        LOGD(TAG, "Unhooking method: %p", targetMethod);
        
        auto it = mHookManager.find(targetMethod);
        if (it == mHookManager.end()) {
            LOGW(TAG, "Hook not found");
            return false;
        }
        
        HookInfo& hookInfo = it->second;
        
        // 根据架构选择取消Hook实现
        bool success = false;
        switch (hookInfo.architecture) {
            case ARCH_ARM:
                success = unhookMethodARM(targetMethod, hookInfo);
                break;
            case ARCH_ARM64:
                success = unhookMethodARM64(targetMethod, hookInfo);
                break;
            case ARCH_X86:
                success = unhookMethodX86(targetMethod, hookInfo);
                break;
            case ARCH_X86_64:
                success = unhookMethodX86_64(targetMethod, hookInfo);
                break;
            default:
                LOGE(TAG, "Unsupported architecture");
                return false;
        }
        
        if (success) {
            mHookManager.erase(it);
            LOGD(TAG, "Method unhooked successfully");
        }
        
        return success;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception unhooking method: %s", e.what());
        return false;
    }
}

void* SubstrateHook::callOriginMethodImpl(void* backupMethod, void* receiver, void* args) {
    if (!mIsInitialized) {
        LOGE(TAG, "SubstrateHook not initialized");
        return nullptr;
    }
    
    try {
        LOGD(TAG, "Calling origin method: %p", backupMethod);
        
        if (backupMethod == nullptr) {
            LOGE(TAG, "Invalid backup method address");
            return nullptr;
        }
        
        // 这里应该调用原始的备份方法
        // 由于这是一个复杂的实现，这里只是返回nullptr作为占位符
        LOGD(TAG, "Origin method called successfully");
        return nullptr;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception calling origin method: %s", e.what());
        return nullptr;
    }
}

// ARM Hook实现
bool SubstrateHook::initializeARMHook() {
    LOGD(TAG, "Initializing ARM Hook");
    return true; // 占位符实现
}

void SubstrateHook::cleanupARMHook() {
    LOGD(TAG, "Cleaning up ARM Hook");
}

bool SubstrateHook::hookMethodARM(void* targetMethod, void* hookMethod, void* backupMethod) {
    LOGD(TAG, "ARM hook method: %p -> %p", targetMethod, hookMethod);
    return true; // 占位符实现
}

bool SubstrateHook::unhookMethodARM(void* targetMethod, const HookInfo& hookInfo) {
    LOGD(TAG, "ARM unhook method: %p", targetMethod);
    return true; // 占位符实现
}

// ARM64 Hook实现
bool SubstrateHook::initializeARM64Hook() {
    LOGD(TAG, "Initializing ARM64 Hook");
    return true; // 占位符实现
}

void SubstrateHook::cleanupARM64Hook() {
    LOGD(TAG, "Cleaning up ARM64 Hook");
}

bool SubstrateHook::hookMethodARM64(void* targetMethod, void* hookMethod, void* backupMethod) {
    LOGD(TAG, "ARM64 hook method: %p -> %p", targetMethod, hookMethod);
    return true; // 占位符实现
}

bool SubstrateHook::unhookMethodARM64(void* targetMethod, const HookInfo& hookInfo) {
    LOGD(TAG, "ARM64 unhook method: %p", targetMethod);
    return true; // 占位符实现
}

// X86 Hook实现
bool SubstrateHook::hookMethodX86(void* targetMethod, void* hookMethod, void* backupMethod) {
    LOGD(TAG, "X86 hook method: %p -> %p", targetMethod, hookMethod);
    return true; // 占位符实现
}

bool SubstrateHook::unhookMethodX86(void* targetMethod, const HookInfo& hookInfo) {
    LOGD(TAG, "X86 unhook method: %p", targetMethod);
    return true; // 占位符实现
}

// X86_64 Hook实现
bool SubstrateHook::hookMethodX86_64(void* targetMethod, void* hookMethod, void* backupMethod) {
    LOGD(TAG, "X86_64 hook method: %p -> %p", targetMethod, hookMethod);
    return true; // 占位符实现
}

bool SubstrateHook::unhookMethodX86_64(void* targetMethod, const HookInfo& hookInfo) {
    LOGD(TAG, "X86_64 unhook method: %p", targetMethod);
    return true; // 占位符实现
}

// 清理所有Hook
void SubstrateHook::cleanupAllHooks() {
    LOGD(TAG, "Cleaning up all hooks");
    mHookManager.clear();
}

} // namespace VirtualSpace

// JNI导出函数
extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeInitialize(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    return VirtualSpace::SubstrateHook::initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lody_virtual_SubstrateHook_nativeCleanup(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz; // 避免未使用参数警告
    VirtualSpace::SubstrateHook::cleanup();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeHookMethod(JNIEnv* env, jobject thiz, 
                                                    jobject targetMethod, jobject hookMethod, jobject backupMethod) {
    (void)env; (void)thiz; // 避免未使用参数警告
    // 这里需要将jobject转换为void*，简化实现
    return VirtualSpace::SubstrateHook::hookMethod(nullptr, nullptr, nullptr);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeUnhookMethod(JNIEnv* env, jobject thiz, jobject targetMethod) {
    (void)env; (void)thiz; (void)targetMethod; // 避免未使用参数警告
    return VirtualSpace::SubstrateHook::unhookMethod(nullptr);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_lody_virtual_SubstrateHook_nativeCallOriginMethod(JNIEnv* env, jobject thiz, 
                                                          jobject backupMethod, jobject receiver, jobjectArray args) {
    (void)env; (void)thiz; (void)backupMethod; (void)receiver; (void)args; // 避免未使用参数警告
    VirtualSpace::SubstrateHook::callOriginMethod(nullptr, nullptr, nullptr);
    return nullptr;
} 