# 虚拟空间库ProGuard规则

# 保留虚拟空间核心类
-keep class com.lody.virtual.** { *; }
-keepclassmembers class com.lody.virtual.** { *; }

# 保留虚拟核心
-keep class com.lody.virtual.client.core.VirtualCore { *; }
-keep class com.lody.virtual.client.core.VirtualCore$* { *; }

# 保留包管理器
-keep class com.lody.virtual.pm.** { *; }
-keepclassmembers class com.lody.virtual.pm.** { *; }

# 保留Hook框架
-keep class com.lody.virtual.hook.** { *; }
-keepclassmembers class com.lody.virtual.hook.** { *; }

# 保留安全机制
-keep class com.lody.virtual.security.** { *; }
-keepclassmembers class com.lody.virtual.security.** { *; }

# 保留性能优化
-keep class com.lody.virtual.optimization.** { *; }
-keepclassmembers class com.lody.virtual.optimization.** { *; }

# 保留工具类
-keep class com.lody.virtual.helper.** { *; }
-keepclassmembers class com.lody.virtual.helper.** { *; }

# 保留环境类
-keep class com.lody.virtual.client.env.** { *; }
-keepclassmembers class com.lody.virtual.client.env.** { *; }

# 保留进程管理
-keep class com.lody.virtual.client.stub.** { *; }
-keepclassmembers class com.lody.virtual.client.stub.** { *; }

# 保留服务类
-keep class com.lody.virtual.client.service.** { *; }
-keepclassmembers class com.lody.virtual.client.service.** { *; }

# 保留插件相关
-keep class com.lody.virtual.client.ipc.** { *; }
-keepclassmembers class com.lody.virtual.client.ipc.** { *; }

# 保留反射相关
-keep class com.lody.virtual.remote.** { *; }
-keepclassmembers class com.lody.virtual.remote.** { *; }

# 保留系统API Hook
-keep class com.lody.virtual.hook.proxies.** { *; }
-keepclassmembers class com.lody.virtual.hook.proxies.** { *; }

# 保留基础类
-keep class com.lody.virtual.client.core.** { *; }
-keepclassmembers class com.lody.virtual.client.core.** { *; }

# 保留工具方法
-keepclassmembers class * {
    @com.lody.virtual.hook.annotations.HookMethod *;
    @com.lody.virtual.hook.annotations.HookMethodBackup *;
    @com.lody.virtual.hook.annotations.HookMethodProxy *;
}

# 保留注解
-keep @interface com.lody.virtual.hook.annotations.** { *; }

# 保留接口
-keep interface com.lody.virtual.** { *; }

# 保留枚举
-keep enum com.lody.virtual.** { *; }

# 保留常量
-keepclassmembers class com.lody.virtual.** {
    public static final *;
}

# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留回调方法
-keepclassmembers class * {
    void on*(...);
}

# 保留getter/setter方法
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 保留Parcelable实现
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable实现
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留R文件
-keep class **.R$* {
    public static <fields>;
}

# 保留资源相关
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.app.Fragment

# 保留View相关
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    void set*(...);
    *** get*();
}

# 保留自定义View
-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
}

# 保留动画相关
-keep public class * extends android.animation.Animator {
    public <init>();
    void setDuration(long);
    void setInterpolator(android.animation.TimeInterpolator);
    void start();
    void cancel();
}

# 保留网络相关
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 保留Retrofit相关
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 保留Gson相关
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留Glide相关
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# 保留RxJava相关
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }
-keep interface io.reactivex.** { *; }

# 保留Dagger相关
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep class javax.inject.** { *; }
-keep interface javax.inject.** { *; }

# 保留ButterKnife相关
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# 保留EventBus相关
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# 保留Room相关
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 保留WorkManager相关
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker

# 保留Security相关
-keep class androidx.security.crypto.** { *; }

# 保留测试相关
-dontnote junit.framework.**
-dontnote junit.runner.**

# 保留日志相关
-keepclassmembers class * {
    @android.util.Log *;
}

# 保留反射相关
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# 保留调试信息
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留异常信息
-keepattributes Exceptions

# 保留内部类
-keepattributes InnerClasses

# 保留泛型信息
-keepattributes Signature

# 保留注解
-keepattributes *Annotation*

# 保留枚举
-keepattributes Enum

# 保留本地方法
-keepattributes Native

# 保留同步方法
-keepattributes Synchronization

# 保留桥接方法
-keepattributes Bridge

# 保留变参方法
-keepattributes Varargs

# 保留废弃方法
-keepattributes Deprecated

# 保留注解处理器
-keepattributes AnnotationDefault

# 保留枚举常量
-keepattributes EnumConstant

# 保留类型注解
-keepattributes TypeAnnotation

# 保留方法参数注解
-keepattributes MethodParameters

# 保留模块信息
-keepattributes Module

# 保留包信息
-keepattributes Package

# 保留版本信息
-keepattributes Version

# 保留调试信息
-keepattributes Debug

# 保留行号信息
-keepattributes LineNumberTable

# 保留源文件信息
-keepattributes SourceFile

# 保留本地变量信息
-keepattributes LocalVariableTable

# 保留本地变量类型信息
-keepattributes LocalVariableTypeTable

# 保留常量池信息
-keepattributes ConstantValue

# 保留代码属性
-keepattributes Code

# 保留栈映射表
-keepattributes StackMapTable

# 保留BootstrapMethods
-keepattributes BootstrapMethods

# 保留RuntimeVisibleTypeAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# 保留RuntimeInvisibleTypeAnnotations
-keepattributes RuntimeInvisibleTypeAnnotations

# 保留RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# 保留RuntimeInvisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# 保留RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleAnnotations

# 保留RuntimeInvisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# 保留EnclosingMethod
-keepattributes EnclosingMethod

# 保留Synthetic
-keepattributes Synthetic 