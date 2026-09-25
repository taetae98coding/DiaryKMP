import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.localProperties

plugins {
    alias(libs.plugins.primitive.android.application)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.dependency.guard)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
}

private val localProperties = localProperties()

android {
    namespace = BuildLogic.NAMESPACE

    defaultConfig {
        applicationId = BuildLogic.NAMESPACE
        versionCode = BuildLogic.VERSION_CODE
        versionName = BuildLogic.VERSION_NAME
    }

    signingConfigs {
        create("dev") {
            storeFile = file("keystore/dev.jks")
            storePassword = localProperties.getProperty("dev.android.signing.storePassword")
            keyAlias = localProperties.getProperty("dev.android.signing.keyAlias")
            keyPassword = localProperties.getProperty("dev.android.signing.keyPassword")
        }

        create("real") {
            storeFile = file("keystore/real.jks")
            storePassword = localProperties.getProperty("real.android.signing.storePassword")
            keyAlias = localProperties.getProperty("real.android.signing.keyAlias")
            keyPassword = localProperties.getProperty("real.android.signing.keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = null
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            signingConfig = null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            signingConfig = signingConfigs.getByName("dev")

            applicationIdSuffix = ".dev"
            manifestPlaceholders["googleMapApiKey"] = requireNotNull(localProperties.getProperty("dev.android.googleMapApiKey"))
            manifestPlaceholders["naverMapNcpKeyId"] = requireNotNull(localProperties.getProperty("dev.naverMapNcpKeyId"))
        }

        create("real") {
            dimension = "environment"
            signingConfig = signingConfigs.getByName("real")

            applicationIdSuffix = null
            manifestPlaceholders["googleMapApiKey"] = requireNotNull(localProperties.getProperty("real.android.googleMapApiKey"))
            manifestPlaceholders["naverMapNcpKeyId"] = requireNotNull(localProperties.getProperty("real.naverMapNcpKeyId"))
        }
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.firebase.performance)
    implementation(libs.google.material)
}

dependencyGuard {
    configuration("realReleaseRuntimeClasspath")
}
