import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    kotlin("kapt")
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.travelhelper"
    compileSdk = 36

    defaultConfig {


        applicationId = "com.example.travelhelper"
        minSdk = 26
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
        viewBinding = true
    }
}

dependencies {
    implementation("com.yandex.android:maps.mobile:4.19.0-lite")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.room:room-runtime:2.8.0") // Библиотека "Room"
    kapt("androidx.room:room-compiler:2.8.0") // Кодогенератор
    implementation("androidx.room:room-ktx:2.8.0") // Дополнительно для Kotlin Coroutines, Kotlin Flows

    val navVersion = "2.9.5"
    // Views/Fragments Integration
    //implementation("androidx.navigation:navigation-fragment:$navVersion")
    //implementation("androidx.navigation:navigation-ui:$navVersion")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")

    // JSON serialization library, works with the Kotlin serialization plugin.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // For photos
    implementation("com.github.bumptech.glide:glide:5.0.5")

    //koin
    val koinVersion = "4.1.0"
    implementation("io.insert-koin:koin-core:${koinVersion}")
    implementation("io.insert-koin:koin-android:${koinVersion}")
    testImplementation("io.insert-koin:koin-test:${koinVersion}")

    implementation("com.google.android.gms:play-services-location:21.3.0")
}

