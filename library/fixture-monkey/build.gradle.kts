plugins {
    alias(libs.plugins.primitive.android.library)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.fixture.monkey.kotlin)
                api(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

tasks.withType<Test>().matching { it.name == "jvmTest" }.configureEach {
    useJUnitPlatform()
}
