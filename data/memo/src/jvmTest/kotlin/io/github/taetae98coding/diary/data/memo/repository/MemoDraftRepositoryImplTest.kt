package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private val json = Json

private val setting =
    GeminiSetting(
        apiKey = "testApiKey",
        model = "models/gemini-flash",
        systemPrompt = "testSystemPrompt",
    )

private val request =
    MemoDraftRequest(
        prompt = "회고를 써 줘",
        title = "주간 회고",
        description = "이번 주 정리",
        dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
        now = LocalDateTime(2026, 9, 20, 14, 30),
        timeZone = TimeZone.of("Asia/Seoul"),
    )

class MemoDraftRepositoryImplTest :
    FunSpec({
        test("TC-9b073a1f-64e9-4bb1-be62-c93c520d2f8b: 전달받은 설정의 인증 정보와 모델, 시스템 프롬프트로 생성을 요청한다") {
            val captured = fetchCapturing(setting = setting, request = request)

            captured.apiKey shouldBe setting.apiKey
            captured.model shouldBe setting.model
            captured.systemInstruction shouldBe setting.systemPrompt
        }

        test("TC-3a3766f6-143b-423d-a9ad-bc65853f66ef: 저장된 시스템 프롬프트를 지시문을 더하지 않고 그대로 보낸다") {
            val systemPrompt = "  첫 줄 지시\n\n둘째 줄 지시  "

            fetchCapturing(setting = setting.copy(systemPrompt = systemPrompt), request = request).systemInstruction shouldBe systemPrompt
        }

        test("TC-5491e9af-67be-4bf7-bc56-76eb638b6889: 프롬프트와 입력 중인 내용, 지금 시각과 시간대를 함께 보낸다") {
            val prompt = fetchPrompt(request = request)

            prompt["prompt"]!!.jsonPrimitive.content shouldBe request.prompt
            prompt["currentTitle"]!!.jsonPrimitive.content shouldBe request.title
            prompt["currentDescription"]!!.jsonPrimitive.content shouldBe request.description
            prompt["now"]!!.jsonObject["date"]!!.jsonPrimitive.content shouldBe "2026-09-20"
            prompt["now"]!!.jsonObject["time"]!!.jsonObject.hourMinute() shouldBe (14 to 30)
            prompt["timeZone"]!!.jsonPrimitive.content shouldBe "Asia/Seoul"
            prompt.containsKey("currentPeriod") shouldBe true
        }

        test("TC-fb71ce81-78c5-4d0d-a7c9-a9364c32e760: 요청 시점의 요일과 분 단위 시각을 보낸다") {
            val now = fetchPrompt(request = request.copy(now = LocalDateTime(2026, 11, 7, 9, 41, 27, 123_000_000)))["now"]!!.jsonObject

            now["date"]!!.jsonPrimitive.content shouldBe "2026-11-07"
            now["dayOfWeek"]!!.jsonPrimitive.content shouldBe "Saturday"
            now["time"]!!.jsonObject.keys shouldContainExactlyInAnyOrder setOf("hour", "minute")
            now["time"]!!.jsonObject.hourMinute() shouldBe (9 to 41)
        }

        test("요일을 상수 이름이 아닌 정해진 이름으로 보낸다") {
            val expected = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

            (2..8).map { day ->
                fetchPrompt(request = request.copy(now = LocalDateTime(2026, 11, day, 0, 0)))["now"]!!.jsonObject["dayOfWeek"]!!.jsonPrimitive.content
            } shouldContainExactly expected
        }

        test("TC-c6e6c244-85be-4b0e-881b-fe240117050c: 지금 입력 중인 기간을 정해진 형식으로 보낸다") {
            val allDay =
                fetchPrompt(
                    request = request.copy(dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 7)..LocalDate(2026, 11, 8))),
                )["currentPeriod"]!!.jsonObject

            allDay["allDay"]!!.jsonPrimitive.content shouldBe "true"
            allDay["startDate"]!!.jsonPrimitive.content shouldBe "2026-11-07"
            allDay["endDate"]!!.jsonPrimitive.content shouldBe "2026-11-08"
            allDay.containsKey("startTime") shouldBe false
            allDay.containsKey("endTime") shouldBe false

            val dateTime =
                fetchPrompt(
                    request =
                        request.copy(
                            dateTime =
                                MemoDateTime.DateTime(
                                    start = LocalDateTime(2026, 11, 7, 9, 30),
                                    endInclusive = LocalDateTime(2026, 11, 7, 18, 0),
                                ),
                        ),
                )["currentPeriod"]!!.jsonObject

            dateTime["allDay"]!!.jsonPrimitive.content shouldBe "false"
            dateTime["startDate"]!!.jsonPrimitive.content shouldBe "2026-11-07"
            dateTime["endDate"]!!.jsonPrimitive.content shouldBe "2026-11-07"
            dateTime["startTime"]!!.jsonObject.hourMinute() shouldBe (9 to 30)
            dateTime["endTime"]!!.jsonObject.hourMinute() shouldBe (18 to 0)
        }

        test("TC-0ea03cb6-5be1-4857-915b-b2aaf75e2102: 비어 있는 프롬프트는 보내지 않는다") {
            listOf("", "   ").forEach { blank ->
                val prompt = fetchPrompt(request = request.copy(prompt = blank))

                prompt.containsKey("prompt") shouldBe false
            }
        }

        test("TC-3a215098-861f-40ad-bfe8-477228e2ac6e: 지금 시각과 시간대, 프롬프트와 입력 중인 내용 말고는 보내지 않는다") {
            fetchPrompt(request = request).keys shouldContainExactlyInAnyOrder
                setOf("prompt", "currentTitle", "currentDescription", "currentPeriod", "now", "timeZone")

            fetchPrompt(request = request.copy(prompt = "", title = "", description = "", dateTime = null)).keys shouldContainExactlyInAnyOrder
                setOf("now", "timeZone")
        }

        test("TC-43233fcb-7378-457d-af25-6c044c85e6e4: 공백뿐인 제목과 설명, 사용하지 않는 기간은 보내지 않는다") {
            listOf("", "   ", "\n\t").forEach { blank ->
                val prompt = fetchPrompt(request = request.copy(title = blank, description = blank, dateTime = null))

                prompt.containsKey("currentTitle") shouldBe false
                prompt.containsKey("currentDescription") shouldBe false
                prompt.containsKey("currentPeriod") shouldBe false
            }
        }

        test("TC-a8dc3fba-4eef-42da-9c37-022c1c1d2007: 결과를 받을 구조와 기간의 형식을 요청에 지정한다") {
            val schema = fetchCapturing(setting = setting, request = request).responseJsonSchema

            schema.typeList() shouldBe listOf("object")
            schema.requiredList() shouldContainExactlyInAnyOrder listOf("title", "description", "period")
            schema.property("title").typeList() shouldBe listOf("string")
            schema.property("description").typeList() shouldBe listOf("string")

            val period = schema.property("period")
            period.typeList() shouldContainExactlyInAnyOrder listOf("object", "null")
            period.requiredList() shouldContainExactlyInAnyOrder listOf("allDay", "startDate", "endDate")
            period.property("allDay").typeList() shouldBe listOf("boolean")
            listOf("startDate", "endDate").forEach { key ->
                period.property(key).typeList() shouldBe listOf("string")
                period.property(key)["format"]!!.jsonPrimitive.content shouldBe "date"
            }
            listOf("startTime", "endTime").forEach { key ->
                val time = period.property(key)
                time.typeList() shouldContainExactlyInAnyOrder listOf("object", "null")
                time.requiredList() shouldContainExactlyInAnyOrder listOf("hour", "minute")
                time.property("hour").range() shouldBe (0 to 23)
                time.property("minute").range() shouldBe (0 to 59)
            }
        }

        test("생성 결과의 제목과 설명을 메모 초안으로 전달한다") {
            val content =
                buildJsonObject {
                    put("title", "주간 회고 정리")
                    put("description", "## 이번 주\n- 한 일")
                    put("period", JsonNull)
                }

            val actual = fetchDraft(content = content)

            actual.title shouldBe "주간 회고 정리"
            actual.description shouldBe "## 이번 주\n- 한 일"
            actual.dateTime shouldBe null
        }

        test("TC-aa58d097-62b0-4b98-922b-07f011287e18: 결과의 기간을 정해진 형식대로 해석한다") {
            listOf(
                period(allDay = true, startDate = "2026-11-07", endDate = "2026-11-07") to
                    MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 7)..LocalDate(2026, 11, 7)),
                period(allDay = true, startDate = "2026-11-07", endDate = "2026-11-08") to
                    MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 7)..LocalDate(2026, 11, 8)),
                period(allDay = true, startDate = "2026-11-07", endDate = "2026-11-07", startTime = time(9, 0), endTime = time(10, 0)) to
                    MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 7)..LocalDate(2026, 11, 7)),
                period(allDay = false, startDate = "2026-11-07", endDate = "2026-11-07", startTime = time(9, 30), endTime = time(18, 0)) to
                    MemoDateTime.DateTime(start = LocalDateTime(2026, 11, 7, 9, 30), endInclusive = LocalDateTime(2026, 11, 7, 18, 0)),
            ).forEach { (period, expected) ->
                fetchDraft(content = buildJsonObject { put("period", period) }).dateTime shouldBe expected
            }
        }

        test("TC-77bce896-4eb1-4bd8-851f-9ef400d157e6: 기간을 알아볼 수 없으면 기간 없는 초안으로 전달한다") {
            listOf(
                // 기간이 빠져 있다
                buildJsonObject { put("title", "제목") },
                // 기간이 시작일과 종료일로 나뉘어 있지 않다
                buildJsonObject { put("period", "2026-09-21") },
                // 시작일이나 종료일이 없다
                buildJsonObject { put("period", period(allDay = true, startDate = "2026-09-21", endDate = null)) },
                buildJsonObject { put("period", period(allDay = true, startDate = null, endDate = "2026-09-21")) },
                // 시작일이나 종료일이 연-월-일 형식의 날짜로 읽히지 않는다
                buildJsonObject { put("period", period(allDay = true, startDate = "내일", endDate = "모레")) },
                buildJsonObject { put("period", period(allDay = true, startDate = "2026-09-21T00:00", endDate = "2026-09-22T00:00")) },
                // 종일 기간이 아닌데 시작 시각이나 종료 시각이 없다
                buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21", startTime = time(9, 0), endTime = null)) },
                buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21")) },
                // 시각의 시나 분이 범위를 벗어난다
                buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21", startTime = time(24, 0), endTime = time(10, 0))) },
                buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21", startTime = time(9, 0), endTime = time(10, 60))) },
                buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21", startTime = time(-1, 0), endTime = time(10, 0))) },
            ).forEach { content ->
                fetchDraft(content = content).dateTime shouldBe null
            }
        }

        test("시와 분이 숫자가 아닌 글이면 알아볼 수 없는 시각으로 본다") {
            val textTime =
                buildJsonObject {
                    put("hour", "9")
                    put("minute", "0")
                }

            fetchDraft(
                content = buildJsonObject { put("period", period(allDay = false, startDate = "2026-09-21", endDate = "2026-09-21", startTime = textTime, endTime = time(10, 0))) },
            ).dateTime shouldBe null
        }

        test("TC-77bce896-4eb1-4bd8-851f-9ef400d157e6: 제목과 설명을 알아볼 수 없으면 빈 값으로 전달한다") {
            listOf(
                JsonObject(emptyMap()),
                buildJsonObject {
                    put("title", 1)
                    put("description", true)
                },
                buildJsonObject {
                    putJsonObject("title") {}
                    putJsonArray("description") {}
                },
            ).forEach { content ->
                val actual = fetchDraft(content = content)

                actual.title shouldBe ""
                actual.description shouldBe ""
            }
        }


        test("인증 실패를 화면이 구분할 수 있는 도메인 실패로 바꿔 알린다") {
            val cause = GeminiException.InvalidApiKey(cause = IllegalStateException("unauthorized"))
            val repository = repositoryThrowing(cause)

            shouldThrow<GeminiApiKeyInvalidException> { repository.fetch(setting = setting, request = request) }.cause shouldBe cause
        }

        test("TC-659c06e0-8cc6-4e57-8cbc-be3ed3159758: 구조화된 형태가 아닌 응답의 실패는 인증 실패로 바꾸지 않는다") {
            val cause = GeminiException.InvalidContent(cause = IllegalStateException("not json"))
            val repository = repositoryThrowing(cause)

            shouldThrow<GeminiException.InvalidContent> { repository.fetch(setting = setting, request = request) } shouldBe cause
        }
    })

