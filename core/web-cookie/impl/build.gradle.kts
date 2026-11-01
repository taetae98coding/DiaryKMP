plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.host.test)
}

kotlin {
    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.androidx.webkit)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
            }
        }
    }
}
