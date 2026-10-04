plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.library.applicationSupport)
                implementation(projects.library.webkit)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
