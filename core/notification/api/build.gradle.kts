plugins {
    alias(libs.plugins.primitive.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)
            }
        }
    }
}
