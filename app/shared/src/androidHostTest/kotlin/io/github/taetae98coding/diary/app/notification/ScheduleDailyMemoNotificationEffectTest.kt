package io.github.taetae98coding.diary.app.notification

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ScheduleDailyMemoNotificationEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 앱 화면이 시작되면 알림이 한 번 예약된다`() {
        val schedule = mockk<() -> Unit>(relaxed = true)

        setScheduleDailyMemoNotificationEffect(schedule = schedule)

        composeRule.runOnIdle {
            verify(exactly = 1) { schedule() }
        }
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017 앱 화면이 시작되어 앞으로의 알림 내용이 정해지면 그 내용을 한 번 제출한다`() {
        val upcomingList = upcomingList()
        val submitUpcoming = mockk<(List<UpcomingDailyMemoNotification>) -> Unit>(relaxed = true)

        setScheduleDailyMemoNotificationEffect(submitUpcoming = submitUpcoming, upcoming = MutableStateFlow(upcomingList))

        composeRule.runOnIdle {
            verify(exactly = 1) { submitUpcoming(upcomingList) }
            verify(exactly = 1) { submitUpcoming(any()) }
        }
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-019 앱 화면이 보이는 동안 알림 내용이 바뀌면 바뀐 내용을 다시 제출한다`() {
        val upcomingList = upcomingList()
        val changedUpcomingList = upcomingList()
        val upcomingFlow = MutableStateFlow(upcomingList)
        val submitUpcoming = mockk<(List<UpcomingDailyMemoNotification>) -> Unit>(relaxed = true)

        setScheduleDailyMemoNotificationEffect(submitUpcoming = submitUpcoming, upcoming = upcomingFlow)

        composeRule.runOnIdle { upcomingFlow.value = changedUpcomingList }

        composeRule.runOnIdle {
            verify(exactly = 1) { submitUpcoming(upcomingList) }
            verify(exactly = 1) { submitUpcoming(changedUpcomingList) }
        }
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-007 앱 화면이 다시 구성되어도 예약과 내용 제출을 다시 요청하지 않는다`() {
        val schedule = mockk<() -> Unit>(relaxed = true)
        val submitUpcoming = mockk<(List<UpcomingDailyMemoNotification>) -> Unit>(relaxed = true)
        val recomposeCount = mutableIntStateOf(0)

        setScheduleDailyMemoNotificationEffect(
            schedule = schedule,
            submitUpcoming = submitUpcoming,
            upcoming = MutableStateFlow(upcomingList()),
            recomposeCount = recomposeCount,
        )

        composeRule.runOnIdle { recomposeCount.intValue++ }

        composeRule.runOnIdle {
            verify(exactly = 1) { schedule() }
            verify(exactly = 1) { submitUpcoming(any()) }
        }
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-007 앱이 백그라운드에 갔다가 돌아와도 예약을 다시 요청하지 않는다`() {
        val schedule = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        setScheduleDailyMemoNotificationEffect(schedule = schedule, lifecycleOwner = lifecycleOwner)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 1) { schedule() }
        }
    }

    // 보이지 않는 동안 바뀐 내용을 놓치지 않도록 다시 보이게 되면 마지막 내용을 다시 제출한다. 같은 날짜의 알림은 새 내용으로 대신되므로 예약이 늘어나지 않는다.
    @Test
    fun `앱이 백그라운드에 갔다가 돌아오면 마지막 알림 내용을 다시 제출한다`() {
        val upcomingList = upcomingList()
        val submitUpcoming = mockk<(List<UpcomingDailyMemoNotification>) -> Unit>(relaxed = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        setScheduleDailyMemoNotificationEffect(
            submitUpcoming = submitUpcoming,
            upcoming = MutableStateFlow(upcomingList),
            lifecycleOwner = lifecycleOwner,
        )

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 2) { submitUpcoming(upcomingList) }
        }
    }

    @Test
    fun `앱이 보이지 않는 동안에는 알림 내용을 제출하지 않는다`() {
        val upcomingFlow = MutableStateFlow(upcomingList())
        val submitUpcoming = mockk<(List<UpcomingDailyMemoNotification>) -> Unit>(relaxed = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)

        setScheduleDailyMemoNotificationEffect(
            submitUpcoming = submitUpcoming,
            upcoming = upcomingFlow,
            lifecycleOwner = lifecycleOwner,
        )

        composeRule.runOnIdle { upcomingFlow.value = upcomingList() }

        composeRule.runOnIdle {
            verify(exactly = 0) { submitUpcoming(any()) }
        }
    }

    private fun setScheduleDailyMemoNotificationEffect(
        schedule: () -> Unit = {},
        submitUpcoming: (List<UpcomingDailyMemoNotification>) -> Unit = {},
        upcoming: Flow<List<UpcomingDailyMemoNotification>> = emptyFlow(),
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED),
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ScheduleDailyMemoNotificationContent(
                    schedule = schedule,
                    submitUpcoming = submitUpcoming,
                    upcoming = upcoming,
                    recomposeCount = recomposeCount,
                )
            }
        }
    }

    @Composable
    private fun ScheduleDailyMemoNotificationContent(
        schedule: () -> Unit,
        submitUpcoming: (List<UpcomingDailyMemoNotification>) -> Unit,
        upcoming: Flow<List<UpcomingDailyMemoNotification>>,
        recomposeCount: MutableIntState,
    ) {
        // 재구성 계기를 효과 호출부와 같은 람다에서 읽어야 효과가 다시 호출된다.
        Text(text = "$APP_CONTENT ${recomposeCount.intValue}")
        ScheduleDailyMemoNotificationEffect(
            schedule = schedule,
            submitUpcoming = submitUpcoming,
            upcoming = upcoming,
        )
    }

    companion object {
        private const val APP_CONTENT = "앱 화면"

        private fun upcomingList(): List<UpcomingDailyMemoNotification> =
            List(size = 7) {
                UpcomingDailyMemoNotification(
                    date = fixtureMonkey.giveMeOne<LocalDate>(),
                    content = DailyMemoNotificationContent.Loaded(memoList = fixtureMonkey.giveMe<DailyMemo>(size = 2)),
                )
            }
    }
}
