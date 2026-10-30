package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.domain.memo.repository.MemoDraftRepository
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchMemoDraftUseCaseTest :
    BehaviorSpec({
        Given("Gemini 설정이 저장되어 있다") {
            When("메모 초안을 조회하면") {
                Then("TC-9b073a1f-64e9-4bb1-be62-c93c520d2f8b: 저장된 설정과 입력 중인 내용을 함께 전달한다") {
                    val setting = fixtureMonkey.giveMeOne<GeminiSetting>()
                    val settingSlot = slot<GeminiSetting>()
                    val requestSlot = slot<MemoDraftRequest>()
                    val memoDraftRepository =
                        mockk<MemoDraftRepository> {
                            coEvery { fetch(capture(settingSlot), capture(requestSlot)) } returns MemoDraft.EMPTY
                        }
                    val useCase =
                        createUseCase(
                            setting = setting,
                            memoDraftRepository = memoDraftRepository,
                        )
                    val parameter =
                        FetchMemoDraftUseCase.Parameter(
                            prompt = "회고를 써 줘",
                            title = "주간 회고",
                            description = "이번 주 정리",
                            dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
                        )

                    useCase(parameter = parameter).shouldBeSuccess()

                    settingSlot.captured shouldBe setting
                    requestSlot.captured.prompt shouldBe parameter.prompt
                    requestSlot.captured.title shouldBe parameter.title
                    requestSlot.captured.description shouldBe parameter.description
                    requestSlot.captured.dateTime shouldBe parameter.dateTime
                }

                Then("요청 시점의 날짜·시각과 시간대를 함께 전달한다") {
                    val now = Instant.parse("2026-09-21T05:30:00Z")
                    val requestSlot = slot<MemoDraftRequest>()
                    val memoDraftRepository =
                        mockk<MemoDraftRepository> {
                            coEvery { fetch(any(), capture(requestSlot)) } returns MemoDraft.EMPTY
                        }
                    val useCase =
                        createUseCase(
                            memoDraftRepository = memoDraftRepository,
                            clock = mockk<Clock> { every { this@mockk.now() } returns now },
                        )

                    useCase(parameter = emptyParameter()).shouldBeSuccess()

                    val timeZone = requestSlot.captured.timeZone
                    timeZone shouldBe TimeZone.currentSystemDefault()
                    requestSlot.captured.now shouldBe now.toLocalDateTime(timeZone)
                }
            }
        }

        Given("도우미를 연 뒤 저장된 Gemini 설정이 바뀌었다") {
            When("메모 초안을 조회하면") {
                Then("TC-c5df7d5f-6386-4350-8e19-85d16fc93017: 조회하는 시점에 저장된 설정으로 요청한다") {
                    val settingFlow = MutableStateFlow(fixtureMonkey.giveMeOne<GeminiSetting>())
                    val settingList = mutableListOf<GeminiSetting>()
                    val memoDraftRepository =
                        mockk<MemoDraftRepository> {
                            coEvery { fetch(capture(settingList), any()) } returns MemoDraft.EMPTY
                        }
                    val useCase =
                        FetchMemoDraftUseCase(
                            geminiSettingRepository = mockk { every { get() } returns settingFlow },
                            memoDraftRepository = memoDraftRepository,
                            clock = Clock.System,
                        )
                    useCase(parameter = emptyParameter()).shouldBeSuccess()
                    val changedSetting = fixtureMonkey.giveMeOne<GeminiSetting>()

                    settingFlow.value = changedSetting
                    useCase(parameter = emptyParameter()).shouldBeSuccess()

                    settingList.last() shouldBe changedSetting
                }
            }
        }

        Given("생성 결과의 제목과 설명이 공백뿐이다") {
            When("메모 초안을 조회하면") {
                Then("TC-0e4ad239-dd9e-40bb-a0a1-e1dfc8211061: 제목과 설명을 없는 결과로 전달한다") {
                    listOf("", "   ", "\n\t").forEach { blank ->
                        val useCase =
                            createUseCase(
                                draft = MemoDraft(title = blank, description = blank, dateTime = null),
                            )

                        val actual = useCase(parameter = emptyParameter()).shouldBeSuccess()

                        actual.title shouldBe ""
                        actual.description shouldBe ""
                    }
                }
            }
        }

        Given("생성 결과의 제목에 줄바꿈이 있다") {
            When("메모 초안을 조회하면") {
                Then("TC-3ef3be8e-3c9a-47b3-852a-652479ec6181: 줄바꿈을 공백으로 바꾼 한 줄 제목을 전달한다") {
                    listOf(
                        "주간\n회고" to "주간 회고",
                        "주간\r\n회고" to "주간 회고",
                        "주간\r회고" to "주간 회고",
                        "주간\n\n회고" to "주간  회고",
                        "\n\n" to "",
                    ).forEach { (title, expected) ->
                        val useCase = createUseCase(draft = MemoDraft(title = title, description = "설명", dateTime = null))

                        useCase(parameter = emptyParameter()).shouldBeSuccess().title shouldBe expected
                    }
                }
            }

            When("설명에도 줄바꿈이 있으면") {
                Then("설명의 줄바꿈은 그대로 전달한다") {
                    val description = "첫 줄\n둘째 줄"
                    val useCase = createUseCase(draft = MemoDraft(title = "제목", description = description, dateTime = null))

                    useCase(parameter = emptyParameter()).shouldBeSuccess().description shouldBe description
                }
            }
        }

        Given("생성 결과의 기간 시작이 종료보다 늦다") {
            When("메모 초안을 조회하면") {
                Then("TC-e56e96e4-61d9-42ad-81e5-d0a2e5b5a3c4: 기간이 없는 결과로 전달한다") {
                    listOf(
                        MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 22)..LocalDate(2026, 9, 21)),
                        MemoDateTime.DateTime(
                            start = LocalDateTime(2026, 9, 22, 10, 0),
                            endInclusive = LocalDateTime(2026, 9, 22, 9, 0),
                        ),
                    ).forEach { dateTime ->
                        val useCase = createUseCase(draft = MemoDraft(title = "제목", description = "설명", dateTime = dateTime))

                        useCase(parameter = emptyParameter()).shouldBeSuccess().dateTime shouldBe null
                    }
                }
            }
        }

        Given("생성 결과의 기간이 쓸 수 있다") {
            When("메모 초안을 조회하면") {
                Then("기간을 그대로 전달한다") {
                    listOf(
                        MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
                        MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 21)),
                        MemoDateTime.DateTime(
                            start = LocalDateTime(2026, 9, 22, 9, 0),
                            endInclusive = LocalDateTime(2026, 9, 22, 10, 0),
                        ),
                    ).forEach { dateTime ->
                        val useCase = createUseCase(draft = MemoDraft(title = "제목", description = "설명", dateTime = dateTime))

                        useCase(parameter = emptyParameter()).shouldBeSuccess().dateTime shouldBe dateTime
                    }
                }
            }
        }

        Given("원격 생성이 실패한다") {
            When("메모 초안을 조회하면") {
                Then("TC-7f71e24a-8875-4480-a89e-23d86fa1c81e: 실패를 그대로 전달한다") {
                    val cause = IllegalStateException("generate failed")
                    val memoDraftRepository =
                        mockk<MemoDraftRepository> {
                            coEvery { fetch(any(), any()) } throws cause
                        }
                    val useCase = createUseCase(memoDraftRepository = memoDraftRepository)

                    useCase(parameter = emptyParameter()).shouldBeFailure() shouldBe cause
                }
            }
        }
    })

private fun emptyParameter(): FetchMemoDraftUseCase.Parameter =
    FetchMemoDraftUseCase.Parameter(
        prompt = "",
        title = "",
        description = "",
        dateTime = null,
    )

private fun createUseCase(
    setting: GeminiSetting = fixtureMonkey.giveMeOne(),
    draft: MemoDraft = MemoDraft.EMPTY,
    memoDraftRepository: MemoDraftRepository =
        mockk {
            coEvery { fetch(any(), any()) } returns draft
        },
    clock: Clock = Clock.System,
): FetchMemoDraftUseCase =
    FetchMemoDraftUseCase(
        geminiSettingRepository =
            mockk<GeminiSettingRepository> {
                every { get() } returns flowOf(setting)
            },
        memoDraftRepository = memoDraftRepository,
        clock = clock,
    )
