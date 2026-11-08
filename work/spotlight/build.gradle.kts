plugins {
    alias(libs.plugins.convention.work)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.navigation)
                implementation(projects.domain.account)
                implementation(projects.library.coroutines)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
