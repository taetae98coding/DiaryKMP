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
                Then("TC-MEMO-GEMINI-DATA-001: 저장된 설정과 입력 중인 내용을 함께 전달한다") {
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

        Given("생성 결과의 제목과 설명이 공백뿐이다") {
            When("메모 초안을 조회하면") {
                Then("TC-MEMO-GEMINI-DOMAIN-002: 제목과 설명을 없는 결과로 전달한다") {
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

        Given("생성 결과의 기간 시작이 종료보다 늦다") {
            When("메모 초안을 조회하면") {
                Then("TC-MEMO-GEMINI-DOMAIN-003: 기간이 없는 결과로 전달한다") {
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
                Then("TC-MEMO-GEMINI-DATA-006: 실패를 그대로 전달한다") {
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
