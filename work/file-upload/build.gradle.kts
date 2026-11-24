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
                implementation(projects.domain.file)
                implementation(projects.library.coroutines)
                implementation(projects.library.kotlin)
                implementation(projects.library.locale)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.kotlinx.io.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.work.runtime)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.work.testing)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
