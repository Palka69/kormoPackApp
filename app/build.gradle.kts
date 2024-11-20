plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services")

    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.kormopack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kormopack"
        minSdk = 27
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
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.android.gms:play-services-auth:21.1.1")
    //implementation("com.google.android.gms:play-services-auth:20.3.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.api-client:google-api-client-gson:1.33.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.44.1")

    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1")

    implementation ("com.google.apis:google-api-services-drive:v3-rev162-1.25.0")

    implementation ("org.apache.poi:poi-ooxml:5.2.3")

    implementation ("com.google.api-client:google-api-client:1.32.1")
}