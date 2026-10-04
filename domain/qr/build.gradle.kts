plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.domain.sync)
                api(libs.androidx.paging.common)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
