plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.domain.setting)
                implementation(projects.domain.sync)
                api(libs.androidx.paging.common)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.logger.crashlytics.api)
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
