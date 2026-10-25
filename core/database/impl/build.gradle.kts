plugins {
    alias(libs.plugins.convention.core.impl)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.room)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.library.room3)
                implementation(libs.androidx.room3.paging)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(libs.androidx.room3.testing)
                implementation(projects.core.testing)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.library.applicationSupport)
            }
        }

        iosMain {
            dependencies {
                implementation(projects.library.applicationSupport)
            }
        }

        nonWasmMain {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}

ksp {
    // Room이 생성하는 스키마 검증 함수가 suspend 상태 기계로 커져 JVM 메서드 크기 한도(64KB)를 넘으므로, 기본값 300보다 잘게 나눈다.
    arg("room.validationSplitSize", "100")
}
