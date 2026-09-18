plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    namespace = "com.edusphere.urduassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edusphere.urduassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.25.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
