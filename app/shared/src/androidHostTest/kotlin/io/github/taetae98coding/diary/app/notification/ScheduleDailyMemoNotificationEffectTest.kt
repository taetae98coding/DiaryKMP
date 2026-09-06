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
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-007 앱 화면이 다시 구성되어도 예약을 다시 요청하지 않는다`() {
        val schedule = mockk<() -> Unit>(relaxed = true)
        val recomposeCount = mutableIntStateOf(0)

        setScheduleDailyMemoNotificationEffect(schedule = schedule, recomposeCount = recomposeCount)

        composeRule.runOnIdle { recomposeCount.intValue++ }

        composeRule.runOnIdle {
            verify(exactly = 1) { schedule() }
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

    private fun setScheduleDailyMemoNotificationEffect(
        schedule: () -> Unit,
        lifecycleOwner: TestLifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED),
        recomposeCount: MutableIntState = mutableIntStateOf(0),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ScheduleDailyMemoNotificationContent(schedule = schedule, recomposeCount = recomposeCount)
            }
        }
    }

    @Composable
    private fun ScheduleDailyMemoNotificationContent(
        schedule: () -> Unit,
        recomposeCount: MutableIntState,
    ) {
        // 재구성 계기를 효과 호출부와 같은 람다에서 읽어야 효과가 다시 호출된다.
        Text(text = "$APP_CONTENT ${recomposeCount.intValue}")
        ScheduleDailyMemoNotificationEffect(schedule = schedule)
    }

    companion object {
        private const val APP_CONTENT = "앱 화면"
    }
}
