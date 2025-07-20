#include "IORelocator.h"
#include "../utils/LogUtils.h"
#include "../utils/FileUtils.h"
#include "../utils/StringUtils.h"
#include <android/log.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <errno.h>
#include <string.h>
#include <map>
#include <mutex>

#define TAG "IORelocator"

namespace VirtualSpace {

IORelocator* IORelocator::sInstance = nullptr;
std::mutex IORelocator::sMutex;

IORelocator::IORelocator() : mIsInitialized(false) {
    LOGD(TAG, "IORelocator constructor");
}

IORelocator::~IORelocator() {
    LOGD(TAG, "IORelocator destructor");
    cleanup();
}

IORelocator* IORelocator::getInstance() {
    if (sInstance == nullptr) {
        std::lock_guard<std::mutex> lock(sMutex);
        if (sInstance == nullptr) {
            sInstance = new IORelocator();
        }
    }
    return sInstance;
}

bool IORelocator::initialize() {
    if (mIsInitialized) {
        LOGW(TAG, "IORelocator already initialized");
        return true;
    }
    
    try {
        LOGD(TAG, "Initializing IORelocator...");
        
        // 初始化路径映射
        mPathMappings.clear();
        
        // 初始化系统调用Hook
        if (!initializeSystemCallHooks()) {
            LOGE(TAG, "Failed to initialize system call hooks");
            return false;
        }
        
        mIsInitialized = true;
        LOGD(TAG, "IORelocator initialized successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during IORelocator initialization: %s", e.what());
        return false;
    }
}

void IORelocator::cleanup() {
    if (!mIsInitialized) {
        return;
    }
    
    try {
        LOGD(TAG, "Cleaning up IORelocator...");
        
        // 清理系统调用Hook
        cleanupSystemCallHooks();
        
        // 清理路径映射
        mPathMappings.clear();
        
        mIsInitialized = false;
        LOGD(TAG, "IORelocator cleanup completed");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception during IORelocator cleanup: %s", e.what());
    }
}

bool IORelocator::addPathMapping(const std::string& originalPath, const std::string& virtualPath) {
    if (!mIsInitialized) {
        LOGE(TAG, "IORelocator not initialized");
        return false;
    }
    
    try {
        std::lock_guard<std::mutex> lock(mMutex);
        
        // 规范化路径
        std::string normalizedOriginal = FileUtils::normalizePath(originalPath);
        std::string normalizedVirtual = FileUtils::normalizePath(virtualPath);
        
        // 检查路径是否有效
        if (normalizedOriginal.empty() || normalizedVirtual.empty()) {
            LOGE(TAG, "Invalid path mapping: %s -> %s", originalPath.c_str(), virtualPath.c_str());
            return false;
        }
        
        // 添加映射
        mPathMappings[normalizedOriginal] = normalizedVirtual;
        
        LOGD(TAG, "Added path mapping: %s -> %s", normalizedOriginal.c_str(), normalizedVirtual.c_str());
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception adding path mapping: %s", e.what());
        return false;
    }
}

bool IORelocator::removePathMapping(const std::string& originalPath) {
    if (!mIsInitialized) {
        LOGE(TAG, "IORelocator not initialized");
        return false;
    }
    
    try {
        std::lock_guard<std::mutex> lock(mMutex);
        
        std::string normalizedPath = FileUtils::normalizePath(originalPath);
        
        auto it = mPathMappings.find(normalizedPath);
        if (it != mPathMappings.end()) {
            mPathMappings.erase(it);
            LOGD(TAG, "Removed path mapping: %s", normalizedPath.c_str());
            return true;
        } else {
            LOGW(TAG, "Path mapping not found: %s", normalizedPath.c_str());
            return false;
        }
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception removing path mapping: %s", e.what());
        return false;
    }
}

std::string IORelocator::redirectPath(const std::string& originalPath) {
    if (!mIsInitialized) {
        return originalPath;
    }
    
    try {
        std::lock_guard<std::mutex> lock(mMutex);
        
        std::string normalizedPath = FileUtils::normalizePath(originalPath);
        
        // 查找最长匹配的路径映射
        std::string bestMatch;
        std::string bestVirtualPath;
        size_t bestLength = 0;
        
        for (const auto& mapping : mPathMappings) {
            const std::string& original = mapping.first;
            const std::string& virtual = mapping.second;
            
            // 检查是否是前缀匹配
            if (normalizedPath.find(original) == 0 && original.length() > bestLength) {
                bestMatch = original;
                bestVirtualPath = virtual;
                bestLength = original.length();
            }
        }
        
        if (!bestMatch.empty()) {
            // 替换路径
            std::string redirectedPath = bestVirtualPath + normalizedPath.substr(bestMatch.length());
            LOGD(TAG, "Path redirected: %s -> %s", normalizedPath.c_str(), redirectedPath.c_str());
            return redirectedPath;
        }
        
        // 没有找到映射，返回原始路径
        return originalPath;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception redirecting path: %s", e.what());
        return originalPath;
    }
}

bool IORelocator::initializeSystemCallHooks() {
    try {
        LOGD(TAG, "Initializing system call hooks...");
        
        // 初始化Substrate Hook框架
        if (!SubstrateHook::initialize()) {
            LOGE(TAG, "Failed to initialize Substrate Hook");
            return false;
        }
        
        // Hook文件操作相关的系统调用
        if (!hookFileOperations()) {
            LOGE(TAG, "Failed to hook file operations");
            return false;
        }
        
        // Hook目录操作相关的系统调用
        if (!hookDirectoryOperations()) {
            LOGE(TAG, "Failed to hook directory operations");
            return false;
        }
        
        LOGD(TAG, "System call hooks initialized successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception initializing system call hooks: %s", e.what());
        return false;
    }
}

void IORelocator::cleanupSystemCallHooks() {
    try {
        LOGD(TAG, "Cleaning up system call hooks...");
        
        // 清理Substrate Hook
        SubstrateHook::cleanup();
        
        LOGD(TAG, "System call hooks cleanup completed");
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception cleaning up system call hooks: %s", e.what());
    }
}

bool IORelocator::hookFileOperations() {
    try {
        LOGD(TAG, "Hooking file operations...");
        
        // Hook open系统调用
        if (!hookOpen()) {
            LOGE(TAG, "Failed to hook open");
            return false;
        }
        
        // Hook stat系统调用
        if (!hookStat()) {
            LOGE(TAG, "Failed to hook stat");
            return false;
        }
        
        // Hook access系统调用
        if (!hookAccess()) {
            LOGE(TAG, "Failed to hook access");
            return false;
        }
        
        // Hook unlink系统调用
        if (!hookUnlink()) {
            LOGE(TAG, "Failed to hook unlink");
            return false;
        }
        
        // Hook rename系统调用
        if (!hookRename()) {
            LOGE(TAG, "Failed to hook rename");
            return false;
        }
        
        LOGD(TAG, "File operations hooked successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking file operations: %s", e.what());
        return false;
    }
}

bool IORelocator::hookDirectoryOperations() {
    try {
        LOGD(TAG, "Hooking directory operations...");
        
        // Hook opendir系统调用
        if (!hookOpendir()) {
            LOGE(TAG, "Failed to hook opendir");
            return false;
        }
        
        // Hook mkdir系统调用
        if (!hookMkdir()) {
            LOGE(TAG, "Failed to hook mkdir");
            return false;
        }
        
        // Hook rmdir系统调用
        if (!hookRmdir()) {
            LOGE(TAG, "Failed to hook rmdir");
            return false;
        }
        
        LOGD(TAG, "Directory operations hooked successfully");
        return true;
        
    } catch (const std::exception& e) {
        LOGE(TAG, "Exception hooking directory operations: %s", e.what());
        return false;
    }
}

// Hook实现函数
bool IORelocator::hookOpen() {
    // TODO: 实现open系统调用Hook
    LOGD(TAG, "Hook open system call");
    return true;
}

bool IORelocator::hookStat() {
    // TODO: 实现stat系统调用Hook
    LOGD(TAG, "Hook stat system call");
    return true;
}

bool IORelocator::hookAccess() {
    // TODO: 实现access系统调用Hook
    LOGD(TAG, "Hook access system call");
    return true;
}

bool IORelocator::hookUnlink() {
    // TODO: 实现unlink系统调用Hook
    LOGD(TAG, "Hook unlink system call");
    return true;
}

bool IORelocator::hookRename() {
    // TODO: 实现rename系统调用Hook
    LOGD(TAG, "Hook rename system call");
    return true;
}

bool IORelocator::hookOpendir() {
    // TODO: 实现opendir系统调用Hook
    LOGD(TAG, "Hook opendir system call");
    return true;
}

bool IORelocator::hookMkdir() {
    // TODO: 实现mkdir系统调用Hook
    LOGD(TAG, "Hook mkdir system call");
    return true;
}

bool IORelocator::hookRmdir() {
    // TODO: 实现rmdir系统调用Hook
    LOGD(TAG, "Hook rmdir system call");
    return true;
}

// JNI接口函数
extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_IORelocator_nativeInitialize(JNIEnv* env, jobject thiz) {
    return IORelocator::getInstance()->initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_com_lody_virtual_IORelocator_nativeCleanup(JNIEnv* env, jobject thiz) {
    IORelocator::getInstance()->cleanup();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_IORelocator_nativeAddPathMapping(JNIEnv* env, jobject thiz, 
                                                      jstring originalPath, jstring virtualPath) {
    const char* origPath = env->GetStringUTFChars(originalPath, nullptr);
    const char* virtPath = env->GetStringUTFChars(virtualPath, nullptr);
    
    bool result = IORelocator::getInstance()->addPathMapping(origPath, virtPath);
    
    env->ReleaseStringUTFChars(originalPath, origPath);
    env->ReleaseStringUTFChars(virtualPath, virtPath);
    
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lody_virtual_IORelocator_nativeRemovePathMapping(JNIEnv* env, jobject thiz, jstring originalPath) {
    const char* origPath = env->GetStringUTFChars(originalPath, nullptr);
    
    bool result = IORelocator::getInstance()->removePathMapping(origPath);
    
    env->ReleaseStringUTFChars(originalPath, origPath);
    
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_lody_virtual_IORelocator_nativeRedirectPath(JNIEnv* env, jobject thiz, jstring originalPath) {
    const char* origPath = env->GetStringUTFChars(originalPath, nullptr);
    
    std::string redirectedPath = IORelocator::getInstance()->redirectPath(origPath);
    
    env->ReleaseStringUTFChars(originalPath, origPath);
    
    return env->NewStringUTF(redirectedPath.c_str());
}

} // namespace VirtualSpace 