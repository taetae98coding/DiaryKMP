plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
