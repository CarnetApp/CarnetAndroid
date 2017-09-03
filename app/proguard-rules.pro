# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/alexandre/mediacenter/android-sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontobfuscate

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses
-keepclassmembers class org.apache.http.**
-dontskipnonpubliclibraryclasses
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-keep public class com.tooleap.sdk.* { public *; }
-dontwarn com.tooleap.sdk.**
-keep class android.support.v7.widget.SearchView { *; }
-keep class com.spisoft.quicknote.*{ *; }
-keep public class com.mypackage.MyClass$MyJavaScriptInterface

-keep public class com.spisoft.quicknote.editor.EditorView$WebViewJavaScriptInterface
-keep public class * implements com.spisoft.quicknote.editor.EditorView$WebViewJavaScriptInterface
-keepclassmembers class com.spisoft.quicknote.editor.EditorView$WebViewJavaScriptInterface {
    <methods>;
}
-keep  class com.fasterxml.jackson.annotation.** {*;}
-keepattributes JavascriptInterface
