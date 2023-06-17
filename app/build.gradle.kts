@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("kapt")
    kotlin("android")
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.yourapphistory"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        applicationId = "com.example.yourapphistory"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packagingOptions {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(libs.androidX.core.ktx)
    implementation(libs.bundles.compose)
    implementation(libs.androidX.navigation.compose)
    implementation(libs.androidX.paging.compose)
    implementation(libs.accompanist.drawablepainter)
}