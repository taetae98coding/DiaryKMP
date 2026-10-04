plugins {
    alias(libs.plugins.convention.data)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.geminiNetwork.api)
                implementation(projects.data.core)
                implementation(projects.domain.memo)
                implementation(projects.domain.setting)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
