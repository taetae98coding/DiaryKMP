plugins {
    `kotlin-dsl`
}

kotlin {
    explicitApi()
}

dependencies {
    compileOnly(libs.gradle.plugin.kotlin)
    compileOnly(libs.gradle.plugin.kotlin.compose.compiler)
    compileOnly(libs.gradle.plugin.kotlin.serialization)
    compileOnly(libs.gradle.plugin.jetbrains.compose)
    compileOnly(libs.gradle.plugin.android)
    compileOnly(libs.gradle.plugin.androidx.room3)
    compileOnly(libs.gradle.plugin.detekt)
    compileOnly(libs.gradle.plugin.koin.compiler)
    compileOnly(libs.gradle.plugin.ksp)
}

gradlePlugin {
    plugins {
        register("primitiveAndroidApplication") {
            id = "io.github.taetae98coding.diary.primitive.android.application"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.AndroidApplicationPrimitivePlugin"
        }
        register("primitiveAndroidHostTest") {
            id = "io.github.taetae98coding.diary.primitive.android.host.test"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.AndroidHostTestPrimitivePlugin"
        }
        register("primitiveAndroidLibrary") {
            id = "io.github.taetae98coding.diary.primitive.android.library"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.AndroidLibraryPrimitivePlugin"
        }
        register("primitiveCompose") {
            id = "io.github.taetae98coding.diary.primitive.compose"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.ComposePrimitivePlugin"
        }
        register("primitiveComposeTest") {
            id = "io.github.taetae98coding.diary.primitive.compose.test"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.ComposeTestPrimitivePlugin"
        }
        register("primitiveKmp") {
            id = "io.github.taetae98coding.diary.primitive.kmp"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin"
        }
        register("primitiveKoin") {
            id = "io.github.taetae98coding.diary.primitive.koin"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KoinPrimitivePlugin"
        }
        register("primitiveKotest") {
            id = "io.github.taetae98coding.diary.primitive.kotest"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin"
        }
        register("primitiveKotlin") {
            id = "io.github.taetae98coding.diary.primitive.kotlin"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KotlinPrimitivePlugin"
        }
        register("primitiveRoom") {
            id = "io.github.taetae98coding.diary.primitive.room"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.RoomPrimitivePlugin"
        }
        register("conventionCompose") {
            id = "io.github.taetae98coding.diary.convention.compose"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.ComposeConventionPlugin"
        }
        register("conventionData") {
            id = "io.github.taetae98coding.diary.convention.data"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.DataConventionPlugin"
        }
        register("conventionDomain") {
            id = "io.github.taetae98coding.diary.convention.domain"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.DomainConventionPlugin"
        }
        register("conventionFeatureApi") {
            id = "io.github.taetae98coding.diary.convention.feature.api"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.FeatureApiConventionPlugin"
        }
        register("conventionFeatureUi") {
            id = "io.github.taetae98coding.diary.convention.feature.ui"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.FeatureUiConventionPlugin"
        }
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
