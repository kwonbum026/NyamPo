plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.nyampo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nyampo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        viewBinding {
            enable = true
        }
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
}

//dependencies {
//    // Firebase (BOM 관리)
//    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-database")
//
//    // AndroidX 핵심 라이브러리
//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.activity:activity-ktx:1.8.2")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    // CameraX
//    implementation("androidx.camera:camera-camera2:1.3.0")
//    implementation("androidx.camera:camera-lifecycle:1.3.0")
//    implementation("androidx.camera:camera-view:1.3.0")
//    implementation("androidx.camera:camera-core:1.3.0")
//
//    // ML Kit (바코드)
//    implementation("com.google.mlkit:barcode-scanning:17.2.0")
//
//
//    // 튜토리얼 SVG
//    implementation("com.caverock:androidsvg:1.4")
//    implementation(libs.androidx.activity)
//
//    // 테스트
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    implementation("com.naver.maps:map-sdk:3.21.0") // 네이버 지도 SDK 의존성 추가
//    implementation("com.google.android.gms:play-services-location:21.3.0") // FusedLocationSource를 사용하기 위한 의존성 추가
//}
//configurations.all {
//    exclude(group = "com.android.support", module = "support-compat")
//}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //   카메라 기능
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation ("androidx.camera:camera-core:1.3.0")

    //튜토리얼
    implementation ("com.caverock:androidsvg:1.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.naver.maps:map-sdk:3.21.0") // 네이버 지도 SDK 의존성 추가
    implementation("com.google.android.gms:play-services-location:21.3.0") // FusedLocationSource를 사용하기 위한 의존성 추가
}