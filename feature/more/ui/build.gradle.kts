plugins {
    alias(libs.plugins.convention.feature.ui)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.account)
                implementation(projects.domain.sync)
                implementation(projects.feature.checklist.api)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.file.api)
                implementation(projects.feature.holiday.api)
                implementation(projects.feature.login.api)
                implementation(projects.feature.more.api)
                implementation(projects.feature.place.api)
                implementation(projects.feature.playlist.api)
                implementation(projects.feature.qr.api)
                implementation(projects.feature.search.api)
                implementation(projects.feature.setting.api)
                implementation(projects.feature.web.api)

                implementation(libs.coil.compose)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
                implementation(libs.coil.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
