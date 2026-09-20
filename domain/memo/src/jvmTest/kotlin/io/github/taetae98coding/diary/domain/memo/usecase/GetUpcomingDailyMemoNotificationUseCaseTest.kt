package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private val NOW: Instant = LocalDateTime(date = LocalDate(2026, 9, 20), time = LocalTime(hour = 7, minute = 0)).toInstant(TimeZone.currentSystemDefault())
private val DATE_LIST: List<LocalDate> = upcomingDailyMemoNotificationDateList(now = NOW, timeZone = TimeZone.currentSystemDefault())

class GetUpcomingDailyMemoNotificationUseCaseTest :
    BehaviorSpec({
        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017: 발생 시점에 앱 코드가 실행되지 않는 환경이고 현지 시각이 정해져 있다") {
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            val useCase = useCase(getDailyMemoUseCase = getDailyMemoUseCase)

            When("앱이 실행되어 알림 내용을 정한다") {
                Then("다음 오전 8시부터 하루씩 이어지는 7개 날짜의 내용이 정해지고 그 밖의 날짜는 정해지지 않는다") {
                    val upcomingList = useCase(parameter = Unit).first().shouldBeSuccess()

                    upcomingList.map { upcoming -> upcoming.date } shouldBe DATE_LIST
                    DATE_LIST.forEach { date -> verify(exactly = 1) { getDailyMemoUseCase(parameter = date) } }
                    verify(exactly = DATE_LIST.size) { getDailyMemoUseCase(parameter = any()) }
                }
            }
        }

        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-018: 날짜마다 겹치는 오늘의 메모가 다르게 저장되어 있다") {
            val firstDayMemoList = fixtureMonkey.giveMe<DailyMemo>(size = 2)
            val spanningMemoList = fixtureMonkey.giveMe<DailyMemo>(size = 1)
            val fourthDayMemoList = fixtureMonkey.giveMe<DailyMemo>(size = 1)
            val memoListByDate =
                mapOf(
                    DATE_LIST[0] to firstDayMemoList + spanningMemoList,
                    DATE_LIST[1] to spanningMemoList,
                    DATE_LIST[2] to spanningMemoList,
                    DATE_LIST[3] to fourthDayMemoList,
                )
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = any()) } answers { flowOf(Result.success(memoListByDate[firstArg<LocalDate>()].orEmpty())) }
            val useCase = useCase(getDailyMemoUseCase = getDailyMemoUseCase)

            When("앱이 실행되어 알림 내용을 정한다") {
                Then("각 날짜의 내용은 그 날짜를 기준으로 정해지고 겹치는 메모가 없는 날짜는 빈 목록이다") {
                    val upcomingList = useCase(parameter = Unit).first().shouldBeSuccess()

                    upcomingList shouldBe
                        DATE_LIST.map { date ->
                            UpcomingDailyMemoNotification(
                                date = date,
                                content = DailyMemoNotificationContent.Loaded(memoList = memoListByDate[date].orEmpty()),
                            )
                        }
                    upcomingList.drop(4).forEach { upcoming -> upcoming.content shouldBe DailyMemoNotificationContent.Loaded(memoList = emptyList()) }
                }
            }
        }

        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-019: 알림 내용이 한 번 정해져 있고 첫째 날에는 오늘의 메모가 하나 담겨 있다") {
            val memo = fixtureMonkey.giveMe<DailyMemo>(size = 1)
            val firstDayFlow = MutableStateFlow<Result<List<DailyMemo>>>(Result.success(memo))
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            every { getDailyMemoUseCase(parameter = DATE_LIST[0]) } returns firstDayFlow
            val useCase = useCase(getDailyMemoUseCase = getDailyMemoUseCase)

            When("앱이 실행되는 동안 기기에 저장된 메모가 바뀐다") {
                Then("바뀐 데이터로 7개 날짜의 내용이 다시 정해진다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess().first().content shouldBe DailyMemoNotificationContent.Loaded(memoList = memo)

                        val addedMemoList = memo + fixtureMonkey.giveMe<DailyMemo>(size = 1)
                        firstDayFlow.value = Result.success(addedMemoList)
                        val added = awaitItem().shouldBeSuccess()
                        added shouldHaveSize DATE_LIST.size
                        added.first().content shouldBe DailyMemoNotificationContent.Loaded(memoList = addedMemoList)

                        firstDayFlow.value = Result.success(emptyList())
                        awaitItem().shouldBeSuccess().first().content shouldBe DailyMemoNotificationContent.Loaded(memoList = emptyList())
                    }
                }
            }
        }

        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-020: 기기에 저장된 메모를 조회하면 실패하도록 준비되어 있다") {
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = any()) } returns flowOf(Result.success(fixtureMonkey.giveMe<DailyMemo>(size = 1)))
            every { getDailyMemoUseCase(parameter = DATE_LIST[3]) } returns flowOf(Result.failure(IllegalStateException("query error")))
            val useCase = useCase(getDailyMemoUseCase = getDailyMemoUseCase)

            When("앱이 실행되어 알림 내용을 정한다") {
                Then("7개 날짜 모두 확인 안내만 담는 내용으로 정해지고 실패로 전달되지 않는다") {
                    val upcomingList = useCase(parameter = Unit).first().shouldBeSuccess()

                    upcomingList shouldBe DATE_LIST.map { date -> UpcomingDailyMemoNotification(date = date, content = DailyMemoNotificationContent.Unavailable) }
                }
            }
        }
    })

private fun useCase(getDailyMemoUseCase: GetDailyMemoUseCase): GetUpcomingDailyMemoNotificationUseCase {
    val clock = mockk<Clock>()
    every { clock.now() } returns NOW

    return GetUpcomingDailyMemoNotificationUseCase(
        getDailyMemoUseCase = getDailyMemoUseCase,
        clock = clock,
    )
}
