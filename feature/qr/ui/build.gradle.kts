plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.permission)
                implementation(projects.domain.qr)
                implementation(projects.domain.sync)
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

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.paging.testing)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.avfoundation)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(npm("jsqr", libs.versions.jsqr.get()))
            }
        }
    }
}
