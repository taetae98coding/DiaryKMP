plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.credential)
                implementation(projects.domain.account)
                implementation(projects.domain.sync)
                implementation(projects.feature.account.api)
                implementation(projects.logger.crashlytics.api)

                implementation(libs.coil.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.logger.core)
                implementation(libs.coil.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