private data class CapturedRequest(
    val apiKey: String,
    val model: String,
    val systemInstruction: String,
    val prompt: String,
    val responseJsonSchema: JsonObject,
)

private suspend fun fetchCapturing(
    setting: GeminiSetting,
    request: MemoDraftRequest,
): CapturedRequest {
    val apiKeySlot = slot<String>()
    val modelSlot = slot<String>()
    val systemInstructionSlot = slot<String>()
    val promptSlot = slot<String>()
    val schemaSlot = slot<JsonObject>()
    val remoteDataSource =
        mockk<GeminiContentRemoteDataSource> {
            coEvery {
                generateStructuredContent(
                    apiKey = capture(apiKeySlot),
                    model = capture(modelSlot),
                    systemInstruction = capture(systemInstructionSlot),
                    prompt = capture(promptSlot),
                    responseJsonSchema = capture(schemaSlot),
                )
            } returns JsonObject(emptyMap())
        }

    MemoDraftRepositoryImpl(geminiContentRemoteDataSource = remoteDataSource).fetch(setting = setting, request = request)

    return CapturedRequest(
        apiKey = apiKeySlot.captured,
        model = modelSlot.captured,
        systemInstruction = systemInstructionSlot.captured,
        prompt = promptSlot.captured,
        responseJsonSchema = schemaSlot.captured,
    )
}

