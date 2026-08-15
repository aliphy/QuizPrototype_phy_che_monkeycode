plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.quizprototype_phy_che_deepseek"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quizprototype_phy_che_deepseek"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "userType"
    productFlavors {
        create("studentSec1") {
            dimension = "userType"
            versionNameSuffix = "-sec1"
            buildConfigField("String", "APP_TYPE", "\"STUDENT\"")
            buildConfigField("String", "TEACHER_ID", "\"110306721261455731628\"")
        }
        create("studentSec2") {
            dimension = "userType"
            versionNameSuffix = "-sec2"
            buildConfigField("String", "APP_TYPE", "\"STUDENT\"")
            buildConfigField("String", "TEACHER_ID", "\"110306721261455731628\"")   //  106306912874477616328
        }
        create("teacher") {
            dimension = "userType"
            versionNameSuffix = "-teacher"
            buildConfigField("String", "APP_TYPE", "\"TEACHER\"")
            buildConfigField("String", "TEACHER_ID", "\"\"")
        }
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Lifecycle & Navigation
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.navigation.compose)

    // Google Services
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.client.gson)
    implementation(libs.google.api.services.classroom)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client.gson)
    implementation(libs.google.http.client.android)

    // Local Storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // File Management & Utilities
    implementation(libs.commons.io)
    implementation(libs.zip4j)
    implementation(libs.coil.compose)

    // Document Viewer
    implementation(libs.android.pdf.viewer)
    implementation(libs.pdfium.android)

    // Local Web Server (Jetty)
    implementation(libs.jetty.server)
    implementation(libs.jetty.servlet)
    implementation(libs.jetty.util)

    // UI Enhancement (Accompanist)
    implementation(libs.androidx.browser)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.google.accompanist.permissions)

    // Async (Coroutines)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
