plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.browser)
                implementation(projects.domain.file)
                implementation(projects.domain.holiday)
                implementation(projects.domain.playlist)
                implementation(projects.domain.setting)
                implementation(projects.feature.setting.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(projects.feature.more.api)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }
    }
}
