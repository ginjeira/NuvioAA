plugins {
    id("com.android.library")
}

android {
    namespace = "com.nuvio.car"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation("androidx.car.app:app:1.7.0")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
}

