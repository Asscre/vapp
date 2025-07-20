# 虚拟空间应用ProGuard规则

# 保留应用核心类
-keep class com.virtualspace.app.** { *; }
-keepclassmembers class com.virtualspace.app.** { *; }

# 保留Activity
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.fragment.app.FragmentActivity

# 保留Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment

# 保留Service
-keep public class * extends android.app.Service
-keep public class * extends androidx.core.app.CoreComponentFactory

# 保留BroadcastReceiver
-keep public class * extends android.content.BroadcastReceiver

# 保留ContentProvider
-keep public class * extends android.content.ContentProvider

# 保留Application
-keep public class * extends android.app.Application

# 保留View
-keep public class * extends android.view.View
-keep public class * extends android.widget.*

# 保留自定义View
-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
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
-keep public class * extends android.preference.Preference

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

# 保留Kotlin相关
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# 保留协程相关
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# 保留ViewBinding相关
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * bind(android.view.View);
}

# 保留DataBinding相关
-keep class * implements androidx.databinding.DataBindingComponent {
    public <init>();
}

# 保留Lifecycle相关
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# 保留Navigation相关
-keepnames class androidx.navigation.fragment.NavHostFragment
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# 保留Material Design相关
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# 保留ConstraintLayout相关
-keep class androidx.constraintlayout.** { *; }
-dontwarn androidx.constraintlayout.**

# 保留RecyclerView相关
-keep class androidx.recyclerview.** { *; }
-dontwarn androidx.recyclerview.**

# 保留CardView相关
-keep class androidx.cardview.** { *; }
-dontwarn androidx.cardview.**

# 保留SwipeRefreshLayout相关
-keep class androidx.swiperefreshlayout.** { *; }
-dontwarn androidx.swiperefreshlayout.**

# 保留ViewPager相关
-keep class androidx.viewpager.** { *; }
-dontwarn androidx.viewpager.**

# 保留Fragment相关
-keep class androidx.fragment.** { *; }
-dontwarn androidx.fragment.**

# 保留AppCompat相关
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# 保留Core相关
-keep class androidx.core.** { *; }
-dontwarn androidx.core.**

# 保留Collection相关
-keep class androidx.collection.** { *; }
-dontwarn androidx.collection.**

# 保留Annotation相关
-keep class androidx.annotation.** { *; }
-dontwarn androidx.annotation.**

# 保留Arch相关
-keep class androidx.arch.** { *; }
-dontwarn androidx.arch.**

# 保留SavedState相关
-keep class androidx.savedstate.** { *; }
-dontwarn androidx.savedstate.**

# 保留Activity相关
-keep class androidx.activity.** { *; }
-dontwarn androidx.activity.**

# 保留Window相关
-keep class androidx.window.** { *; }
-dontwarn androidx.window.**

# 保留Startup相关
-keep class androidx.startup.** { *; }
-dontwarn androidx.startup.**

# 保留Tracing相关
-keep class androidx.tracing.** { *; }
-dontwarn androidx.tracing.**

# 保留VersionedParcelable相关
-keep class androidx.versionedparcelable.** { *; }
-dontwarn androidx.versionedparcelable.**

# 保留DocumentFile相关
-keep class androidx.documentfile.** { *; }
-dontwarn androidx.documentfile.**

# 保留Print相关
-keep class androidx.print.** { *; }
-dontwarn androidx.print.**

# 保留LocalBroadcastManager相关
-keep class androidx.localbroadcastmanager.** { *; }
-dontwarn androidx.localbroadcastmanager.**

# 保留Loader相关
-keep class androidx.loader.** { *; }
-dontwarn androidx.loader.**

# 保留CursorAdapter相关
-keep class androidx.cursoradapter.** { *; }
-dontwarn androidx.cursoradapter.**

# 保留DrawerLayout相关
-keep class androidx.drawerlayout.** { *; }
-dontwarn androidx.drawerlayout.**

# 保留SlidingPaneLayout相关
-keep class androidx.slidingpanelayout.** { *; }
-dontwarn androidx.slidingpanelayout.**

# 保留CoordinatorLayout相关
-keep class androidx.coordinatorlayout.** { *; }
-dontwarn androidx.coordinatorlayout.**

# 保留PercentLayout相关
-keep class androidx.percentlayout.** { *; }
-dontwarn androidx.percentlayout.**

# 保留Palette相关
-keep class androidx.palette.** { *; }
-dontwarn androidx.palette.**

# 保留ExifInterface相关
-keep class androidx.exifinterface.** { *; }
-dontwarn androidx.exifinterface.**

# 保留Emoji相关
-keep class androidx.emoji.** { *; }
-dontwarn androidx.emoji.**

# 保留Autofill相关
-keep class androidx.autofill.** { *; }
-dontwarn androidx.autofill.**

