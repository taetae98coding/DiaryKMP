plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
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
