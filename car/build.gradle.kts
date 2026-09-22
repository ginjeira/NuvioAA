plugins {
    id("com.android.library")
}

android {
    namespace = "com.nuvio.car"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.car.app:app:1.3.0-beta01")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("io.coil-kt:coil:2.6.0")
    implementation(project(":androidApp"))
    implementation(project(":composeApp"))
}

