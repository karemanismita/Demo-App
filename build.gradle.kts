plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mydemoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mydemoapp"
        minSdk = 28
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.firebase.messaging.ktx)

    // Add the Google services BOM to manage Firebase versions automatically
    implementation(platform(libs.firebase.bom))

    // Firebase Analytics dependency
    implementation(libs.firebase.analytics)

    // Firebase Messaging dependency
    implementation(libs.firebase.messaging.ktx)

    // AWS SDK for SNS
    implementation(libs.aws.sdk.sns)

    // AWS SDK for Cognito Identity Provider (if using Cognito for authentication)
    //implementation(libs.aws.sdk.cognitoidentityprovider)

    // AWS SDK Core dependency
    implementation(libs.aws.sdk.core)

    // Optionally, you can include local `.jar` files if required
    implementation(files("libs/android.car.jar"))

    implementation(libs.androidx.app)
    implementation(libs.androidx.app.projected) // Optional for projected apps
}