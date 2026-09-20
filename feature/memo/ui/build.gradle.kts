plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.place)
                implementation(projects.compose.tag)
                implementation(projects.domain.location)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.setting)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.library.composeUi)
                implementation(projects.library.coroutines)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.web.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
