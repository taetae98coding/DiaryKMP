plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.credential)
                implementation(projects.domain.account)
                implementation(projects.feature.login.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.logger.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
