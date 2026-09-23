plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.android.host.test)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
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
