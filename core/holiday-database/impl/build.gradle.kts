plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.primitive.room)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.holidayDatabase.api)
                implementation(projects.library.room3)
                implementation(libs.androidx.room3.runtime)
            }
        }

        jvmMain {
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
