package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
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
        test("TC-MEMO-GEMINI-DATA-001: 전달받은 설정의 인증 정보와 모델, 시스템 프롬프트로 생성을 요청한다") {
            val apiKeySlot = slot<String>()
            val modelSlot = slot<String>()
            val systemInstructionSlot = slot<String>()
            val remoteDataSource =
                mockk<GeminiContentRemoteDataSource> {
                    coEvery {
                        generateStructuredContent(
                            apiKey = capture(apiKeySlot),
                            model = capture(modelSlot),
                            systemInstruction = capture(systemInstructionSlot),
                            prompt = any(),
                            responseSchema = any(),
                        )
                    } returns JsonObject(emptyMap())
                }

            MemoDraftRepositoryImpl(geminiContentRemoteDataSource = remoteDataSource).fetch(setting = setting, request = request)

            apiKeySlot.captured shouldBe setting.apiKey
            modelSlot.captured shouldBe setting.model
            systemInstructionSlot.captured shouldBe setting.systemPrompt
        }

        test("TC-MEMO-GEMINI-DATA-002: 프롬프트와 입력 중인 내용, 지금 시각과 시간대를 함께 보낸다") {
            val prompt = fetchPrompt(request = request)

            prompt["prompt"]!!.jsonPrimitive.content shouldBe request.prompt
            prompt["currentTitle"]!!.jsonPrimitive.content shouldBe request.title
            prompt["currentDescription"]!!.jsonPrimitive.content shouldBe request.description
            prompt["now"]!!.jsonPrimitive.content shouldBe "2026-09-20T14:30"
            prompt["timeZone"]!!.jsonPrimitive.content shouldBe "Asia/Seoul"

            val period = prompt["currentPeriod"]!!.jsonObject
            period["allDay"]!!.jsonPrimitive.content shouldBe "true"
            period["start"]!!.jsonPrimitive.content shouldBe "2026-09-21T00:00"
            period["end"]!!.jsonPrimitive.content shouldBe "2026-09-22T00:00"
        }

        test("시간까지 지정한 기간은 시작과 종료의 시각까지 보낸다") {
            val prompt =
                fetchPrompt(
                    request =
                        request.copy(
                            dateTime =
                                MemoDateTime.DateTime(
                                    start = LocalDateTime(2026, 9, 21, 9, 30),
                                    endInclusive = LocalDateTime(2026, 9, 21, 10, 30),
                                ),
                        ),
                )
            val period = prompt["currentPeriod"]!!.jsonObject

            period["allDay"]!!.jsonPrimitive.content shouldBe "false"
            period["start"]!!.jsonPrimitive.content shouldBe "2026-09-21T09:30"
            period["end"]!!.jsonPrimitive.content shouldBe "2026-09-21T10:30"
        }

        test("TC-MEMO-GEMINI-DATA-003: 비어 있는 프롬프트는 보내지 않는다") {
            listOf("", "   ").forEach { blank ->
                val prompt = fetchPrompt(request = request.copy(prompt = blank))

                prompt.containsKey("prompt") shouldBe false
            }
        }

        test("TC-MEMO-GEMINI-DATA-004: 지금 시각과 시간대, 프롬프트와 입력 중인 내용 말고는 보내지 않는다") {
            fetchPrompt(request = request).keys shouldContainExactlyInAnyOrder
                setOf("prompt", "currentTitle", "currentDescription", "currentPeriod", "now", "timeZone")

            fetchPrompt(request = request.copy(prompt = "", title = "", description = "", dateTime = null)).keys shouldContainExactlyInAnyOrder
                setOf("now", "timeZone")
        }

        test("생성 결과를 메모 초안으로 전달한다") {
            val content =
                buildJsonObject {
                    put("title", "주간 회고 정리")
                    put("description", "## 이번 주\n- 한 일")
                    putJsonObject("period") {
                        put("allDay", true)
                        put("start", "2026-09-21T00:00")
                        put("end", "2026-09-22T00:00")
                    }
                }

            val actual = fetchDraft(content = content)

            actual.title shouldBe "주간 회고 정리"
            actual.description shouldBe "## 이번 주\n- 한 일"
            actual.dateTime shouldBe MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22))
        }

        test("시간까지 지정한 기간을 그대로 전달한다") {
            val content =
                buildJsonObject {
                    put("title", "회의")
                    put("description", "")
                    putJsonObject("period") {
                        put("allDay", false)
                        put("start", "2026-09-21T09:30")
                        put("end", "2026-09-21T10:30")
                    }
                }

            fetchDraft(content = content).dateTime shouldBe
                MemoDateTime.DateTime(
                    start = LocalDateTime(2026, 9, 21, 9, 30),
                    endInclusive = LocalDateTime(2026, 9, 21, 10, 30),
                )
        }

        test("기간을 읽을 수 없으면 기간 없는 초안으로 전달한다") {
            listOf(
                buildJsonObject { put("title", "제목") },
                buildJsonObject {
                    put("title", "제목")
                    put("period", "2026-09-21")
                },
                buildJsonObject {
                    put("title", "제목")
                    putJsonObject("period") {
                        put("allDay", true)
                        put("start", "내일")
                        put("end", "모레")
                    }
                },
                buildJsonObject {
                    put("title", "제목")
                    putJsonObject("period") {
                        put("allDay", true)
                        put("start", "2026-09-21T00:00")
                    }
                },
            ).forEach { content ->
                fetchDraft(content = content).dateTime shouldBe null
            }
        }

        test("제목과 설명이 없으면 빈 값으로 전달한다") {
            val actual = fetchDraft(content = JsonObject(emptyMap()))

            actual.title shouldBe ""
            actual.description shouldBe ""
        }

        test("인증 실패를 화면이 구분할 수 있는 도메인 실패로 바꿔 알린다") {
            val cause = GeminiException.InvalidApiKey(cause = IllegalStateException("unauthorized"))
            val repository =
                MemoDraftRepositoryImpl(
                    geminiContentRemoteDataSource =
                        mockk {
                            coEvery { generateStructuredContent(any(), any(), any(), any(), any()) } throws cause
                        },
                )

            shouldThrow<GeminiApiKeyInvalidException> { repository.fetch(setting = setting, request = request) }.cause shouldBe cause
        }

        test("TC-MEMO-GEMINI-DATA-005: 구조를 지키지 않은 응답의 실패는 인증 실패로 바꾸지 않는다") {
            val cause = GeminiException.InvalidContent(cause = IllegalStateException("not json"))
            val repository =
                MemoDraftRepositoryImpl(
                    geminiContentRemoteDataSource =
                        mockk {
                            coEvery { generateStructuredContent(any(), any(), any(), any(), any()) } throws cause
                        },
                )

            shouldThrow<GeminiException.InvalidContent> { repository.fetch(setting = setting, request = request) } shouldBe cause
        }
    })

private suspend fun fetchPrompt(request: MemoDraftRequest): JsonObject {
    val promptSlot = slot<String>()
    val remoteDataSource =
        mockk<GeminiContentRemoteDataSource> {
            coEvery {
                generateStructuredContent(
                    apiKey = any(),
                    model = any(),
                    systemInstruction = any(),
                    prompt = capture(promptSlot),
                    responseSchema = any(),
                )
            } returns JsonObject(emptyMap())
        }

    MemoDraftRepositoryImpl(geminiContentRemoteDataSource = remoteDataSource).fetch(setting = setting, request = request)

    return json.parseToJsonElement(promptSlot.captured).jsonObject
}

private suspend fun fetchDraft(content: JsonObject) =
    MemoDraftRepositoryImpl(
        geminiContentRemoteDataSource =
            mockk {
                coEvery { generateStructuredContent(any(), any(), any(), any(), any()) } returns content
            },
    ).fetch(setting = setting, request = request)
