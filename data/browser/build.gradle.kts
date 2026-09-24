plugins {
    alias(libs.plugins.convention.data)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.browserCookie.api)
                implementation(projects.core.datastore.api)
                implementation(projects.domain.browser)
            }
        }
    }
}
