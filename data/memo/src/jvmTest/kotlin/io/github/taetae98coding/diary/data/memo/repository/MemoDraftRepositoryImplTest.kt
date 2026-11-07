package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftGeneration
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.core.testing.gemini.geminiSetting
import io.github.taetae98coding.diary.core.testing.memo.memoDateTime
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.exception.GeminiRequestException
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val json = Json

private const val LONG_PROMPT_LINE_COUNT = 500
private const val LONG_PROMPT_LINE_LENGTH = 100

private fun newRequest(): MemoDraftRequest =
    MemoDraftRequest(
        prompt = Arb.nonBlankString().next(),
        title = Arb.nonBlankString().next(),
        description = Arb.nonBlankString().next(),
        dateTime = Arb.memoDateTime().next(),
        now = Arb.kotlinxLocalDateTime().next(),
        timeZone = TimeZone.of(Arb.element("Asia/Seoul", "UTC", "America/New_York").next()),
    )

class MemoDraftRepositoryImplTest :
    FunSpec({
        test("TC-9b073a1f-64e9-4bb1-be62-c93c520d2f8b: 전달받은 설정의 인증 정보와 모델, 시스템 프롬프트로 생성을 요청한다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
            val captured = fetchCapturing(setting = setting, request = request)

            captured.apiKey shouldBe setting.apiKey
            captured.model shouldBe setting.model
            captured.systemInstruction shouldBe setting.systemPrompt
        }

        test("TC-3a3766f6-143b-423d-a9ad-bc65853f66ef: 저장된 시스템 프롬프트를 지시문을 더하지 않고 그대로 보낸다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
            val systemPrompt = "  첫 줄 지시\n\n둘째 줄 지시  "

            fetchCapturing(setting = setting.copy(systemPrompt = systemPrompt), request = request).systemInstruction shouldBe systemPrompt
        }

        test("TC-7fbc6869-e13c-4bd8-9965-0e2292c8d0ba: 조각 사이 대기를 2분으로 요청한다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
            fetchCapturing(setting = setting, request = request).idleTimeout shouldBe 2.minutes
        }

        test("TC-5491e9af-67be-4bf7-bc56-76eb638b6889: 프롬프트와 입력 중인 내용, 지금 시각과 시간대를 함께 보낸다") {
            val request = newRequest()
            val prompt = fetchPrompt(request = request)

            prompt["prompt"]!!.jsonPrimitive.content shouldBe request.prompt
            prompt["currentTitle"]!!.jsonPrimitive.content shouldBe request.title
            prompt["currentDescription"]!!.jsonPrimitive.content shouldBe request.description
            prompt["now"]!!.jsonObject["date"]!!.jsonPrimitive.content shouldBe request.now.date.toString()
            prompt["now"]!!.jsonObject["time"]!!.jsonObject.hourMinute() shouldBe (request.now.hour to request.now.minute)
            prompt["timeZone"]!!.jsonPrimitive.content shouldBe request.timeZone.id
            prompt.containsKey("currentPeriod") shouldBe true
        }

        test("TC-fb71ce81-78c5-4d0d-a7c9-a9364c32e760: 요청 시점의 요일과 분 단위 시각을 보낸다") {
            val request = newRequest()
            val prompt = fetchPrompt(request = request.copy(now = LocalDateTime(2026, 11, 7, 9, 41, 27, 123_000_000)))
            val now = prompt["now"]!!.jsonObject

            now["date"]!!.jsonPrimitive.content shouldBe "2026-11-07"
            now["dayOfWeek"]!!.jsonPrimitive.content shouldBe "Saturday"
            now["time"]!!.jsonObject.keys shouldContainExactlyInAnyOrder setOf("hour", "minute")
            now["time"]!!.jsonObject.hourMinute() shouldBe (9 to 41)
            prompt["timeZone"]!!.jsonPrimitive.content shouldBe request.timeZone.id
        }

        test("요일을 상수 이름이 아닌 정해진 이름으로 보낸다") {
            val request = newRequest()
            val expected = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

            (2..8).map { day ->
                fetchPrompt(request = request.copy(now = LocalDateTime(2026, 11, day, 0, 0)))["now"]!!.jsonObject["dayOfWeek"]!!.jsonPrimitive.content
            } shouldContainExactly expected
        }

        test("TC-c6e6c244-85be-4b0e-881b-fe240117050c: 지금 입력 중인 기간을 정해진 형식으로 보낸다") {
            val request = newRequest()
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
            val request = newRequest()
            listOf("", "   ").forEach { blank ->
                val prompt = fetchPrompt(request = request.copy(prompt = blank))

                prompt.containsKey("prompt") shouldBe false
            }
        }

        test("TC-52cc9cbb-b5c0-4819-94ef-2f5838293b74: 줄바꿈이 여러 번 들어 있는 긴 프롬프트도 줄이지 않고 그대로 보낸다") {
            val request = newRequest()
            val longPrompt = (1..LONG_PROMPT_LINE_COUNT).joinToString(separator = "\n") { index -> "$index 번째 줄 ${"가".repeat(LONG_PROMPT_LINE_LENGTH)}" }

            fetchPrompt(request = request.copy(prompt = longPrompt))["prompt"]!!.jsonPrimitive.content shouldBe longPrompt
        }

        test("TC-3a215098-861f-40ad-bfe8-477228e2ac6e: 지금 시각과 시간대, 프롬프트와 입력 중인 내용 말고는 보내지 않는다") {
            val request = newRequest()
            fetchPrompt(request = request).keys shouldContainExactlyInAnyOrder
                setOf("prompt", "currentTitle", "currentDescription", "currentPeriod", "now", "timeZone")

            fetchPrompt(request = request.copy(prompt = "", title = "", description = "", dateTime = null)).keys shouldContainExactlyInAnyOrder
                setOf("now", "timeZone")
        }

        test("TC-43233fcb-7378-457d-af25-6c044c85e6e4: 공백뿐인 제목과 설명, 사용하지 않는 기간은 보내지 않는다") {
            val request = newRequest()
            listOf("", "   ", "\n\t").forEach { blank ->
                val prompt = fetchPrompt(request = request.copy(title = blank, description = blank, dateTime = null))

                prompt.containsKey("currentTitle") shouldBe false
                prompt.containsKey("currentDescription") shouldBe false
                prompt.containsKey("currentPeriod") shouldBe false
            }
        }

        test("TC-a8dc3fba-4eef-42da-9c37-022c1c1d2007: 결과를 받을 구조와 기간의 형식을 요청에 지정한다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
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

        test("TC-b2c73e80-0370-41e4-849b-c578276c14f2: 여러 조각을 이어 붙인 전체를 결과로 삼는다") {
            val content =
                buildJsonObject {
                    put("title", "주간 회고")
                    put("description", "## 이번 주\n- 한 일")
                    put("period", period(allDay = true, startDate = "2026-11-07", endDate = "2026-11-08"))
                }.toString()
            val chunkList = content.chunked(size = 5)

            val actual = fetchGenerationList(chunkFlow = flowOf(*chunkList.toTypedArray())).completedDraft()

            actual.title shouldBe "주간 회고"
            actual.description shouldBe "## 이번 주\n- 한 일"
            actual.dateTime shouldBe MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 7)..LocalDate(2026, 11, 8))
        }

        test("TC-11c3ed9d-2f48-41c6-8682-ddb76b0b5379: 조각을 받을 때마다 그때까지 이어 붙인 제목과 설명을 알린다") {
            val actual =
                fetchGenerationList(
                    chunkFlow = flowOf("""{"title":"주간""", """ 회고","desc""", """ription":"## 이번""", """ 주"}"""),
                )

            actual.progressList() shouldContainExactly
                listOf(
                    MemoDraftProgress(title = "주간", description = ""),
                    MemoDraftProgress(title = "주간 회고", description = ""),
                    MemoDraftProgress(title = "주간 회고", description = "## 이번"),
                    MemoDraftProgress(title = "주간 회고", description = "## 이번 주"),
                )
            actual.last() shouldBe MemoDraftGeneration.Completed(draft = MemoDraft(title = "주간 회고", description = "## 이번 주", dateTime = null))
        }

        test("바뀐 내용이 없는 조각에서는 진행을 다시 알리지 않는다") {
            val actual = fetchGenerationList(chunkFlow = flowOf("""{"title":"제목"""", ",", " ", """"period":null}"""))

            actual.progressList() shouldContainExactly listOf(MemoDraftProgress(title = "제목", description = ""))
        }

        test("TC-9a7e13eb-a822-47f3-812b-adc76fbc39d3: 조각을 받는 도중 실패하면 결과 없이 실패를 알린다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
            val failure = GeminiException.Network(cause = IllegalStateException("connection reset"))
            val chunkFlow =
                flow {
                    emit("""{"title":"주간""")
                    throw failure
                }
            val generationList = mutableListOf<MemoDraftGeneration>()

            shouldThrow<GeminiRequestException.Network> {
                repositoryStreaming(chunkFlow = chunkFlow).stream(setting = setting, request = request).collect { generation -> generationList += generation }
            }.cause shouldBe failure
            generationList.filterIsInstance<MemoDraftGeneration.Completed>() shouldBe emptyList()
        }

        test("TC-cafafa06-f52a-4988-ab6e-164c6d4fd04b: 원격 실패를 실패 이유를 나눌 수 있는 도메인 실패로 바꿔 알린다") {
            val setting = Arb.geminiSetting().next()
            val request = newRequest()
            val description = "Request contains an invalid argument."
            listOf<Pair<Throwable, (Throwable) -> Unit>>(
                GeminiException.InvalidApiKey(cause = null) to { it.shouldBeInstanceOf<GeminiApiKeyInvalidException>() },
                GeminiException.QuotaExceeded(cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.QuotaExceeded>() },
                GeminiException.ModelNotFound(cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.ModelNotFound>() },
                GeminiException.Blocked(detail = "blockReason=SAFETY") to { it.shouldBeInstanceOf<GeminiRequestException.Blocked>() },
                GeminiException.Truncated(detail = "finishReason=MAX_TOKENS") to { it.shouldBeInstanceOf<GeminiRequestException.Truncated>() },
                GeminiException.Timeout(cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.Timeout>() },
                GeminiException.Network(cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.Network>() },
                GeminiException.Server(cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.Server>() },
                GeminiException.Other(description = description, cause = null) to { it.shouldBeInstanceOf<GeminiRequestException.Other>().description shouldBe description },
            ).forEach { (cause, assertFailure) ->
                val actual = shouldThrow<Exception> { repositoryThrowing(cause).stream(setting = setting, request = request).toList() }

                assertFailure(actual)
                actual.cause shouldBe cause
            }
        }

        test("TC-659c06e0-8cc6-4e57-8cbc-be3ed3159758: 이어 붙인 전체가 구조화된 형태가 아니면 설명 없는 그 밖의 실패로 알린다") {
            listOf(
                emptyList(),
                listOf("생성된 ", "문장입니다"),
                listOf("[1, ", "2]"),
                listOf("""{"title":"끊긴"""),
            ).forEach { chunkList ->
                val actual =
                    shouldThrow<GeminiRequestException.Other> {
                        fetchGenerationList(chunkFlow = flowOf(*chunkList.toTypedArray()))
                    }

                actual.description shouldBe ""
                actual.cause.shouldBeInstanceOf<GeminiException.InvalidContent>()
            }
        }
    })

