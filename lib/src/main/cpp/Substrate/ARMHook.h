#ifndef ARM_HOOK_H
#define ARM_HOOK_H

namespace VirtualSpace {
    class ARMHook {
    public:
        static bool initialize();
        static void cleanup();
        static bool hookMethod(void* targetMethod, void* hookMethod, void* backupMethod);
        static bool unhookMethod(void* targetMethod);
    };
}

#endif // ARM_HOOK_H 