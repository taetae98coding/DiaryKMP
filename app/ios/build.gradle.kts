plugins {
    alias(libs.plugins.primitive.ios)
    alias(libs.plugins.primitive.compose)
}

kotlin {
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
