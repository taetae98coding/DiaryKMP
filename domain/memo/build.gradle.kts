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
                implementation(projects.logger.crashlytics.api)
                api(libs.androidx.paging.common)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(projects.core.testing)
            }
        }
    }
}
