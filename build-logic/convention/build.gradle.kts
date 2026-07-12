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
    compileOnly(libs.gradle.plugin.detekt)
    compileOnly(libs.gradle.plugin.koin.compiler)
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
        register("primitiveComposeResources") {
            id = "io.github.taetae98coding.diary.primitive.compose.resources"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.ComposeResourcesPrimitivePlugin"
        }
        register("primitiveComposePreview") {
            id = "io.github.taetae98coding.diary.primitive.compose.preview"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.ComposePreviewPrimitivePlugin"
        }
        register("primitiveIos") {
            id = "io.github.taetae98coding.diary.primitive.ios"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.IosPrimitivePlugin"
        }
        register("primitiveJvm") {
            id = "io.github.taetae98coding.diary.primitive.jvm"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.JvmPrimitivePlugin"
        }
        register("primitiveKoin") {
            id = "io.github.taetae98coding.diary.primitive.koin"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KoinPrimitivePlugin"
        }
        register("primitiveKotest") {
            id = "io.github.taetae98coding.diary.primitive.kotest"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin"
        }
        register("primitiveKotlinProject") {
            id = "io.github.taetae98coding.diary.primitive.kotlin.project"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.KotlinProjectPrimitivePlugin"
        }
        register("primitiveMultiplatform") {
            id = "io.github.taetae98coding.diary.primitive.multiplatform"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.MultiplatformPrimitivePlugin"
        }
        register("primitiveMultiplatformAndroidLibrary") {
            id = "io.github.taetae98coding.diary.primitive.multiplatform.android.library"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.MultiplatformAndroidLibraryPrimitivePlugin"
        }
        register("primitiveWasm") {
            id = "io.github.taetae98coding.diary.primitive.wasm"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.primitive.WasmPrimitivePlugin"
        }
        register("conventionDomain") {
            id = "io.github.taetae98coding.diary.convention.domain"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.DomainConventionPlugin"
        }
        register("conventionData") {
            id = "io.github.taetae98coding.diary.convention.data"
            implementationClass = "io.github.taetae98coding.diary.buildlogic.convention.DataConventionPlugin"
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
