plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.webNetwork.api)
                implementation(projects.library.ktor)

                implementation(ktorLibs.client.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
            }
        }
    }
}
