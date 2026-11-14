package io.github.taetae98coding.diary.core.network.impl.routine.entity

import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineCycleRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutinePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRecordRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.TimeZone

private val CYCLE_VALUE_LIST: List<Pair<RoutineCycleRemoteEntity, String>> =
    listOf(
        RoutineCycleRemoteEntity.DAILY to "FREQ=DAILY",
        RoutineCycleRemoteEntity.WEEKDAYS to "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR",
        RoutineCycleRemoteEntity.WEEKENDS to "FREQ=DAILY;BYDAY=SA,SU",
        RoutineCycleRemoteEntity.WEEKLY to "FREQ=WEEKLY;WKST=SU",
        RoutineCycleRemoteEntity.MONTHLY to "FREQ=MONTHLY",
    )

class RoutinePushRequestRemoteEntityTest :
    FunSpec({
        test("루틴 업로드 요청은 루틴이 가지는 값을 빠짐없이 담는다") {
            val encoded = encodeRoutine(routine = Arb.routineRemoteEntity().next())

            encoded.keys shouldBe ROUTINE_KEY_SET
            encoded.getValue("detail").jsonObject.keys shouldBe DETAIL_KEY_SET
        }

        test("루틴 업로드 요청은 빈 문자열과 null인 값으로도 모든 키를 담는다") {
            val routine =
                Arb.routineRemoteEntity().next().let { routine ->
                    routine.copy(detail = routine.detail.copy(title = "", description = "", goalAmount = null), isFinished = false, isDeleted = false)
                }

            val encoded = encodeRoutine(routine = routine)

            encoded.keys shouldBe ROUTINE_KEY_SET
            encoded.getValue("detail").jsonObject.keys shouldBe DETAIL_KEY_SET
        }

        test("루틴 업로드 요청은 루틴을 마치는 날짜를 담지 않는다") {
            val encoded = encodeRoutine(routine = Arb.routineRemoteEntity().next())

            encoded.keys shouldNotContain "endDate"
            encoded.getValue("detail").jsonObject.keys shouldNotContain "endDate"
        }

        listOf(
            Triple(2000, false, JsonPrimitive(2000)),
            Triple(null, true, JsonNull),
        ).forEach { (goalAmount, isFinished, expectedGoalAmount) ->
            test("TC-02485cfa-c629-468a-a468-06d1efb26fab 목표량 ${goalAmount ?: "없음"}, 완료 여부 ${isFinished}인 루틴 요청은 전체 상태와 연·월·일 날짜를 담는다") {
                withTimeZone("Asia/Seoul") {
                    val routine =
                        Arb.routineRemoteEntity().next().let { value ->
                            value.copy(detail = value.detail.copy(startDate = LocalDate(2026, 3, 1), goalAmount = goalAmount), isFinished = isFinished)
                        }

                    val encoded = encodeRoutine(routine = routine)
                    val detail = encoded.getValue("detail").jsonObject

                    detail.getValue("title") shouldBe JsonPrimitive(routine.detail.title)
                    detail.getValue("description") shouldBe JsonPrimitive(routine.detail.description)
                    detail.getValue("color") shouldBe JsonPrimitive(routine.detail.color)
                    detail.getValue("amountPerRecord") shouldBe JsonPrimitive(routine.detail.amountPerRecord)
                    detail.getValue("startDate") shouldBe JsonPrimitive("2026-03-01")
                    detail.getValue("goalAmount") shouldBe expectedGoalAmount
                    encoded.getValue("isFinished") shouldBe JsonPrimitive(isFinished)
                    encoded.getValue("isDeleted") shouldBe JsonPrimitive(routine.isDeleted)
                }
            }
        }

        test("TC-483afe78-01ae-44e6-b1e9-8deca12073b7 루틴 요청의 주기는 정해진 값으로 글자의 대소문자와 순서까지 같게 담긴다") {
            CYCLE_VALUE_LIST.forEach { (cycle, value) ->
                val routine = Arb.routineRemoteEntity().next().let { routine -> routine.copy(detail = routine.detail.copy(cycle = cycle)) }

                encodeRoutine(routine = routine).getValue("detail").jsonObject.getValue("cycle") shouldBe JsonPrimitive(value)
            }
        }

        test("TC-6f442d59-63b6-4f58-b781-7060329ecdce 주고받는 주기 값은 다시 같은 주기로 읽힌다") {
            CYCLE_VALUE_LIST.forEach { (cycle, value) ->
                supabaseRequestJson.encodeToJsonElement(cycle) shouldBe JsonPrimitive(value)
                supabaseRequestJson.decodeFromString<RoutineCycleRemoteEntity>("\"$value\"") shouldBe cycle
            }
            RoutineCycleRemoteEntity.entries.size shouldBe CYCLE_VALUE_LIST.size
        }

        test("TC-b0f6d809-7328-44d8-8bf4-1d62c8eb366c 정해진 다섯 값이 아닌 반복 규칙은 루틴의 주기로 읽지 않는다") {
            listOf(
                "freq=daily",
                "Freq=Monthly",
                "WKST=SU;FREQ=WEEKLY",
                "FREQ=WEEKLY",
                "FREQ=WEEKLY;WKST=MO",
                "FREQ=WEEKLY;INTERVAL=2;WKST=SU",
                "FREQ=WEEKLY;WKST=SU;BYDAY=MO,WE",
                "FREQ=DAILY;BYDAY=MO,WE,FR",
                "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR,SA",
                "FREQ=DAILY;BYDAY=SU,SA",
                "FREQ=DAILY;BYDAY=FR,TH,WE,TU,MO",
                "BYDAY=SA,SU;FREQ=DAILY",
                "FREQ=DAILY;BYDAY=sa,su",
                "FREQ=DAILY;BYDAY=MO, TU, WE, TH, FR",
                "FREQ=WEEKLY;WKST=SU;BYDAY=SA,SU",
                "FREQ=DAILY;INTERVAL=2",
                "FREQ=YEARLY",
                "",
            ).forEach { value ->
                runCatching { supabaseRequestJson.decodeFromString<RoutineCycleRemoteEntity>("\"$value\"") }.isFailure shouldBe true
            }
        }

        test("TC-06a7ffb0-3d16-48fd-a03f-423fae4bc4e3 루틴 요청의 단위는 적은 글자 그대로 담기고 빈 단위는 빈 글자로 담긴다") {
            listOf("회", "ml", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val routine = Arb.routineRemoteEntity().next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }

                encodeRoutine(routine = routine).getValue("detail").jsonObject.getValue("unit") shouldBe JsonPrimitive(unit)
            }
        }

        test("TC-4534eddf-89a0-4740-a48a-fd3accd33b78 응답의 단위는 확인하지 않고 글자 그대로 읽는다") {
            listOf("회", "km", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val routine = Arb.routineRemoteEntity().next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }
                val responseText = supabaseRequestJson.encodeToJsonElement(RoutinePullResponseRemoteEntity(routineList = listOf(RoutinePullRemoteEntity(routine = routine, usn = 11L)))).toString()

                val pull = supabaseRequestJson.decodeFromString<RoutinePullResponseRemoteEntity>(responseText).routineList.single()

                pull.routine.detail.unit shouldBe unit
                pull.usn shouldBe 11L
            }
        }

        test("TC-bfcaced9-f529-491c-a839-d267b96601aa 루틴 기록 업로드 요청은 속한 루틴, 날짜, 양, 삭제 여부와 시각을 빠짐없이 담는다") {
            val request = RoutineRecordPushRequestRemoteEntity(routineRecordList = listOf(Arb.routineRecordRemoteEntity().next()))

            supabaseRequestJson
                .encodeToJsonElement(request)
                .jsonObject
                .getValue("routineRecordList")
                .jsonArray
                .single()
                .jsonObject
                .keys shouldBe setOf("id", "routineId", "date", "amount", "isDeleted", "updatedAt", "createdAt")
        }

        test("TC-89458657-5b6f-4db1-8dbf-f5dd41305c9f 루틴 기록 요청은 날짜를 시각과 시간대 없는 연·월·일로, 양을 수로 담는다") {
            withTimeZone("Asia/Seoul") {
                val record = Arb.routineRecordRemoteEntity().next().copy(date = LocalDate(2026, 3, 1), amount = 300)

                val encoded =
                    supabaseRequestJson
                        .encodeToJsonElement(RoutineRecordPushRequestRemoteEntity(routineRecordList = listOf(record)))
                        .jsonObject
                        .getValue("routineRecordList")
                        .jsonArray
                        .single()
                        .jsonObject

                encoded.getValue("date") shouldBe JsonPrimitive("2026-03-01")
                encoded.getValue("amount") shouldBe JsonPrimitive(300)
            }
        }

        listOf("UTC", "Asia/Seoul", "Pacific/Honolulu").forEach { timeZoneId ->
            test("TC-d9851777-4709-4023-9a95-513efbdf274b $timeZoneId 시간대에서도 응답의 루틴과 루틴 기록 날짜를 같은 연·월·일로 읽는다") {
                withTimeZone(timeZoneId) {
                    val routine =
                        Arb.routineRemoteEntity().next().let { value ->
                            value.copy(detail = value.detail.copy(startDate = LocalDate(2026, 3, 1)))
                        }
                    val record = Arb.routineRecordRemoteEntity().next().copy(routineId = routine.id, date = LocalDate(2026, 3, 1))
                    val routineText =
                        supabaseRequestJson.encodeToJsonElement(RoutinePullResponseRemoteEntity(routineList = listOf(RoutinePullRemoteEntity(routine = routine, usn = 1L)))).toString()
                    val recordText =
                        supabaseRequestJson
                            .encodeToJsonElement(RoutineRecordPullResponseRemoteEntity(routineRecordList = listOf(RoutineRecordPullRemoteEntity(routineRecord = record, usn = 1L))))
                            .toString()
                    routineText shouldContain """"startDate":"2026-03-01""""
                    recordText shouldContain """"date":"2026-03-01""""

                    val decodedRoutine =
                        supabaseRequestJson
                            .decodeFromString<RoutinePullResponseRemoteEntity>(routineText)
                            .routineList
                            .single()
                            .routine
                    val decodedRecord =
                        supabaseRequestJson
                            .decodeFromString<RoutineRecordPullResponseRemoteEntity>(recordText)
                            .routineRecordList
                            .single()
                            .routineRecord

                    decodedRoutine.detail.startDate shouldBe LocalDate(2026, 3, 1)
                    decodedRecord.date shouldBe LocalDate(2026, 3, 1)
                }
            }
        }

        listOf(
            "주기 FREQ=YEARLY" to (""""cycle":"FREQ=MONTHLY"""" to """"cycle":"FREQ=YEARLY""""),
            "주기 FREQ=WEEKLY" to (""""cycle":"FREQ=MONTHLY"""" to """"cycle":"FREQ=WEEKLY""""),
            "주기 FREQ=DAILY;BYDAY=MO,WE,FR" to (""""cycle":"FREQ=MONTHLY"""" to """"cycle":"FREQ=DAILY;BYDAY=MO,WE,FR""""),
            "주기 FREQ=DAILY;BYDAY=SU,SA" to (""""cycle":"FREQ=MONTHLY"""" to """"cycle":"FREQ=DAILY;BYDAY=SU,SA""""),
            "주기 freq=daily;byday=sa,su" to (""""cycle":"FREQ=MONTHLY"""" to """"cycle":"freq=daily;byday=sa,su""""),
        ).forEach { (name, replacement) ->
            test("TC-c7c8b058-c682-4007-9209-94574e097147 $name 인 루틴이 섞인 응답은 같은 묶음의 루틴까지 읽지 않는다") {
                val valid =
                    Arb.routineRemoteEntity().next().let { routine ->
                        routine.copy(detail = routine.detail.copy(title = "A", description = "", cycle = RoutineCycleRemoteEntity.DAILY, unit = "회"))
                    }
                val invalid =
                    Arb.routineRemoteEntity().next().let { routine ->
                        routine.copy(detail = routine.detail.copy(title = "B", description = "", cycle = RoutineCycleRemoteEntity.MONTHLY, unit = "m"))
                    }
                val responseText =
                    supabaseRequestJson
                        .encodeToJsonElement(
                            RoutinePullResponseRemoteEntity(
                                routineList = listOf(RoutinePullRemoteEntity(routine = valid, usn = 11L), RoutinePullRemoteEntity(routine = invalid, usn = 12L)),
                            ),
                        ).toString()
                supabaseRequestJson.decodeFromString<RoutinePullResponseRemoteEntity>(responseText).routineList.size shouldBe 2

                runCatching { supabaseRequestJson.decodeFromString<RoutinePullResponseRemoteEntity>(responseText.replace(replacement.first, replacement.second)) }.isFailure shouldBe true
            }
        }

        test("루틴과 루틴 기록은 JSON으로 바꿨다가 되돌리면 같은 값이 된다") {
            val routine = Arb.routineRemoteEntity().next()
            val record = Arb.routineRecordRemoteEntity().next()

            supabaseRequestJson.decodeFromString<RoutineRemoteEntity>(supabaseRequestJson.encodeToJsonElement(routine).toString()) shouldBe routine
            supabaseRequestJson.decodeFromString<RoutineRecordRemoteEntity>(supabaseRequestJson.encodeToJsonElement(record).toString()) shouldBe record
        }
    })

private val supabaseRequestJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

private val ROUTINE_KEY_SET: Set<String> = setOf("id", "detail", "isFinished", "isDeleted", "updatedAt", "createdAt")

private val DETAIL_KEY_SET: Set<String> =
    setOf("title", "description", "color", "startDate", "cycle", "unit", "goalAmount", "amountPerRecord")

private fun encodeRoutine(routine: RoutineRemoteEntity): JsonObject =
    supabaseRequestJson
        .encodeToJsonElement(RoutinePushRequestRemoteEntity(routineList = listOf(routine)))
        .jsonObject
        .getValue("routineList")
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
