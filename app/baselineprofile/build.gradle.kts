import com.android.build.api.dsl.ManagedVirtualDevice
import io.github.taetae98coding.diary.buildlogic.BuildLogic

plugins {
    alias(libs.plugins.primitive.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = projects.app.android.path

    testOptions.managedDevices.allDevices {
        // 프로필을 생성하려면 root 권한이 있는 aosp 이미지가 필요하다.
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }

    // 대상 앱의 flavor와 같은 이름으로 맞춰야 variant가 짝지어진다.
    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
        }

        create("real") {
            dimension = "environment"
            testInstrumentationRunnerArguments["targetAppId"] = BuildLogic.NAMESPACE
        }
    }
}

androidComponents {
    // 앱이 real에서 생성한 프로필만 받아 모든 variant에 적용하므로 dev variant는 만들지 않는다.
    beforeVariants(selector().withFlavor("environment" to "dev")) { variantBuilder ->
        variantBuilder.enable = false
    }
}

baselineProfile {
    // 연결된 기기는 저장 공간, 이미 설치된 앱의 서명, 기기 상태에 따라 결과가 달라지므로 Gradle 관리 기기에서만 생성한다.
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
}
