# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ZhanTao/Documents/04_Program_Files/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keep class com.okay.reader.plugin.pdf.pdflib.** { *; }

#greenDao
-keep class org.greenrobot.greendao.**{*;}
-keep public interface org.greenrobot.greendao.**
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-keep class net.sqlcipher.database.**{*;}
-keep public interface net.sqlcipher.database.**
-dontwarn net.sqlcipher.database.**
-dontwarn org.greenrobot.greendao.**

#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v7.app.Fragment


-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
# keep 使用Pdfviewer的类
-keepclassmembers class com.okay.reader.plugin.pdf.PDFViewer {
   *;
}

-keep class com.okay.reader.plugin.PDFManager {*;}

# keep　 使用Pdfviewer类的类的所有的内部类
-keepclassmembers  class com.okay.reader.plugin.pdf.PDFViewer*{
    *;
}

#内部接口不被混淆
-keep public interface　com.okay.reader.plugin.pdf.PDFViewer$OnPageChangeListener{ *; }
-keep public interface　com.okay.reader.plugin.pdf.PDFViewer$OnPdfLoadListener{ *; }


  #避免混淆注解类
    -dontwarn android.annotation
    -keepattributes *Annotation*

    #避免混淆内部类
    -keepattributes InnerClasses
