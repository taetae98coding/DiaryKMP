plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    sourceSets {
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.common)
                implementation(libs.google.play.integrity)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }
    }
}
