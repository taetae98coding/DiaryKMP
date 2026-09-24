plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.calendarNetwork.api)
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
