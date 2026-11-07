plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
