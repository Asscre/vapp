# Hook库ProGuard规则

# 保留SandHook核心类
-keep class com.swift.sandhook.** { *; }
-keepclassmembers class com.swift.sandhook.** { *; }

# 保留Hook相关类
-keep class com.swift.sandhook.hookclass.** { *; }
-keep class com.swift.sandhook.hookdelegate.** { *; }
-keep class com.swift.sandhook.wrapper.** { *; }

# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}
