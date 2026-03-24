plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.example.gurrywallet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gurrywallet"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // ====== AÑADE ESTO (Configuración del compilador C++) ======
        externalNativeBuild {
            cmake {
                // Longfellow requiere C++17 y extensiones de criptografía por hardware
                cppFlags += "-std=c++17 -march=armv8-a+crypto"
                arguments += "-DANDROID_STL=c++_shared"

                // Compilamos solo para móviles modernos de 64 bits (evita errores en emuladores antiguos)
                abiFilters += "arm64-v8a"
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        prefab = true
    }
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Para Corrutinas
    implementation("com.android.ndk.thirdparty:openssl:1.1.1q-beta-1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    ksp("androidx.room:room-compiler:2.6.1")
}