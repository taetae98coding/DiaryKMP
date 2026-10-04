plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.room)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.room3)
                implementation(libs.androidx.room3.paging)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(libs.androidx.room3.testing)
                implementation(projects.core.testing)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.applicationSupport)
            }
        }

        iosMain {
            dependencies {
                implementation(projects.library.applicationSupport)
            }
        }

        nonWasmMain {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}
