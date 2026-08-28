plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.intervalreminder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.intervalreminder"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.0")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    // For Kotlin Symbol Processing (KSP), but I'll stick to annotationProcessor for simplicity if KSP isn't setup
    // Alternatively, just use kapt if it was there, but it's not. 
    // I'll add KSP later if needed, but for now annotationProcessor usually works in simple setups or I can use kapt.
    // Actually, I should probably use KSP or Kapt for Room in Kotlin. 
}
