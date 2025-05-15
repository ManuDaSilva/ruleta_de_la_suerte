plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services") version "4.4.2" apply false
}

android {
    namespace = "com.example.mainactivity"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mainactivity"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compose = true
    }
}

dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.play.services.location)
    kapt("androidx.room:room-compiler:2.6.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson (si estás usando Gson)
    implementation("com.google.code.gson:gson:2.8.8")

    // Moshi (si decides usar Moshi en lugar de Gson)
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")

    // Retrofit Moshi Converter (si usas Moshi)
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Firebase
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:21.0.1")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.0.1")

    // Google Sign-In (si no lo tienes ya)
    implementation("com.google.android.gms:play-services-auth:20.0.1")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.3.1")

    // RxJava para Room
    implementation("androidx.room:room-rxjava3:2.6.1")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    //appcombat
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Coroutines para Room
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

