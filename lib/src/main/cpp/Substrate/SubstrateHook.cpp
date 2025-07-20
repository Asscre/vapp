#include "SubstrateHook.h"
#include "../utils/LogUtils.h"
#include <android/log.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

#define TAG "SubstrateHook"

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

bool SubstrateHook::initialize() {
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

void SubstrateHook::cleanup() {
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

bool SubstrateHook::hookMethod(void* targetMethod, void* hookMethod, void* backupMethod) {
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
            // 注册Hook信息
            HookInfo hookInfo;
            hookInfo.targetMethod = targetMethod;
            hookInfo.hookMethod = hookMethod;
            hookInfo.backupMethod = backupMethod;
            hookInfo.architecture = arch;
            hookInfo.hookTime = getCurrentTime();
            
            mHookManager[targetMethod] = hookInfo;
            
            LOGD(TAG, "Method hooked successfully");
        }
        
        return success;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethod(void* targetMethod) {
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

void* SubstrateHook::callOriginMethod(void* backupMethod, void* receiver, void* args) {
    if (!mIsInitialized) {
        LOGE(TAG, "SubstrateHook not initialized");
        return nullptr;
    }
    
    try {
        // 调用备份方法
        if (backupMethod != nullptr) {
            // 这里需要根据具体的调用约定来调用备份方法
            // 暂时返回nullptr作为占位符
            return nullptr;
        }
        
        return nullptr;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception calling origin method: %s", e.what());
        return nullptr;
    }
}

bool SubstrateHook::isMethodHooked(void* targetMethod) {
    if (!mIsInitialized) {
        return false;
    }
    
    return mHookManager.find(targetMethod) != mHookManager.end();
}

void SubstrateHook::cleanupAllHooks() {
    try {
        LOGD(TAG, "Cleaning up all hooks...");
        
        for (auto& pair : mHookManager) {
            unhookMethod(pair.first);
        }
        
        mHookManager.clear();
        LOGD(TAG, "All hooks cleaned up");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception cleaning up all hooks: %s", e.what());
    }
}

bool SubstrateHook::initializeARMHook() {
    try {
        LOGD(TAG, "Initializing ARM Hook...");
        
        // TODO: 实现ARM架构的Hook初始化
        // - 设置ARM指令集
        // - 初始化ARM特定的Hook机制
        
        LOGD(TAG, "ARM Hook initialized");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception initializing ARM Hook: %s", e.what());
        return false;
    }
}

bool SubstrateHook::initializeARM64Hook() {
    try {
        LOGD(TAG, "Initializing ARM64 Hook...");
        
        // TODO: 实现ARM64架构的Hook初始化
        // - 设置ARM64指令集
        // - 初始化ARM64特定的Hook机制
        
        LOGD(TAG, "ARM64 Hook initialized");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception initializing ARM64 Hook: %s", e.what());
        return false;
    }
}

void SubstrateHook::cleanupARMHook() {
    try {
        LOGD(TAG, "Cleaning up ARM Hook...");
        
        // TODO: 清理ARM Hook资源
        
        LOGD(TAG, "ARM Hook cleaned up");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception cleaning up ARM Hook: %s", e.what());
    }
}

void SubstrateHook::cleanupARM64Hook() {
    try {
        LOGD(TAG, "Cleaning up ARM64 Hook...");
        
        // TODO: 清理ARM64 Hook资源
        
        LOGD(TAG, "ARM64 Hook cleaned up");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception cleaning up ARM64 Hook: %s", e.what());
    }
}

bool SubstrateHook::hookMethodARM(void* targetMethod, void* hookMethod, void* backupMethod) {
    try {
        LOGD(TAG, "Hooking ARM method: %p -> %p", targetMethod, hookMethod);
        
        // TODO: 实现ARM架构的方法Hook
        // - 保存原始指令
        // - 写入跳转指令
        // - 设置备份方法
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking ARM method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::hookMethodARM64(void* targetMethod, void* hookMethod, void* backupMethod) {
    try {
        LOGD(TAG, "Hooking ARM64 method: %p -> %p", targetMethod, hookMethod);
        
        // TODO: 实现ARM64架构的方法Hook
        // - 保存原始指令
        // - 写入跳转指令
        // - 设置备份方法
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking ARM64 method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::hookMethodX86(void* targetMethod, void* hookMethod, void* backupMethod) {
    try {
        LOGD(TAG, "Hooking X86 method: %p -> %p", targetMethod, hookMethod);
        
        // TODO: 实现X86架构的方法Hook
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking X86 method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::hookMethodX86_64(void* targetMethod, void* hookMethod, void* backupMethod) {
    try {
        LOGD(TAG, "Hooking X86_64 method: %p -> %p", targetMethod, hookMethod);
        
        // TODO: 实现X86_64架构的方法Hook
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking X86_64 method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethodARM(void* targetMethod, const HookInfo& hookInfo) {
    try {
        LOGD(TAG, "Unhooking ARM method: %p", targetMethod);
        
        // TODO: 实现ARM架构的方法取消Hook
        // - 恢复原始指令
        // - 清理跳转指令
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception unhooking ARM method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethodARM64(void* targetMethod, const HookInfo& hookInfo) {
    try {
        LOGD(TAG, "Unhooking ARM64 method: %p", targetMethod);
        
        // TODO: 实现ARM64架构的方法取消Hook
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception unhooking ARM64 method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethodX86(void* targetMethod, const HookInfo& hookInfo) {
    try {
        LOGD(TAG, "Unhooking X86 method: %p", targetMethod);
        
        // TODO: 实现X86架构的方法取消Hook
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception unhooking X86 method: %s", e.what());
        return false;
    }
}

bool SubstrateHook::unhookMethodX86_64(void* targetMethod, const HookInfo& hookInfo) {
    try {
        LOGD(TAG, "Unhooking X86_64 method: %p", targetMethod);
        
        // TODO: 实现X86_64架构的方法取消Hook
        
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception unhooking X86_64 method: %s", e.what());
        return false;
    }
}

SubstrateHook::Architecture SubstrateHook::getArchitecture() {
    // 检测当前架构
    #if defined(__arm__)
        return ARCH_ARM;
    #elif defined(__aarch64__)
        return ARCH_ARM64;
    #elif defined(__i386__)
        return ARCH_X86;
    #elif defined(__x86_64__)
        return ARCH_X86_64;
    #else
        return ARCH_UNKNOWN;
    #endif
}

long SubstrateHook::getCurrentTime() {
    // 获取当前时间戳（毫秒）
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000 + ts.tv_nsec / 1000000;
}

// JNI接口函数
extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeInitialize(JNIEnv* env, jobject thiz) {
    return SubstrateHook::getInstance()->initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lody_virtual_SubstrateHook_nativeCleanup(JNIEnv* env, jobject thiz) {
    SubstrateHook::getInstance()->cleanup();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeHookMethod(JNIEnv* env, jobject thiz, 
                                                    jobject targetMethod, jobject hookMethod, jobject backupMethod) {
    // 获取方法地址
    void* targetAddr = nullptr;
    void* hookAddr = nullptr;
    void* backupAddr = nullptr;
    
    // TODO: 从Java Method对象获取Native地址
    
    return SubstrateHook::getInstance()->hookMethod(targetAddr, hookAddr, backupAddr);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_SubstrateHook_nativeUnhookMethod(JNIEnv* env, jobject thiz, jobject targetMethod) {
    // 获取方法地址
    void* targetAddr = nullptr;
    
    // TODO: 从Java Method对象获取Native地址
    
    return SubstrateHook::getInstance()->unhookMethod(targetAddr);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_lody_virtual_SubstrateHook_nativeCallOriginMethod(JNIEnv* env, jobject thiz, 
                                                          jobject backupMethod, jobject receiver, jobjectArray args) {
    // 获取方法地址
    void* backupAddr = nullptr;
    void* receiverAddr = nullptr;
    void* argsAddr = nullptr;
    
    // TODO: 从Java对象获取Native地址
    
    void* result = SubstrateHook::getInstance()->callOriginMethod(backupAddr, receiverAddr, argsAddr);
    
    // TODO: 将Native结果转换为Java对象
    
    return nullptr;
}

} // namespace VirtualSpace 