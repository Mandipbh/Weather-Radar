plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.theweatherapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.theweatherapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Size Optimization: Keep only necessary resources
        resourceConfigurations += "en"
        
        ndk {
            // Size Optimization: Only include common ABIs (Mapbox is large, this helps significantly)
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    signingConfigs {
        create("release") {
            // NOTE: You must generate this file in the 'app' folder using the command:
            // keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
            storeFile = file("release-keystore.jks")
            storePassword = "password123"
            keyAlias = "release-key"
            keyPassword = "password123"
        }
    }

    buildTypes {
        release {
            // Size Optimization: Enable R8 obfuscation and resource shrinking
            isMinifyEnabled = true
            isShrinkResources = true
            
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    
    // Size Optimization: Enable splits for APKs if not using App Bundle
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.legacy.support.v4)
    ksp(libs.hilt.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Glide
    implementation(libs.glide)

    // Play Services Location
    implementation(libs.play.services.location)

    // AppLovin
    implementation(libs.applovin.sdk)

    //mapbox
    implementation("com.mapbox.maps:android-ndk27:11.21.0")

    // Lottie
    implementation(libs.lottie)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
