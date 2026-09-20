plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.file.api)
                implementation(projects.core.network.api)
                implementation(projects.core.supabase.api)
                implementation(projects.domain.account)
            }
        }
    }
}
