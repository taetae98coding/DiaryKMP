plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.browser)
                implementation(projects.domain.holiday)
                implementation(projects.domain.playlist)
                implementation(projects.domain.setting)
                implementation(projects.feature.setting.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.feature.more.api)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }
    }
}
