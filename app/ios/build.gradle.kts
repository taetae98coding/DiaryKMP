plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.primitive.kotlin)
    alias(libs.plugins.primitive.compose)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    swiftExport {
        moduleName = "DiaryIos"
        flattenPackage = "io.github.taetae98coding.diary"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(libs.jetbrains.compose.ui)
            }
        }
    }
}
