package io.github.taetae98coding.diary.core.network.impl.dday.entity

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.util.TimeZone

class DDayPushRequestRemoteEntityTest :
    FunSpec({
        test("디데이 업로드 요청은 디데이가 가지는 값을 빠짐없이 담는다") {
            val encoded = encodeDDay(dday = Arb.ddayRemoteEntity().next())

            encoded.keys shouldBe DDAY_KEY_SET
            encoded.getValue("detail").jsonObject.keys shouldBe DETAIL_KEY_SET
        }

        listOf(LocalDate(2027, 3, 1) to JsonPrimitive("2027-03-01"), null to JsonNull).forEach { (endDate, expectedEndDate) ->
            test("TC-4db2e680-d3fd-4b84-aa40-5cec0f9a2c53 종료일이 ${endDate ?: "없는"} 디데이 요청은 날짜를 시각과 시간대 없는 연·월·일로 담는다") {
                withTimeZone("Asia/Seoul") {
                    val dday =
                        Arb.ddayRemoteEntity().next().let { value ->
                            value.copy(detail = value.detail.copy(baseDate = LocalDate(2026, 3, 1), endDate = endDate))
                        }

                    val detail = encodeDDay(dday = dday).getValue("detail").jsonObject

                    detail.getValue("baseDate") shouldBe JsonPrimitive("2026-03-01")
                    detail.getValue("endDate") shouldBe expectedEndDate
                }
            }
        }

        test("TC-a17f601a-80cd-4c92-8809-966700ddc88d 반복 간격은 간격마다 days나 months 키 하나만 가진 객체로 담긴다") {
            listOf(
                listOf(DDayRepeatRemoteEntity.Days(days = 100), DDayRepeatRemoteEntity.Months(months = 24)) to
                    listOf(buildJsonObject { put("days", 100) }, buildJsonObject { put("months", 24) }),
                emptyList<DDayRepeatRemoteEntity>() to emptyList(),
            ).forEach { (repeats, expected) ->
                val dday = Arb.ddayRemoteEntity().next().let { value -> value.copy(detail = value.detail.copy(repeats = repeats)) }

                encodeDDay(dday = dday)
                    .getValue("detail")
                    .jsonObject
                    .getValue("repeats")
                    .jsonArray shouldContainExactlyInAnyOrder expected
            }
        }

        listOf("UTC", "Asia/Seoul", "Pacific/Honolulu").forEach { timeZoneId ->
            test("TC-d7fa68b7-e666-44f9-b23a-87aca127608c $timeZoneId 시간대에서도 응답의 날짜를 같은 연·월·일로 읽는다") {
                withTimeZone(timeZoneId) {
                    val dday =
                        Arb.ddayRemoteEntity().next().let { value ->
                            value.copy(detail = value.detail.copy(baseDate = LocalDate(2026, 3, 1), endDate = LocalDate(2026, 12, 31)))
                        }
                    val responseText = supabaseRequestJson.encodeToJsonElement(DDayPullResponseRemoteEntity(ddayList = listOf(DDayPullRemoteEntity(dday = dday, usn = 1L)))).toString()
                    responseText shouldContain """"baseDate":"2026-03-01""""
                    responseText shouldContain """"endDate":"2026-12-31""""

                    val decoded =
                        supabaseRequestJson
                            .decodeFromString<DDayPullResponseRemoteEntity>(responseText)
                            .ddayList
                            .single()
                            .dday

                    decoded.detail.baseDate shouldBe LocalDate(2026, 3, 1)
                    decoded.detail.endDate shouldBe LocalDate(2026, 12, 31)
                }
            }
        }

        // 추가 테스트: 반복 간격의 JSON 왕복과 종류를 가리지 못하는 입력의 거부.
        test("반복 간격은 JSON으로 바꿨다가 되돌리면 같은 간격이 된다") {
            val dday = Arb.ddayRemoteEntity().next()

            supabaseRequestJson.decodeFromString<DDayRemoteEntity>(supabaseRequestJson.encodeToJsonElement(dday).toString()) shouldBe dday
        }

        test("days와 months가 모두 없는 반복 간격은 읽지 않는다") {
            runCatching { supabaseRequestJson.decodeFromString<DDayRepeatRemoteEntity>("{}") }.isFailure shouldBe true
        }
    })

private val supabaseRequestJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

private val DDAY_KEY_SET: Set<String> = setOf("id", "detail", "isDeleted", "updatedAt", "createdAt")

private val DETAIL_KEY_SET: Set<String> =
    setOf("title", "description", "color", "baseDate", "countsBaseDay", "repeats", "endDate", "overflowPolicy")

private fun encodeDDay(dday: DDayRemoteEntity): JsonObject =
    supabaseRequestJson
        .encodeToJsonElement(DDayPushRequestRemoteEntity(ddayList = listOf(dday)))
        .jsonObject
        .getValue("ddayList")
        .jsonArray
        .single()
        .jsonObject

private suspend fun withTimeZone(
    timeZoneId: String,
    block: suspend () -> Unit,
) {
    val defaultTimeZone = TimeZone.getDefault()
    try {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
        block()
    } finally {
        TimeZone.setDefault(defaultTimeZone)
    }
}
