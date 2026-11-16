plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.memo)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.feature.core)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.more.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidMain {
            dependencies {
                implementation(projects.core.webCookie.api)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.webkit)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
