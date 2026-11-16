plugins {
    alias(libs.plugins.convention.work)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.checklist)
                implementation(projects.domain.contact)
                implementation(projects.domain.dday)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.playlist)
                implementation(projects.domain.qr)
                implementation(projects.domain.routine)
                implementation(projects.domain.search)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(libs.androidx.paging.common)
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
