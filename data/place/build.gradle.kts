plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.googleNetwork.api)
                implementation(projects.core.naverNetwork.api)
                implementation(projects.data.core)
                implementation(projects.data.tag)
                implementation(projects.domain.place)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
