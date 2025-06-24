plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.bucovinawanders"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bucovinawanders"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //Google Maps
    implementation("com.google.maps.android:maps-compose:6.1.0") //integrare Google Maps in Jetpack Compose
    implementation("com.google.android.gms:play-services-maps:19.2.0") //servicii Google Maps clasice (suport nativ)
    implementation("com.google.android.gms:play-services-location:21.3.0") //acces la locatie GPS si update-uri de locatie

    //gestionare permisiuni in Compose
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    //retrofit pentru API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0") //biblioteca pentru apeluri HTTP/API
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") //conversie automata JSON in obiecte Kotlin folosind Gson

    //Jetpack Compose UI
    implementation("androidx.compose.ui:ui:1.8.2") //componente de baza UI in Compose
    implementation("androidx.compose.material3:material3:1.3.2") //design Material 3 pentru Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1") //suport pentru lifecycle in Compose
    implementation("androidx.navigation:navigation-compose:2.9.0") //navigare intre ecrane Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") //integrare Hilt pentru injectare dependinte in navigare Compose

    //imagine din URL si afisare usoara
    implementation("io.coil-kt:coil-compose:2.6.0") //incarcare imagini in Compose folosind Coil

    //salvare preferinte
    implementation("androidx.datastore:datastore-preferences:1.1.7") //inlocuieste SharedPreferences, stocare key-value moderna

    //componente si extensii UI Material
    implementation("androidx.compose.material:material-icons-extended:1.7.8") //iconite Material predefinite
    implementation("androidx.compose.material:material:1.8.2") //componente clasice Material pentru Compose
    implementation("androidx.compose.foundation:foundation:1.8.2") //functii de baza (scroll, gesturi, layouturi) in Compose
    implementation("androidx.compose.runtime:runtime:1.8.2") //functii legate de starea UI (remember, mutableState)
    implementation("androidx.compose.material3:material3-window-size-class") //suport pentru design adaptiv (responsive) pe diferite dimensiuni

    //Coroutines (programare asincrona)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1") //suport coroutines pentru play-services
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1") //coroutines pe Android (main thread, scope etc.)

    //pager in Compose (ex: swipe intre imagini)
    implementation("com.google.accompanist:accompanist-pager:0.30.1")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.30.1")

    implementation(libs.transportation.consumer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
