plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.muhaimen.arenax"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.muhaimen.arenax"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.tools.core)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.play.services.fido)
    implementation(libs.androidx.espresso.core)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.graphview)
    implementation(libs.play.services.auth)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.mpandroidchart)
    implementation(libs.glide)
    kapt(libs.glideCompiler)
    implementation(libs.kotlin)
    implementation(libs.okhttp)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(libs.jsoup)
    implementation("com.arthenica:mobile-ffmpeg-full-gpl:4.4.LTS")
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.exoplayer )
    implementation(libs.media3exoplayer)
}
