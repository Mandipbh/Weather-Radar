# Add project specific ProGuard rules here.

# Hilt/Dagger rules
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Retrofit/Gson rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep class com.example.theweatherapp.data.model.** { *; }

# Glide rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Lottie rules
-keep class com.airbnb.lottie.** { *; }

# Mapbox rules
-keep class com.mapbox.** { *; }

# AppLovin rules
-keep class com.applovin.** { *; }

# ViewBinding
-keep class com.example.theweatherapp.databinding.** { *; }
