plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.ktor)
                implementation(libs.supabase.functions)
            }
        }

        jvmTest {
            dependencies {
                implementation(ktorLibs.client.mock)
            }
        }
    }
}
