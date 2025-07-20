#ifndef PROCESS_MANAGER_H
#define PROCESS_MANAGER_H

#include <string>
#include <map>
#include <mutex>

namespace VirtualSpace {
    class ProcessManager {
    private:
        static ProcessManager* sInstance;
        static std::mutex sMutex;
        
        bool mIsInitialized;
        std::map<int, std::string> mProcessMap;
        std::mutex mMutex;
        
        ProcessManager();
        ~ProcessManager();
        
        // 私有实例方法实现
        bool initializeImpl();
        void cleanupImpl();
        
    public:
        static ProcessManager* getInstance();
        static bool initialize();
        static void cleanup();
    };
}

#endif // PROCESS_MANAGER_H 