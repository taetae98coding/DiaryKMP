plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.kotlinxDatetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
