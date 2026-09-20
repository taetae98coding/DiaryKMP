plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.data.core)
                implementation(projects.data.place)
                implementation(projects.data.tag)
                implementation(projects.data.web)
                implementation(projects.domain.memo)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
