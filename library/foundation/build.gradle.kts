plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.primitive.kotlin)
    alias(libs.plugins.primitive.kotest)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.objc)
            }
        }
    }
}

// macOS 시스템 프레임워크를 java.lang.foreign으로 직접 부르는 바인딩이라 다른 OS에는 검증할 대상이 없다.
tasks.named<Test>("jvmTest") {
    val isMacOs = providers.systemProperty("os.name").map { it.startsWith("Mac") }.getOrElse(false)
    onlyIf("macOS 시스템 프레임워크가 필요하다") { isMacOs }
}
