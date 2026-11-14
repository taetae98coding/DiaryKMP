plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.dday)
                implementation(projects.compose.list)
                implementation(projects.compose.memo)
                implementation(projects.compose.place)
                implementation(projects.compose.routine)
                implementation(projects.compose.tag)
                implementation(projects.compose.web)
                implementation(projects.domain.dday)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.routine)
                implementation(projects.domain.search)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.routine.api)
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

        androidHostTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }
    }
}
