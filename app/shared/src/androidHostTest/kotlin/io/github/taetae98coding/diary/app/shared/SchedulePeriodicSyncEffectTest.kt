package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SchedulePeriodicSyncEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DATA-SYNC-DOMAIN-056 앱이 인증된 사용자 계정을 확인하면 주기 동기화를 예약한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val uiStateFlow = MutableStateFlow<AppPeriodicSyncUiState>(AppPeriodicSyncUiState.Confirmed(account = account))
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSchedulePeriodicSyncEffect(uiStateFlow, schedulePeriodicSync)

        composeRule.runOnIdle {
            verify(exactly = 1) { schedulePeriodicSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-059 로그아웃되어 계정이 바뀌면 주기 동기화 예약을 다시 정한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val uiStateFlow = MutableStateFlow<AppPeriodicSyncUiState>(AppPeriodicSyncUiState.Confirmed(account = account))
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSchedulePeriodicSyncEffect(uiStateFlow, schedulePeriodicSync)

        composeRule.runOnIdle { uiStateFlow.value = AppPeriodicSyncUiState.Confirmed(account = Account.Guest) }

        composeRule.runOnIdle {
            verify(exactly = 2) { schedulePeriodicSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-083 로그인 세션이 인증되지 않은 것으로 확인되면 주기 동기화 예약을 다시 정한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true, isSessionPending = false)
        val uiStateFlow = MutableStateFlow<AppPeriodicSyncUiState>(AppPeriodicSyncUiState.Confirmed(account = account))
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSchedulePeriodicSyncEffect(uiStateFlow, schedulePeriodicSync)

        composeRule.runOnIdle { uiStateFlow.value = AppPeriodicSyncUiState.Confirmed(account = account.copy(isSessionPending = true, isSessionValid = false)) }
        composeRule.runOnIdle { uiStateFlow.value = AppPeriodicSyncUiState.Confirmed(account = account.copy(isSessionPending = false, isSessionValid = false)) }

        composeRule.runOnIdle {
            verify(exactly = 3) { schedulePeriodicSync() }
        }
    }

    @Test
    fun `앱이 화면에 보이더라도 계정이 확인되기 전에는 주기 동기화를 예약하지 않는다`() {
        val uiStateFlow = MutableStateFlow<AppPeriodicSyncUiState>(AppPeriodicSyncUiState.Loading)
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSchedulePeriodicSyncEffect(uiStateFlow, schedulePeriodicSync)

        composeRule.runOnIdle {
            verify(exactly = 0) { schedulePeriodicSync() }
        }
    }

    private fun setSchedulePeriodicSyncEffect(
        uiStateFlow: MutableStateFlow<AppPeriodicSyncUiState>,
        schedulePeriodicSync: () -> Unit,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SchedulePeriodicSyncEffect(
                    schedulePeriodicSync = schedulePeriodicSync,
                    uiState = uiStateFlow,
                )
            }
        }

        return lifecycleOwner
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
