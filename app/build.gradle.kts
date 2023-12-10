plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.objtranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.objtranslator"
        minSdk = 21
        targetSdk = 33
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
    buildFeatures {
        mlModelBinding = true
    }

    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //image classification
    implementation("com.google.mlkit:image-labeling:17.0.5")
    //object detection
    implementation("com.google.mlkit:object-detection:16.2.6")
    //text translation
    implementation("com.google.mlkit:translate:17.0.1")
    //log-in/register
    implementation("com.google.firebase:firebase-auth:22.3.0")
    //custom obj detector
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("com.google.mlkit:object-detection-custom:17.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}