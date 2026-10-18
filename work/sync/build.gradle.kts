plugins {
    alias(libs.plugins.convention.work)
    alias(libs.plugins.primitive.android.host.test)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.database.api)
                implementation(projects.core.datastore.api)
                implementation(projects.core.network.api)
                implementation(projects.domain.account)
                implementation(projects.domain.sync)
                implementation(projects.library.coroutines)
                implementation(projects.logger.crashlytics.api)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.work.runtime)
                implementation(libs.koin.androidx.workmanager)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.work.testing)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.core.testing)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
