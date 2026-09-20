plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.android.host.test)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.memo)
                implementation(projects.notification)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.work.runtime)
                implementation(libs.koin.androidx.workmanager)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.work.testing)
            }
        }

        jvmWasmMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
