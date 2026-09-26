plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.permission)
                implementation(projects.feature.qr.api)
                implementation(libs.qrose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.compose)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.google.mlkit.barcode.scanning)
            }
        }
    }
}
