package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class SubmitUpcomingDailyMemoNotificationUseCaseTest :
    BehaviorSpec({
        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017: 앞으로 7일의 알림 내용이 정해져 있다") {
            val upcomingList = upcomingList()
            val dailyMemoNotificationManager = mockk<DailyMemoNotificationManager>(relaxed = true)
            val useCase = SubmitUpcomingDailyMemoNotificationUseCase(dailyMemoNotificationManager = dailyMemoNotificationManager)

            When("알림 내용을 제출한다") {
                Then("각 날짜의 오전 8시 알림으로 한 번 제출된다") {
                    useCase(parameter = upcomingList).shouldBeSuccess(Unit)

                    coVerify(exactly = 1) { dailyMemoNotificationManager.submitUpcoming(time = LocalTime(hour = 8, minute = 0), upcomingList = upcomingList) }
                }
            }
        }

        Given("알림 제출이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException("submit error")
            val dailyMemoNotificationManager = mockk<DailyMemoNotificationManager>()
            coEvery { dailyMemoNotificationManager.submitUpcoming(time = any(), upcomingList = any()) } throws throwable
            val useCase = SubmitUpcomingDailyMemoNotificationUseCase(dailyMemoNotificationManager = dailyMemoNotificationManager)

            When("알림 내용을 제출한다") {
                Then("제출 실패를 전달한다") {
                    useCase(parameter = upcomingList()).shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    })

private fun upcomingList(): List<UpcomingDailyMemoNotification> =
    List(size = 7) {
        UpcomingDailyMemoNotification(
            date = fixtureMonkey.giveMeOne<LocalDate>(),
            content = DailyMemoNotificationContent.Loaded(memoList = fixtureMonkey.giveMe<DailyMemo>(size = 2)),
        )
    }
