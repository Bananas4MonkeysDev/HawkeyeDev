plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.hawkeyeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hawkeyeapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("androidx.fragment:fragment:1.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common:2.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}