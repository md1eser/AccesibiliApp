plugins {

    alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android) // -> AGP +9.0 ya viene integrado
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {


    namespace = "com.accesibilidad.accesibiliapp"
    compileSdk {
        version = release(36)
    }

    buildFeatures { compose = true }

    defaultConfig {
        applicationId = "com.accesibilidad.accesibiliapp"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {


    // Litert
    implementation(libs.litert)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)
    implementation(libs.litert.gpu)
    implementation(libs.litert.gpu.api)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialización
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)



    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.testing)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

}

