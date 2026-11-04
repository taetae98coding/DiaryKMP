plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        jvmTest {
            dependencies {
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
