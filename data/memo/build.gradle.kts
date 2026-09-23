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
                implementation(projects.data.contact)
                implementation(projects.data.core)
                implementation(projects.data.place)
                implementation(projects.data.tag)
                implementation(projects.data.web)
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
