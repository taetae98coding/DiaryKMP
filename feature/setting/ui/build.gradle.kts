plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.file)
                implementation(projects.domain.holiday)
                implementation(projects.domain.mcp)
                implementation(projects.domain.setting)
                implementation(projects.feature.setting.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.feature.memo.api)
                implementation(projects.feature.more.api)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
