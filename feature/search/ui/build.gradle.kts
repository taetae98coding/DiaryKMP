plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(projects.compose.memo)
                implementation(projects.compose.place)
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.search)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
                implementation(projects.library.coroutines)

                implementation(libs.androidx.paging.compose)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
