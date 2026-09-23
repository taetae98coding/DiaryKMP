import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.localProperties
import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.primitive.kotlin)
    alias(libs.plugins.primitive.compose)
}

private val buildKonfigFlavor = providers.gradleProperty("buildkonfig.flavor").orElse("dev")
private val naverMapNcpKeyIdProperty = buildKonfigFlavor.map { "$it.naverMapNcpKeyId" }
private val googleMapApiKeyProperty = buildKonfigFlavor.map { "$it.web.googleMapApiKey" }
private val naverMapNcpKeyId =
    naverMapNcpKeyIdProperty.map { propertyName ->
        requireNotNull(localProperties().getProperty(propertyName)) {
            "$propertyName is missing from local.properties"
        }
    }
private val googleMapApiKey =
    googleMapApiKeyProperty.map { propertyName ->
        requireNotNull(localProperties().getProperty(propertyName)) {
            "$propertyName is missing from local.properties"
        }
    }

private val runJvmArgs =
    listOf(
        "-Dio.github.taetae98coding.diary.naverMapNcpKeyId=${naverMapNcpKeyId.get()}",
        "-Dio.github.taetae98coding.diary.googleMapApiKey=${googleMapApiKey.get()}",
    )

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.app.shared)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "${BuildLogic.NAMESPACE}.JvmAppKt"
        jvmArgs += runJvmArgs

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "Diary"
            packageVersion = BuildLogic.VERSION_NAME
            includeAllModules = true

            macOS {
                iconFile.set(layout.projectDirectory.file("icons/diary.icns"))
            }
        }
    }
}

tasks.withType<JavaExec>().matching { it.name == "hotRunJvm" }.configureEach {
    jvmArgs(runJvmArgs)
}
