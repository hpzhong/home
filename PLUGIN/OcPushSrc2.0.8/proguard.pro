-injars 'F:\SDK\build_new\OcSDK3.1.1.jar'
-outjars 'F:\SDK\build_new\OcSDK3.1.1_G.jar'

-libraryjars 'D:\tools\android-sdk-windows\platforms\android-8\android.jar'

-dontoptimize
-optimizationpasses 5
-dontusemixedcaseclassnames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-verbose
-ignorewarnings

-keep public class * extends android.app.Activity

-keep public class * extends android.app.Application

-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver

-keep public class * extends android.content.ContentProvider

-keep public class * extends android.app.backup.BackupAgentHelper

-keep public class * extends android.preference.Preference

-keep public class com.android.vending.licensing.ILicensingService

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public void set*(...);
}

-keepclasseswithmembers class * {
	public <init>(int,android.content.Context, android.os.Handler);
} 

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class com.oc.system.promotion.activity.PromDesktopAdActivity$MyJavaScriptInterface{*;}

-keep public class com.oc.system.promotion.listener.OcPromSDK {
    <fields>;
    <methods>;
}


-keep public class com.oc.system.statistics.listener.OcStatisticsSDK {
    <fields>;
    <methods>;
}

-keepclasseswithmembers class com.oc.system.util.PhoneInfoUtils {
    public static java.lang.String getIMSI(android.content.Context);
}

-keep class com.lotuseed.android.** {
    <fields>;
    <methods>;
}
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}
-keepattributes *Annotation*