package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.CorruptionException
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okio.Buffer
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SettingSerializerTest :
    FunSpec({
        test("지도 설정을 왕복 변환한다") {
            val settings =
                listOf(
                    MapSettingData(),
                    MapSettingData(defaultProvider = MapProviderLocalEntity.NAVER.persistentValue),
                    MapSettingData(defaultProvider = MapProviderLocalEntity.GOOGLE.persistentValue),
                )

            settings.forEach { setting ->
                MapSettingSerializer.roundTrip(setting) shouldBe setting
            }
        }

        test("공휴일 설정을 왕복 변환한다") {
            val settings =
                listOf(
                    HolidaySettingData(),
                    HolidaySettingData(hiddenKeySet = setOf("초복")),
                    HolidaySettingData(hiddenKeySet = setOf("초복", "중복", "제헌절")),
                )

            settings.forEach { setting ->
                HolidaySettingSerializer.roundTrip(setting) shouldBe setting
            }
        }

        test("Gemini 설정을 왕복 변환한다") {
            val settings =
                listOf(
                    GeminiSettingLocalEntity(),
                    GeminiSettingLocalEntity(apiKey = "storedApiKey", model = "models/gemini-flash", systemPrompt = "첫 줄\n둘째 줄"),
                    GeminiSettingLocalEntity(systemPrompt = "지시문만 있는 설정"),
                )

            settings.forEach { setting ->
                GeminiSettingSerializer.roundTrip(setting) shouldBe setting
            }
        }

        test("저장된 항목이 없으면 기본 설정을 제공한다") {
            MapSettingSerializer.readText("{}") shouldBe MapSettingData()
            HolidaySettingSerializer.readText("{}") shouldBe HolidaySettingData()
            GeminiSettingSerializer.readText("{}") shouldBe GeminiSettingLocalEntity()
        }

        test("모르는 설정 항목이 있어도 아는 항목을 읽는다") {
            MapSettingSerializer.readText("""{"defaultProvider":"google","unknownSetting":true}""") shouldBe
                MapSettingData(defaultProvider = "google")
            GeminiSettingSerializer.readText("""{"apiKey":"key","unknownSetting":true}""") shouldBe
                GeminiSettingLocalEntity(apiKey = "key")
        }

        test("각 설정은 다른 역할의 항목을 읽지 않는다") {
            val mixed = """{"defaultProvider":"google","hiddenKeySet":["초복"],"apiKey":"key"}"""

            MapSettingSerializer.readText(mixed) shouldBe MapSettingData(defaultProvider = "google")
            HolidaySettingSerializer.readText(mixed) shouldBe HolidaySettingData(hiddenKeySet = setOf("초복"))
            GeminiSettingSerializer.readText(mixed) shouldBe GeminiSettingLocalEntity(apiKey = "key")
        }

        test("계정별 마지막 동기화 시각을 왕복 변환한다") {
            val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
            val settings =
                listOf(
                    SyncTimeData(),
                    SyncTimeData(syncedAtMap = mapOf(fixtureMonkey.giveMeOne<Uuid>() to fixtureMonkey.giveMeOne<Instant>())),
                    SyncTimeData(
                        syncedAtMap =
                            List(3) { fixtureMonkey.giveMeOne<Uuid>() to fixtureMonkey.giveMeOne<Instant>() }.toMap(),
                    ),
                )

            settings.forEach { setting ->
                SyncTimeSerializer.roundTrip(setting) shouldBe setting
            }
        }

        test("설정을 해석할 수 없으면 손상으로 알린다") {
            val corruptedTexts = listOf("", "  ", "not a setting")

            corruptedTexts.forEach { text ->
                shouldThrowExactly<CorruptionException> { MapSettingSerializer.readText(text) }
                shouldThrowExactly<CorruptionException> { HolidaySettingSerializer.readText(text) }
                shouldThrowExactly<CorruptionException> { GeminiSettingSerializer.readText(text) }
            }

            shouldThrowExactly<CorruptionException> { MapSettingSerializer.readText("""{"defaultProvider":1}""") }
            shouldThrowExactly<CorruptionException> { HolidaySettingSerializer.readText("""{"hiddenKeySet":1}""") }
            shouldThrowExactly<CorruptionException> { GeminiSettingSerializer.readText("""{"apiKey":1}""") }
            shouldThrowExactly<CorruptionException> { SyncTimeSerializer.readText("""{"syncedAtMap":1}""") }
        }
    })

private suspend fun <T> SettingSerializer<T>.roundTrip(setting: T): T {
    val buffer = Buffer()

    writeTo(setting, buffer)

    return readFrom(buffer)
}

private suspend fun <T> SettingSerializer<T>.readText(text: String): T = readFrom(Buffer().apply { writeUtf8(text) })