private suspend fun fetchPrompt(request: MemoDraftRequest): JsonObject = json.parseToJsonElement(fetchCapturing(setting = setting, request = request).prompt).jsonObject

private suspend fun fetchDraft(content: JsonObject) =
    MemoDraftRepositoryImpl(
        geminiContentRemoteDataSource =
            mockk {
                coEvery { generateStructuredContent(any(), any(), any(), any(), any()) } returns content
            },
    ).fetch(setting = setting, request = request)

private fun repositoryThrowing(cause: Throwable): MemoDraftRepositoryImpl =
    MemoDraftRepositoryImpl(
        geminiContentRemoteDataSource =
            mockk {
                coEvery { generateStructuredContent(any(), any(), any(), any(), any()) } throws cause
            },
    )

private fun period(
    allDay: Boolean,
    startDate: String?,
    endDate: String?,
    startTime: JsonObject? = null,
    endTime: JsonObject? = null,
): JsonObject =
    buildJsonObject {
        put("allDay", allDay)
        startDate?.let { value -> put("startDate", value) }
        endDate?.let { value -> put("endDate", value) }
        startTime?.let { value -> put("startTime", value) }
        endTime?.let { value -> put("endTime", value) }
    }

private fun time(
    hour: Int,
    minute: Int,
): JsonObject =
    buildJsonObject {
        put("hour", hour)
        put("minute", minute)
    }

private fun JsonObject.hourMinute(): Pair<Int, Int> = this["hour"]!!.jsonPrimitive.int to this["minute"]!!.jsonPrimitive.int

private fun JsonObject.property(key: String): JsonObject = this["properties"]!!.jsonObject[key]!!.jsonObject

private fun JsonObject.typeList(): List<String> =
    when (val type: JsonElement = this["type"]!!) {
        is JsonArray -> type.map { element -> element.jsonPrimitive.content }
        is JsonPrimitive -> listOf(type.content)
        else -> error("type is not a string or an array")
    }

private fun JsonObject.requiredList(): List<String> = this["required"]!!.jsonArray.map { element -> element.jsonPrimitive.content }

private fun JsonObject.range(): Pair<Int, Int> {
    typeList() shouldBe listOf("integer")

    return this["minimum"]!!.jsonPrimitive.int to this["maximum"]!!.jsonPrimitive.int
}
