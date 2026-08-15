plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
                implementation(projects.library.coroutines)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
