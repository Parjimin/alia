plugins {
    id("com.android.application")
}

fun String.asBuildConfigString(): String = buildString {
    append('"')
    this@asBuildConfigString.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            else -> append(character)
        }
    }
    append('"')
}

val supabaseUrl = providers.gradleProperty("ALIA_SUPABASE_URL")
    .orElse(providers.environmentVariable("ALIA_SUPABASE_URL"))
    .getOrElse("")
val supabasePublishableKey = providers.gradleProperty("ALIA_SUPABASE_PUBLISHABLE_KEY")
    .orElse(providers.environmentVariable("ALIA_SUPABASE_PUBLISHABLE_KEY"))
    .getOrElse("")

android {
    namespace = "com.littleblueworld.alia"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.littleblueworld.alia"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "SUPABASE_URL", supabaseUrl.asBuildConfigString())
        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            supabasePublishableKey.asBuildConfigString(),
        )
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
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
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Activity 1.12+ adds an unused NavigationEvent -> Compose annotation edge.
    //noinspection GradleDependency
    implementation("androidx.activity:activity:1.11.0")

    // M1 persistence and structured lifecycle work. Both are required by ARCHITECTURE.md.
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // M14: reliable, network-constrained delivery after process death.
    implementation("androidx.work:work-runtime:2.11.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
