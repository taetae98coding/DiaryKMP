@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.convention.work)
    alias(libs.plugins.primitive.android.host.test)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.domain.checklist)
                implementation(projects.domain.contact)
                implementation(projects.domain.dday)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.playlist)
                implementation(projects.domain.qr)
                implementation(projects.domain.search)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(libs.androidx.paging.common)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        androidMain {
            // AppFunction 컴파일러가 서비스 설명 XML을 KSP 리소스(assets/)로 만들지만, KMP Android 라이브러리는 KSP 리소스를 패키징하지 않으므로 직접 등록한다.
            resources.srcDir(files(layout.buildDirectory.dir("generated/ksp/android/androidMain/resources")).builtBy("kspAndroidMain"))

            dependencies {
                implementation(libs.androidx.appfunctions)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.androidx.appfunctions.testing)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.appfunctions.compiler)
}

// Android 16.0은 v1 형식 XML만 색인하고 그 뒤 버전은 v2 형식을 읽으므로 두 형식을 함께 만든다.
ksp {
    arg("appfunctions:generateV1Xml", "true")
}
