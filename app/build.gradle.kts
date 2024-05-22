plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.codex.tala"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codex.tala"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Use the debug API key for debug builds
            buildConfigField("String", "OPENAI_API_KEY", "\"${project.property("OPENAI_API_KEY")}\"")
        }
        release {
            // Use the release API key for release builds
            buildConfigField("String", "OPENAI_API_KEY", "\"${project.property("OPENAI_API_KEY")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging{
        resources.excludes.add("META-INF/LICENSE-notice.md")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/COPYRIGHT")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("io.github.lambdua:api:0.20.7")
    implementation("io.github.lambdua:client:0.20.7")
    implementation("io.github.lambdua:service:0.20.7")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:+")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}