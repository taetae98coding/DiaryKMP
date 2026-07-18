plugins {
    alias(libs.plugins.primitive.jvm)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.fixture.monkey.kotlin)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
