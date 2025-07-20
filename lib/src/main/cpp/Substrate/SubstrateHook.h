#ifndef SUBSTRATE_HOOK_H
#define SUBSTRATE_HOOK_H

#include <map>
#include <mutex>
#include <jni.h>

namespace VirtualSpace {

/**
 * Substrate Hook类
 * 提供Native层的Hook功能，支持多架构
 */
class SubstrateHook {
public:
    // 架构枚举
    enum Architecture {
        ARCH_UNKNOWN = 0,
        ARCH_ARM = 1,
        ARCH_ARM64 = 2,
        ARCH_X86 = 3,
        ARCH_X86_64 = 4
    };
    
    // Hook信息结构
    struct HookInfo {
        void* targetMethod;
        void* hookMethod;
        void* backupMethod;
        Architecture architecture;
        long hookTime;
    };
    
    static SubstrateHook* getInstance();
    
    /**
     * 初始化Substrate Hook
     */
    bool initialize();
    
    /**
     * 清理资源
     */
    void cleanup();
    
    /**
     * Hook方法
     * @param targetMethod 目标方法地址
     * @param hookMethod Hook方法地址
     * @param backupMethod 备份方法地址
     * @return 是否成功
     */
    bool hookMethod(void* targetMethod, void* hookMethod, void* backupMethod);
    
    /**
     * 取消Hook
     * @param targetMethod 目标方法地址
     * @return 是否成功
     */
    bool unhookMethod(void* targetMethod);
    
    /**
     * 调用原始方法
     * @param backupMethod 备份方法地址
     * @param receiver 接收者对象
     * @param args 参数
     * @return 调用结果
     */
    void* callOriginMethod(void* backupMethod, void* receiver, void* args);
    
    /**
     * 检查方法是否已Hook
     * @param targetMethod 目标方法地址
     * @return 是否已Hook
     */
    bool isMethodHooked(void* targetMethod);
    
private:
    SubstrateHook();
    ~SubstrateHook();
    
    // 禁用拷贝构造和赋值
    SubstrateHook(const SubstrateHook&) = delete;
    SubstrateHook& operator=(const SubstrateHook&) = delete;
    
    // 单例相关
    static SubstrateHook* sInstance;
    static std::mutex sMutex;
    
    // 成员变量
    bool mIsInitialized;
    std::map<void*, HookInfo> mHookManager;
    
    // 架构相关方法
    bool initializeARMHook();
    bool initializeARM64Hook();
    void cleanupARMHook();
    void cleanupARM64Hook();
    
    // Hook实现方法
    bool hookMethodARM(void* targetMethod, void* hookMethod, void* backupMethod);
    bool hookMethodARM64(void* targetMethod, void* hookMethod, void* backupMethod);
    bool hookMethodX86(void* targetMethod, void* hookMethod, void* backupMethod);
    bool hookMethodX86_64(void* targetMethod, void* hookMethod, void* backupMethod);
    
    // 取消Hook实现方法
    bool unhookMethodARM(void* targetMethod, const HookInfo& hookInfo);
    bool unhookMethodARM64(void* targetMethod, const HookInfo& hookInfo);
    bool unhookMethodX86(void* targetMethod, const HookInfo& hookInfo);
    bool unhookMethodX86_64(void* targetMethod, const HookInfo& hookInfo);
    
    // 工具方法
    void cleanupAllHooks();
    Architecture getArchitecture();
    long getCurrentTime();
};

} // namespace VirtualSpace

#endif // SUBSTRATE_HOOK_H 