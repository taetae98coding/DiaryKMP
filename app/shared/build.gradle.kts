import com.codingfeline.buildkonfig.compiler.FieldSpec
import io.github.taetae98coding.diary.buildlogic.localProperties
import io.github.taetae98coding.diary.buildlogic.namespace

plugins {
    alias(libs.plugins.primitive.kmp)
    alias(libs.plugins.primitive.android.library)
    alias(libs.plugins.primitive.compose)
    alias(libs.plugins.primitive.compose.test)
    alias(libs.plugins.primitive.koin)
    alias(libs.plugins.primitive.kotest)
    alias(libs.plugins.build.konfig)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.compose.core)
                implementation(projects.compose.permission)
                implementation(projects.core.browserCookie.impl)
                implementation(projects.core.database.impl)
                implementation(projects.core.datastore.impl)
                implementation(projects.core.fcm.impl)
                implementation(projects.core.file.impl)
                implementation(projects.core.geminiNetwork.impl)
                implementation(projects.core.googleNetwork.impl)
                implementation(projects.core.holidayDatabase.impl)
                implementation(projects.core.holidayNetwork.impl)
                implementation(projects.core.image.impl)
                implementation(projects.core.ipNetwork.impl)
                implementation(projects.core.location.impl)
                implementation(projects.core.naverNetwork.impl)
                implementation(projects.core.network.impl)
                implementation(projects.core.supabase.impl)
                implementation(projects.core.weatherNetwork.impl)
                implementation(projects.core.webNetwork.impl)
                implementation(projects.core.youtubeNetwork.impl)
                implementation(projects.data.account)
                implementation(projects.data.contact)
                implementation(projects.data.holiday)
                implementation(projects.data.location)
                implementation(projects.data.memo)
                implementation(projects.data.place)
                implementation(projects.data.playlist)
                implementation(projects.data.search)
                implementation(projects.data.setting)
                implementation(projects.data.sync)
                implementation(projects.data.tag)
                implementation(projects.data.weather)
                implementation(projects.data.web)
                implementation(projects.domain.account)
                implementation(projects.domain.contact)
                implementation(projects.domain.holiday)
                implementation(projects.domain.location)
                implementation(projects.domain.memo)
                implementation(projects.domain.place)
                implementation(projects.domain.playlist)
                implementation(projects.domain.search)
                implementation(projects.domain.setting)
                implementation(projects.domain.sync)
                implementation(projects.domain.tag)
                implementation(projects.domain.weather)
                implementation(projects.domain.web)
                implementation(projects.feature.calendar.api)
                implementation(projects.feature.calendar.ui)
                implementation(projects.feature.checklist.api)
                implementation(projects.feature.checklist.ui)
                implementation(projects.feature.contact.api)
                implementation(projects.feature.contact.ui)
                implementation(projects.feature.dday.api)
                implementation(projects.feature.dday.ui)
                implementation(projects.feature.file.api)
                implementation(projects.feature.file.ui)
                implementation(projects.feature.holiday.api)
                implementation(projects.feature.holiday.ui)
                implementation(projects.feature.login.api)
                implementation(projects.feature.login.ui)
                implementation(projects.feature.memo.api)
                implementation(projects.feature.memo.ui)
                implementation(projects.feature.more.api)
                implementation(projects.feature.more.ui)
                implementation(projects.feature.place.api)
                implementation(projects.feature.place.ui)
                implementation(projects.feature.playlist.api)
                implementation(projects.feature.playlist.ui)
                implementation(projects.feature.qr.api)
                implementation(projects.feature.qr.ui)
                implementation(projects.feature.routine.api)
                implementation(projects.feature.routine.ui)
                implementation(projects.feature.search.api)
                implementation(projects.feature.search.ui)
                implementation(projects.feature.setting.api)
                implementation(projects.feature.setting.ui)
                implementation(projects.feature.tag.api)
                implementation(projects.feature.tag.ui)
                implementation(projects.feature.web.api)
                implementation(projects.feature.web.ui)
                implementation(projects.library.coroutines)
                implementation(projects.notification)
                implementation(projects.work.dailyMemo)
                implementation(projects.work.sync)
                implementation(projects.logger.analytics.impl)
                implementation(projects.logger.console.impl)
                implementation(projects.logger.crashlytics.impl)

                implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
                implementation(libs.jetbrains.navigation3.ui)
                implementation(libs.jetbrains.compose.material3.adaptive.navigation.suite)

                implementation(libs.koin.compose.viewmodel)

                runtimeOnly(libs.coil.network.ktor3)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.startup.runtime)
                implementation(libs.koin.androidx.workmanager)
            }
        }

        androidHostTest {
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.coil.network.ktor3)
            }
        }

        jvmMain {
            dependencies {
                runtimeOnly(libs.kotlinx.coroutines.swing)
            }
        }
    }

    compilerOptions {
        // BuildKonfig가 타깃별 값을 expect/actual object로 생성하므로 Beta 경고를 끈다.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

private val localProperties = localProperties()

buildkonfig {
    packageName = namespace()

    defaultConfigs {
    }

    defaultConfigs("dev") {
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "SUPABASE_URL",
            value = localProperties.getProperty("dev.supabase.url"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "SUPABASE_KEY",
            value = localProperties.getProperty("dev.supabase.key"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "OPEN_WEATHER_APP_ID",
            value = localProperties.getProperty("dev.openWeather.appId"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "GOOGLE_PLACES_API_KEY",
            value = localProperties.getProperty("dev.googlePlaces.apiKey"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "NAVER_OPEN_API_CLIENT_ID",
            value = localProperties.getProperty("dev.naverOpenApi.clientId"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "NAVER_OPEN_API_CLIENT_SECRET",
            value = localProperties.getProperty("dev.naverOpenApi.clientSecret"),
            nullable = false,
            const = false,
        )
    }

    defaultConfigs("real") {
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "SUPABASE_URL",
            value = localProperties.getProperty("real.supabase.url"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "SUPABASE_KEY",
            value = localProperties.getProperty("real.supabase.key"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "OPEN_WEATHER_APP_ID",
            value = localProperties.getProperty("real.openWeather.appId"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "GOOGLE_PLACES_API_KEY",
            value = localProperties.getProperty("real.googlePlaces.apiKey"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "NAVER_OPEN_API_CLIENT_ID",
            value = localProperties.getProperty("real.naverOpenApi.clientId"),
            nullable = false,
            const = false,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "NAVER_OPEN_API_CLIENT_SECRET",
            value = localProperties.getProperty("real.naverOpenApi.clientSecret"),
            nullable = false,
            const = false,
        )
    }

    targetConfigs("dev") {
        create("android") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_SERVER_CLIENT_ID",
                value = localProperties.getProperty("dev.android.googleCredentialsServerClientId"),
                nullable = false,
                const = true,
            )
        }

        create("jvm") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "APP_DIRECTORY",
                value = "DiaryDev",
                nullable = false,
                const = true,
            )
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_CLIENT_ID",
                value = localProperties.getProperty("dev.jvm.googleCredentialsClientId"),
                nullable = false,
                const = true,
            )
        }

        create("wasmJs") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_CLIENT_ID",
                value = localProperties.getProperty("dev.wasm.googleCredentialsClientId"),
                nullable = false,
                const = true,
            )
        }
    }

    targetConfigs("real") {
        create("android") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_SERVER_CLIENT_ID",
                value = localProperties.getProperty("real.android.googleCredentialsServerClientId"),
                nullable = false,
                const = true,
            )
        }

        create("jvm") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "APP_DIRECTORY",
                value = "Diary",
                nullable = false,
                const = true,
            )
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_CLIENT_ID",
                value = localProperties.getProperty("real.jvm.googleCredentialsClientId"),
                nullable = false,
                const = true,
            )
        }

        create("wasmJs") {
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = "GOOGLE_CREDENTIALS_CLIENT_ID",
                value = localProperties.getProperty("real.wasm.googleCredentialsClientId"),
                nullable = false,
                const = true,
            )
        }
    }
}
