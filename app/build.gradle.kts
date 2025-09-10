import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    kotlin("kapt")
}

android {
    namespace = "com.example.travelhelper"
    compileSdk = 36

    defaultConfig {


        applicationId = "com.example.travelhelper"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        val apiKey = properties.getProperty("MAPKIT_KEY") ?: ""
        if (apiKey.isEmpty()) {
            error("API_KEY not set in local.properties")
        }
        buildConfigField("String", "MAPKIT_KEY", "\"$apiKey\"")

    }

    kapt {
        arguments {arg("room.schemaLocation", "$projectDir/schemas")}
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
        buildConfig = true
    }
}

dependencies {
    implementation("com.yandex.android:maps.mobile:4.19.0-lite")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.room:room-runtime:2.8.0") // Библиотека "Room"
    kapt("androidx.room:room-compiler:2.8.0") // Кодогенератор
    implementation("androidx.room:room-ktx:2.8.0") // Дополнительно для Kotlin Coroutines, Kotlin Flows
}

