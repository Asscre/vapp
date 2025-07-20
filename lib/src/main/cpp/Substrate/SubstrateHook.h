#ifndef SUBSTRATE_HOOK_H
#define SUBSTRATE_HOOK_H

#include <string>
#include <map>
#include <mutex>
#include <memory>

namespace VirtualSpace {
    class SubstrateHook {
    private:
        static SubstrateHook* sInstance;
        static std::mutex sMutex;
        
        bool mIsInitialized;
        std::map<void*, void*> mHookManager;
        std::mutex mMutex;
        
        SubstrateHook();
        ~SubstrateHook();
        
        bool initializeARMHook();
        bool initializeARM64Hook();
        void cleanupAllHooks();
        void cleanupARMHook();
        void cleanupARM64Hook();
        
        bool hookMethodARM(void* targetMethod, void* hookMethod, void* backupMethod);
        bool hookMethodARM64(void* targetMethod, void* hookMethod, void* backupMethod);
        bool hookMethodX86(void* targetMethod, void* hookMethod, void* backupMethod);
        bool hookMethodX86_64(void* targetMethod, void* hookMethod, void* backupMethod);
        
        bool unhookMethodARM(void* targetMethod, const struct HookInfo& hookInfo);
        bool unhookMethodARM64(void* targetMethod, const struct HookInfo& hookInfo);
        bool unhookMethodX86(void* targetMethod, const struct HookInfo& hookInfo);
        bool unhookMethodX86_64(void* targetMethod, const struct HookInfo& hookInfo);
        
    public:
        static SubstrateHook* getInstance();
        static bool initialize();
        static void cleanup();
        static bool hookMethod(void* targetMethod, void* hookMethod, void* backupMethod);
        static bool unhookMethod(void* targetMethod);
        static void* callOriginMethod(void* backupMethod, void* receiver, void* args);
        
        struct HookInfo {
            void* originalMethod;
            void* backupMethod;
            void* hookMethod;
        };
    };
}

#endif // SUBSTRATE_HOOK_H