# 保留ContentPager相关
-keep class androidx.contentpager.** { *; }
-dontwarn androidx.contentpager.**

# 保留Recommendation相关
-keep class androidx.recommendation.** { *; }
-dontwarn androidx.recommendation.**

# 保留MediaRouter相关
-keep class androidx.mediarouter.** { *; }
-dontwarn androidx.mediarouter.**

# 保留Leanback相关
-keep class androidx.leanback.** { *; }
-dontwarn androidx.leanback.**

# 保留GridLayout相关
-keep class androidx.gridlayout.** { *; }
-dontwarn androidx.gridlayout.**

# 保留RecyclerView相关
-keep class androidx.recyclerview.** { *; }
-dontwarn androidx.recyclerview.**

# 保留Preference相关
-keep class androidx.preference.** { *; }
-dontwarn androidx.preference.**

# 保留Legacy相关
-keep class androidx.legacy.** { *; }
-dontwarn androidx.legacy.**

# 保留HeifWriter相关
-keep class androidx.heifwriter.** { *; }
-dontwarn androidx.heifwriter.**

# 保留GifDrawable相关
-keep class androidx.gifdrawable.** { *; }
-dontwarn androidx.gifdrawable.**

# 保留Dynamite相关
-keep class androidx.dynamite.** { *; }
-dontwarn androidx.dynamite.**

# 保留CustomView相关
-keep class androidx.customview.** { *; }
-dontwarn androidx.customview.**

# 保留Browser相关
-keep class androidx.browser.** { *; }
-dontwarn androidx.browser.**

# 保留Biometric相关
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# 保留AsyncLayoutInflater相关
-keep class androidx.asynclayoutinflater.** { *; }
-dontwarn androidx.asynclayoutinflater.**

# 保留Annotation相关
-keep class androidx.annotation.** { *; }
-dontwarn androidx.annotation.**

# 保留Aidl相关
-keep class androidx.aidl.** { *; }
-dontwarn androidx.aidl.**

# 保留Ads相关
-keep class androidx.ads.** { *; }
-dontwarn androidx.ads.**

# 保留AccessibilityService相关
-keep class androidx.accessibilityservice.** { *; }
-dontwarn androidx.accessibilityservice.**

# 保留WebView相关
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

# 保留Telephony相关
-keep class android.telephony.** { *; }
-dontwarn android.telephony.**

# 保留Telecom相关
-keep class android.telecom.** { *; }
-dontwarn android.telecom.**

# 保留System相关
-keep class android.system.** { *; }
-dontwarn android.system.**

# 保留Speech相关
-keep class android.speech.** { *; }
-dontwarn android.speech.**

# 保留Service相关
-keep class android.service.** { *; }
-dontwarn android.service.**

# 保留Security相关
-keep class android.security.** { *; }
-dontwarn android.security.**

# 保留Provider相关
-keep class android.provider.** { *; }
-dontwarn android.provider.**

# 保留Print相关
-keep class android.print.** { *; }
-dontwarn android.print.**

# 保留PrintService相关
-keep class android.printservice.** { *; }
-dontwarn android.printservice.**

# 保留Preference相关
-keep class android.preference.** { *; }
-dontwarn android.preference.**

# 保留Os相关
-keep class android.os.** { *; }
-dontwarn android.os.**

# 保留Net相关
-keep class android.net.** { *; }
-dontwarn android.net.**

# 保留Media相关
-keep class android.media.** { *; }
-dontwarn android.media.**

# 保留Location相关
-keep class android.location.** { *; }
-dontwarn android.location.**

# 保留Hardware相关
-keep class android.hardware.** { *; }
-dontwarn android.hardware.**

# 保留Graphics相关
-keep class android.graphics.** { *; }
-dontwarn android.graphics.**

# 保留Gesture相关
-keep class android.gesture.** { *; }
-dontwarn android.gesture.**

# 保留Filter相关
-keep class android.filter.** { *; }
-dontwarn android.filter.**

# 保留Ext相关
-keep class android.ext.** { *; }
-dontwarn android.ext.**

# 保留Drm相关
-keep class android.drm.** { *; }
-dontwarn android.drm.**

# 保留Database相关
-keep class android.database.** { *; }
-dontwarn android.database.**

# 保留Content相关
-keep class android.content.** { *; }
-dontwarn android.content.**

# 保留Companion相关
-keep class android.companion.** { *; }
-dontwarn android.companion.**

# 保留Bluetooth相关
-keep class android.bluetooth.** { *; }
-dontwarn android.bluetooth.**

# 保留App相关
-keep class android.app.** { *; }
-dontwarn android.app.**

# 保留Animation相关
-keep class android.animation.** { *; }
-dontwarn android.animation.**

