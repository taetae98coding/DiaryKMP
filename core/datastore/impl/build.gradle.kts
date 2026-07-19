plugins {
    alias(libs.plugins.primitive.multiplatform.android.library)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.datastore.api)
                implementation(libs.androidx.datastore.core.okio)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
