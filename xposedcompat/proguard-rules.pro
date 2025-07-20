# Xposed兼容层ProGuard规则

# 保留Xposed兼容层类
-keep class com.xposedcompat.** { *; }
-keepclassmembers class com.xposedcompat.** { *; }

# 保留Xposed API
-keep class de.robv.android.xposed.** { *; }
-keep interface de.robv.android.xposed.** { *; }
