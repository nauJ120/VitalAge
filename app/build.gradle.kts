plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.vitalage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.vitalage"
        minSdk = 26
        targetSdk = 34
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
    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.lottie)
    implementation(libs.mpandroidchart)

    // Unit testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.androidx.uiautomator)

    // Android testing (instrumentation)
    testImplementation(libs.androidx.junit)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)


    // Additional dependencies
    implementation(libs.androidx.junit.ktx)

    androidTestImplementation(libs.androidx.test.runner)

    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.espresso.contrib)

    testImplementation(libs.mockito.inline)
    // UIAutomator dependency for UI testing
    androidTestImplementation(libs.androidx.uiautomator)
}