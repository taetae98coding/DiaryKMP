plugins {
    alias(libs.plugins.convention.domain)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.domain.routine)
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
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
