#ifndef IORELOCATOR_H
#define IORELOCATOR_H

#include <string>
#include <map>
#include <mutex>
#include <jni.h>

// 前向声明
namespace VirtualSpace {
    class SubstrateHook;
}

namespace VirtualSpace {

/**
 * IO重定向器
 * 负责拦截和重定向文件系统操作
 */
class IORelocator {
public:
    static IORelocator* getInstance();
    
    /**
     * 初始化IO重定向器
     */
    bool initialize();
    
    /**
     * 清理资源
     */
    void cleanup();
    
    /**
     * 添加路径映射
     * @param originalPath 原始路径
     * @param virtualPath 虚拟路径
     * @return 是否成功
     */
    bool addPathMapping(const std::string& originalPath, const std::string& virtualPath);
    
    /**
     * 移除路径映射
     * @param originalPath 原始路径
     * @return 是否成功
     */
    bool removePathMapping(const std::string& originalPath);
    
    /**
     * 重定向路径
     * @param originalPath 原始路径
     * @return 重定向后的路径
     */
    std::string redirectPath(const std::string& originalPath);
    
private:
    IORelocator();
    ~IORelocator();
    
    // 禁用拷贝构造和赋值
    IORelocator(const IORelocator&) = delete;
    IORelocator& operator=(const IORelocator&) = delete;
    
    // 单例相关
    static IORelocator* sInstance;
    static std::mutex sMutex;
    
    // 成员变量
    bool mIsInitialized;
    std::map<std::string, std::string> mPathMappings;
    std::mutex mMutex;
    
    // 系统调用Hook相关
    bool initializeSystemCallHooks();
    void cleanupSystemCallHooks();
    bool hookFileOperations();
    bool hookDirectoryOperations();
    
    // 具体的Hook实现
    bool hookOpen();
    bool hookStat();
    bool hookAccess();
    bool hookUnlink();
    bool hookRename();
    bool hookOpendir();
    bool hookMkdir();
    bool hookRmdir();
};

} // namespace VirtualSpace

#endif // IORELOCATOR_H 