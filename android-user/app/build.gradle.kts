plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.user"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.user"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"



        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/api/v1/\"")




        buildConfigField("String", "FIREBASE_API_KEY", "\"AIzaSyANrJhK9AEscyH80Gxc0OPXsL91zsbrLFM\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"1:911148332136:android:411a1e8f9b4281abdcb7dd\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"room-rental-management-dff3d\"")





        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"911148332136-doqmg1emquue1vcr7m1qgrtt2rq62q3p.apps.googleusercontent.com\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)


    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)


    implementation(libs.glide)


    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)


    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
