plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.permission)
                implementation(projects.compose.place)
                implementation(projects.compose.qr)
                implementation(projects.compose.tag)
                implementation(projects.domain.qr)
                implementation(projects.domain.setting)
                implementation(projects.domain.tag)
                implementation(projects.feature.core)
                implementation(projects.feature.qr.api)
                implementation(projects.feature.tag.api)
                implementation(projects.library.kotlin)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
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
                implementation(libs.androidx.paging.testing)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
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
