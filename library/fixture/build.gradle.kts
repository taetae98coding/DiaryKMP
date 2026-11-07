plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotest.property)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
