#ifndef SUBSTRATE_HOOK_H
#define SUBSTRATE_HOOK_H

namespace VirtualSpace {
    class SubstrateHook {
    public:
        static bool initialize();
        static void cleanup();
    };
}

#endif // SUBSTRATE_HOOK_H
