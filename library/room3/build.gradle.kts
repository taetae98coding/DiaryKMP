plugins {
    alias(libs.plugins.primitive.kmp)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.androidx.room3.common)

                api(libs.kotlinx.datetime)
            }
        }
    }
}
