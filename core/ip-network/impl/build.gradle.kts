plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.ipNetwork.api)
                implementation(projects.library.ktor)

                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.serialization.kotlinx.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
            }
        }
    }
}
