#ifndef ARM64_HOOK_H
#define ARM64_HOOK_H

namespace VirtualSpace {
    class ARM64Hook {
    public:
        static bool initialize();
        static void cleanup();
        static bool hookMethod(void* targetMethod, void* hookMethod, void* backupMethod);
        static bool unhookMethod(void* targetMethod);
    };
}

#endif // ARM64_HOOK_H 