plugins {
    alias(libs.plugins.convention.core.impl)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.ktor)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
            }
        }
    }
}
