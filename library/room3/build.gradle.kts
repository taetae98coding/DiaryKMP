plugins {
    alias(libs.plugins.primitive.multiplatform)
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
