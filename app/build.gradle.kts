import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.belltree.pomodoroshareapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.belltree.pomodoroshareapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // TODO: 本番では gradle.properties など外部から注入
        val localProps = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localProps.load(FileInputStream(localFile))
        }
        val apiKey: String = localProps.getProperty("API_KEY", "")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        val supabaseUrl: String = localProps.getProperty("SUPABASE_URL", "")
        val supabaseAnonKey: String = localProps.getProperty("SUPABASE_ANON_KEY", "")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "\"620028010859-cvuadtmajqcdj0v6ri5g2scgv14t47ke.apps.googleusercontent.com\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    // DataStore (Preferences)
    implementation(libs.androidx.datastore.preferences)
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // For firebase (BOM でバージョン一括管理)
    implementation(platform(libs.firebase.bom))
    // For firestore
    implementation(libs.firebase.firestore)
    // For firebase auth
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ui.auth)
    // For navigation compose
    implementation(libs.androidx.navigation.compose)
    // Google Auth
    implementation(libs.google.play.services.auth)

    // For ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // For Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // For Gemini API
    implementation(libs.generativeai)

    // For Lifecycle
    implementation(libs.androidx.lifecycle.extensions)

    // Supabase SDK
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)

    implementation(libs.androidx.work.runtime.ktx)

    // For Vico
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    // Coil for Compose (image loading)
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Image Cropper
    implementation(libs.image.cropper)
}