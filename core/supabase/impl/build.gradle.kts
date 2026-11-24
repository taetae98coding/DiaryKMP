plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.library.ktor)
                api(libs.supabase.functions)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
                implementation(projects.core.network.api)
                implementation(projects.logger.crashlytics.api)
            }
        }
    }
}
