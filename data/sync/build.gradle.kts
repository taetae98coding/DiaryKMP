plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.network.api)
                implementation(projects.core.work.api)
                implementation(projects.domain.sync)
                implementation(projects.logger.crashlytics.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
