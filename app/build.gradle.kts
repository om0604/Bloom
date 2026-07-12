// app/build.gradle.kts
// Bloom — Premium Android Journaling App

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.bloom.app"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.bloom.app"
        minSdk          = 26          // Android 8.0 — ~96% device coverage
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Groq API key — stored in local.properties, never committed to VCS
        val groqApiKey: String = gradleLocalProperties(rootDir, providers)
            .getProperty("GROQ_API_KEY")?.trim() ?: ""
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled     = true
            isShrinkResources   = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Room schema export — schemas committed to VCS for migration safety
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental",    "true")
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable experimental Compose APIs
        freeCompilerArgs += listOf("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Helper to read local.properties without external plugin
fun gradleLocalProperties(
    rootDir: File,
    providers: ProviderFactory
): Properties {
    val properties = Properties()
    val localPropertiesFile = rootDir.resolve("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }
    return properties
}

dependencies {
    // ---- Core ----
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)

    // ---- Compose BOM (aligned versions) ----
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.androidx.activity.compose)

    // ---- Navigation ----
    implementation(libs.androidx.navigation.compose)

    // ---- Lifecycle / ViewModel ----
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ---- Room ----
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ---- DataStore ----
    implementation(libs.datastore.preferences)

    // ---- Coroutines ----
    implementation(libs.kotlinx.coroutines.android)

    // ---- Networking ----
    implementation(libs.okhttp)
    implementation(libs.gson)

    // ---- Lottie ----
    implementation(libs.lottie.compose)

    // ---- Debug ----
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // ---- Testing ----
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
