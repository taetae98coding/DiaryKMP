plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(projects.domain.playlist)
                implementation(projects.domain.sync)
                implementation(projects.feature.playlist.api)

                implementation(libs.coil.compose)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
