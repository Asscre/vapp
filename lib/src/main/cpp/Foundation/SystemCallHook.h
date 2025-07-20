#ifndef SYSTEM_CALL_HOOK_H
#define SYSTEM_CALL_HOOK_H

#include <string>
#include <map>
#include <mutex>

namespace VirtualSpace {
    class SystemCallHook {
    private:
        static SystemCallHook* sInstance;
        static std::mutex sMutex;
        
        bool mIsInitialized;
        std::map<int, void*> mHookMap;
        std::mutex mMutex;
        
        SystemCallHook();
        ~SystemCallHook();
        
        // 私有实例方法实现
        bool initializeImpl();
        void cleanupImpl();
        
    public:
        static SystemCallHook* getInstance();
        static bool initialize();
        static void cleanup();
    };
}

#endif // SYSTEM_CALL_HOOK_H 