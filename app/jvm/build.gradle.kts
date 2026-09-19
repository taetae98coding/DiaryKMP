import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.localProperties
import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.primitive.jvm)
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

// :library:webkit이 FFM으로 objc·WebKit 심볼을 직접 붙이면서 JDK가 제한한 메서드를 호출한다.
// 플래그가 없으면 호출마다 경고가 나오고, 이후 JDK에서는 호출 자체가 막힌다.
private val webkitJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
private val mapJvmArgs =
    listOf(
        "-Dio.github.taetae98coding.diary.naverMapNcpKeyId=${naverMapNcpKeyId.get()}",
        "-Dio.github.taetae98coding.diary.googleMapApiKey=${googleMapApiKey.get()}",
    )
private val runJvmArgs = webkitJvmArgs + mapJvmArgs

kotlin {
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
