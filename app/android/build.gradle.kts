import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.localProperties

plugins {
    alias(libs.plugins.primitive.android.application)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.androidx.baselineprofile)
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

baselineProfile {
    // flavor는 서버 환경만 다르고 실행 경로가 같으므로 real에서만 생성하고, src/main에 두어 dev도 함께 쓴다.
    mergeIntoMain = true
    variants {
        create("real") {
            from(project(projects.app.baselineprofile.path))
        }
    }
    // 생성에는 기기가 필요하므로 일반 빌드에서는 생성하지 않고, 커밋된 프로필을 쓴다.
    automaticGenerationDuringBuild = false
    // 프로필에 담긴 시작 경로의 클래스를 dex 앞쪽에 모아 시작 시 읽는 페이지를 줄인다.
    dexLayoutOptimization = true
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.navigation)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.firebase.performance)
    implementation(libs.google.material)
    implementation(libs.koin.android)
}

dependencyGuard {
    configuration("realReleaseRuntimeClasspath")
}