private data class CapturedRequest(
    val apiKey: String,
    val model: String,
    val systemInstruction: String,
    val prompt: String,
    val responseJsonSchema: JsonObject,
    val idleTimeout: Duration,
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
    val timeoutSlot = slot<Duration>()
    val remoteDataSource =
        mockk<GeminiContentRemoteDataSource> {
            every {
                streamStructuredContent(
                    apiKey = capture(apiKeySlot),
                    model = capture(modelSlot),
                    systemInstruction = capture(systemInstructionSlot),
                    prompt = capture(promptSlot),
                    responseJsonSchema = capture(schemaSlot),
                    idleTimeout = capture(timeoutSlot),
                )
            } returns flowOf("{}")
        }

    MemoDraftRepositoryImpl(geminiContentRemoteDataSource = remoteDataSource).stream(setting = setting, request = request).toList()

    return CapturedRequest(
        apiKey = apiKeySlot.captured,
        model = modelSlot.captured,
        systemInstruction = systemInstructionSlot.captured,
        prompt = promptSlot.captured,
        responseJsonSchema = schemaSlot.captured,
        idleTimeout = timeoutSlot.captured,
    )
}

private suspend fun fetchPrompt(request: MemoDraftRequest): JsonObject = json.parseToJsonElement(fetchCapturing(setting = Arb.geminiSetting().next(), request = request).prompt).jsonObject

private suspend fun fetchDraft(content: JsonObject) = fetchGenerationList(chunkFlow = flowOf(content.toString())).completedDraft()

private suspend fun fetchGenerationList(chunkFlow: Flow<String>): List<MemoDraftGeneration> =
    repositoryStreaming(chunkFlow = chunkFlow)
        .stream(setting = Arb.geminiSetting().next(), request = newRequest())
        .toList()

private fun repositoryStreaming(chunkFlow: Flow<String>): MemoDraftRepositoryImpl =
    MemoDraftRepositoryImpl(
        geminiContentRemoteDataSource =
            mockk {
                every { streamStructuredContent(any(), any(), any(), any(), any(), any()) } returns chunkFlow
            },
    )

private fun repositoryThrowing(cause: Throwable): MemoDraftRepositoryImpl = repositoryStreaming(chunkFlow = flow { throw cause })

private fun List<MemoDraftGeneration>.completedDraft(): MemoDraft = filterIsInstance<MemoDraftGeneration.Completed>().single().draft

private fun List<MemoDraftGeneration>.progressList(): List<MemoDraftProgress> = filterIsInstance<MemoDraftGeneration.InProgress>().map { generation -> generation.progress }

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
