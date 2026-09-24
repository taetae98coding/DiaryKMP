plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.logger.crashlytics.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
