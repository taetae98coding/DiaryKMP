plugins {
    alias(libs.plugins.convention.work)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.datastore.api)
                implementation(projects.domain.checklist)
                implementation(projects.domain.contact)
                implementation(projects.domain.dday)
                implementation(projects.domain.holiday)
                implementation(projects.domain.lunar)
                implementation(projects.domain.mcp)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.playlist)
                implementation(projects.domain.qr)
                implementation(projects.domain.routine)
                implementation(projects.domain.search)
                implementation(projects.domain.tag)
                implementation(projects.domain.web)
                implementation(projects.library.coroutines)
                implementation(projects.work.appaction)
                implementation(libs.androidx.paging.common)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core.testing)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.mcp.kotlin.sdk.server)
                implementation(ktorLibs.server.cio)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.androidx.paging.testing)
                implementation(ktorLibs.client.cio)
                implementation(ktorLibs.server.testHost)
            }
        }
    }
}
