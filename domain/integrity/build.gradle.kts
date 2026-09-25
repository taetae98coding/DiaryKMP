plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.logger.analytics.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
