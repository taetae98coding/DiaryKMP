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
private val appName = buildKonfigFlavor.map { if (it == "dev") "DiaryDev" else "Diary" }
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
        "-Dio.github.taetae98coding.diary.appName=${appName.get()}",
        "-Dio.github.taetae98coding.diary.naverMapNcpKeyId=${naverMapNcpKeyId.get()}",
        "-Dio.github.taetae98coding.diary.googleMapApiKey=${googleMapApiKey.get()}",
    )

// Mac App Store 빌드는 CI가 -Pdiary.mac.appStore=true와 프로비저닝 프로파일 경로를 넘길 때만 켠다.
private val isMacAppStore = providers.gradleProperty("diary.mac.appStore").map(String::toBoolean).orElse(false)
private val macProvisioningProfile = providers.gradleProperty("diary.mac.provisioningProfile")
private val macRuntimeProvisioningProfile = providers.gradleProperty("diary.mac.runtimeProvisioningProfile")
private val macAppResourcesDir = layout.buildDirectory.dir("macAppResources")

// 샌드박스 앱은 번들 밖에 풀어 둔 네이티브 라이브러리를 불러올 수 없으므로, SQLite 드라이버의 라이브러리를 번들 resources로 옮긴다.
private val prepareMacAppResources by tasks.registering(Sync::class) {
    val runtimeClasspath = configurations.named("jvmRuntimeClasspath")

    from(runtimeClasspath.map { classpath -> classpath.filter { file -> file.name.startsWith("sqlite-bundled-jvm") }.map(::zipTree) }) {
        include("natives/osx_arm64/libsqliteJni.dylib")
        eachFile { path = "macos-arm64/$name" }
        includeEmptyDirs = false
    }
    into(macAppResourcesDir)
}

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
        jvmArgs +=
            listOf(
                "--enable-native-access=ALL-UNNAMED",
                "-Dandroidx.sqlite.driver.bundled.path=\$APPDIR/resources",
            )

        // 앱이 리플렉션과 java.lang.foreign 바인딩에 기대므로, ProGuard 규칙을 맞추는 대신 줄이기를 끈다.
        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg)
            packageName = appName.get()
            packageVersion = BuildLogic.VERSION_NAME
            includeAllModules = true
            appResourcesRootDir.set(macAppResourcesDir)

            macOS {
                iconFile.set(layout.projectDirectory.file("icons/diary.icns"))
                bundleID = buildKonfigFlavor.map { if (it == "dev") "${BuildLogic.NAMESPACE}.dev" else BuildLogic.NAMESPACE }.get()
                packageBuildVersion = BuildLogic.VERSION_CODE.toString()
                appCategory = "public.app-category.productivity"
                minimumSystemVersion = "12.0"
                infoPlist {
                    extraKeysRawXml =
                        """
                        <key>NSCameraUsageDescription</key>
                        <string>Allow camera access to scan QR codes.</string>
                        <key>ITSAppUsesNonExemptEncryption</key>
                        <false/>
                        """.trimIndent()
                }

                if (isMacAppStore.get()) {
                    appStore = true
                    entitlementsFile.set(layout.projectDirectory.file("macos/app.entitlements"))
                    runtimeEntitlementsFile.set(layout.projectDirectory.file("macos/runtime.entitlements"))
                    provisioningProfile.set(file(macProvisioningProfile.get()))
                    runtimeProvisioningProfile.set(file(macRuntimeProvisioningProfile.get()))
                }
            }
        }
    }
}

tasks.matching { it.name == "prepareAppResources" }.configureEach {
    dependsOn(prepareMacAppResources)
}

tasks.withType<JavaExec>().matching { it.name == "hotRunJvm" }.configureEach {
    jvmArgs(runJvmArgs)
}