# 保留Accounts相关
-keep class android.accounts.** { *; }
-dontwarn android.accounts.**

# 保留Accessibility相关
-keep class android.accessibilityservice.** { *; }
-dontwarn android.accessibilityservice.**

# 保留Accessibility相关
-keep class android.accessibility.** { *; }
-dontwarn android.accessibility.**

# 保留AbstractMethodError相关
-keep class java.lang.AbstractMethodError { *; }
-dontwarn java.lang.AbstractMethodError

# 保留Annotation相关
-keep class java.lang.annotation.** { *; }
-dontwarn java.lang.annotation.**

# 保留Reflect相关
-keep class java.lang.reflect.** { *; }
-dontwarn java.lang.reflect.**

# 保留Thread相关
-keep class java.lang.Thread { *; }
-dontwarn java.lang.Thread

# 保留ThreadGroup相关
-keep class java.lang.ThreadGroup { *; }
-dontwarn java.lang.ThreadGroup

# 保留Throwable相关
-keep class java.lang.Throwable { *; }
-dontwarn java.lang.Throwable

# 保留System相关
-keep class java.lang.System { *; }
-dontwarn java.lang.System

# 保留String相关
-keep class java.lang.String { *; }
-dontwarn java.lang.String

# 保留StringBuilder相关
-keep class java.lang.StringBuilder { *; }
-dontwarn java.lang.StringBuilder

# 保留StringBuffer相关
-keep class java.lang.StringBuffer { *; }
-dontwarn java.lang.StringBuffer

# 保留StackTraceElement相关
-keep class java.lang.StackTraceElement { *; }
-dontwarn java.lang.StackTraceElement

# 保留Runtime相关
-keep class java.lang.Runtime { *; }
-dontwarn java.lang.Runtime

# 保留RuntimeException相关
-keep class java.lang.RuntimeException { *; }
-dontwarn java.lang.RuntimeException

# 保留Process相关
-keep class java.lang.Process { *; }
-dontwarn java.lang.Process

# 保留ProcessBuilder相关
-keep class java.lang.ProcessBuilder { *; }
-dontwarn java.lang.ProcessBuilder

# 保留Package相关
-keep class java.lang.Package { *; }
-dontwarn java.lang.Package

# 保留Object相关
-keep class java.lang.Object { *; }
-dontwarn java.lang.Object

# 保留Number相关
-keep class java.lang.Number { *; }
-dontwarn java.lang.Number

# 保留NullPointerException相关
-keep class java.lang.NullPointerException { *; }
-dontwarn java.lang.NullPointerException

# 保留NoSuchMethodException相关
-keep class java.lang.NoSuchMethodException { *; }
-dontwarn java.lang.NoSuchMethodException

# 保留NoSuchFieldException相关
-keep class java.lang.NoSuchFieldException { *; }
-dontwarn java.lang.NoSuchFieldException

# 保留NoSuchElementException相关
-keep class java.lang.NoSuchElementException { *; }
-dontwarn java.lang.NoSuchElementException

# 保留NoClassDefFoundError相关
-keep class java.lang.NoClassDefFoundError { *; }
-dontwarn java.lang.NoClassDefFoundError

# 保留Math相关
-keep class java.lang.Math { *; }
-dontwarn java.lang.Math

# 保留Long相关
-keep class java.lang.Long { *; }
-dontwarn java.lang.Long

# 保留LinkageError相关
-keep class java.lang.LinkageError { *; }
-dontwarn java.lang.LinkageError

# 保留Integer相关
-keep class java.lang.Integer { *; }
-dontwarn java.lang.Integer

# 保留InstantiationException相关
-keep class java.lang.InstantiationException { *; }
-dontwarn java.lang.InstantiationException

# 保留IllegalStateException相关
-keep class java.lang.IllegalStateException { *; }
-dontwarn java.lang.IllegalStateException

# 保留IllegalArgumentException相关
-keep class java.lang.IllegalArgumentException { *; }
-dontwarn java.lang.IllegalArgumentException

# 保留IllegalAccessException相关
-keep class java.lang.IllegalAccessException { *; }
-dontwarn java.lang.IllegalAccessException

# 保留IndexOutOfBoundsException相关
-keep class java.lang.IndexOutOfBoundsException { *; }
-dontwarn java.lang.IndexOutOfBoundsException

# 保留IncompatibleClassChangeError相关
-keep class java.lang.IncompatibleClassChangeError { *; }
-dontwarn java.lang.IncompatibleClassChangeError

# 保留InheritableThreadLocal相关
-keep class java.lang.InheritableThreadLocal { *; }
-dontwarn java.lang.InheritableThreadLocal

# 保留Inheritance相关
-keep class java.lang.Inheritance { *; }
-dontwarn java.lang.Inheritance