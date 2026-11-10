plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ca.pebble.watchpatrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "ca.pebble.watchpatrol"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Reddit app credentials — injected from Gradle property or env var
        // (CI secrets), never committed to source. Empty if unset.
        val redditId = (project.findProperty("REDDIT_CLIENT_ID") as String?)
            ?: System.getenv("REDDIT_CLIENT_ID") ?: ""
        val redditSecret = (project.findProperty("REDDIT_CLIENT_SECRET") as String?)
            ?: System.getenv("REDDIT_CLIENT_SECRET") ?: ""
        buildConfigField("String", "REDDIT_CLIENT_ID", "\"$redditId\"")
        buildConfigField("String", "REDDIT_CLIENT_SECRET", "\"$redditSecret\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Flip BuildConfig.DEBUG -> enables OkHttp body logging (see ApiConfig).
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose (versions supplied by the BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.coil.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.retrofit.scalars)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
}
