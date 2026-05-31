# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve source file and line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin annotations needed by serialization and reflection
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Navigation3 route NavKey classes and all other @Serializable classes
-keepnames @kotlinx.serialization.Serializable class *
-keepclassmembers @kotlinx.serialization.Serializable class * { *; }

# Credential Manager (native Google Sign-In)
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }