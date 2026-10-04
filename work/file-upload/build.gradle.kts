plugins {
    alias(libs.plugins.convention.work)
    alias(libs.plugins.primitive.android.host.test)
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
                implementation(projects.core.navigation)
                implementation(projects.domain.account)
                implementation(projects.domain.file)
                implementation(projects.library.coroutines)
                implementation(projects.library.kotlin)
                implementation(projects.library.locale)
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
                implementation(projects.core.testing)
                implementation(libs.androidx.work.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
