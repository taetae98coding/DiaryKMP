package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SyncEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Android는 앱이 화면에 보이는 동안을 활성 상태로 본다`() {
        syncMinActiveState shouldBe Lifecycle.State.STARTED
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-032 TC-SYNC-REFRESH-FEATURE-003 Android와 iOS에서 앱이 다시 화면에 보이게 되면 진행을 표시할 동기화를 다시 요청한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val requestSync = mockk<(SyncTrigger) -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync)

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync(any()) }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 2) { requestSync(SyncTrigger.ACCOUNT_CONFIRMED) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-035 Android와 iOS에서 앱이 화면에서 보이지 않는 동안에는 동기화를 요청하지 않는다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val otherAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(Account.Guest)
        val requestSync = mockk<(SyncTrigger) -> Unit>(relaxed = true)
        setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { accountFlow.value = account }
        composeRule.runOnIdle { accountFlow.value = otherAccount }

        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync(any()) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-086 Android와 iOS에서 보이지 않는 동안 바뀐 계정은 다시 보이게 될 때 동기화를 한 번 요청한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val otherAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(Account.Guest)
        val requestSync = mockk<(SyncTrigger) -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { accountFlow.value = account }
        composeRule.runOnIdle { accountFlow.value = otherAccount }
        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync(any()) }
            lifecycleOwner.currentState = Lifecycle.State.STARTED
        }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync(any()) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-085 TC-SYNC-REFRESH-FEATURE-003 앱 화면이 다시 만들어지면 같은 계정이어도 진행을 표시할 동기화를 한 번 다시 요청한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
        val requestSyncUseCase = mockk<RequestSyncUseCase>()
        coEvery { requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED) } returns Result.success(Unit)
        val viewModel =
            AppSyncViewModel(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                schedulePeriodicSyncUseCase = mockk(relaxed = true),
            )
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SyncEffect(
                    requestSync = viewModel::requestSync,
                    schedulePeriodicSync = viewModel::schedulePeriodicSync,
                    account = viewModel.account,
                )
            }
        }
        composeRule.runOnIdle {
            coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED) }
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            coVerify(exactly = 2) { requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED) }
        }
    }

    @Test
    fun `Android와 iOS에서는 포커스를 잃고 다시 얻는 것은 계기가 아니다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val requestSync = mockk<(SyncTrigger) -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync(any()) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-056 앱이 인증된 사용자 계정을 확인하면 주기 동기화를 예약한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(accountFlow, mockk(relaxed = true), schedulePeriodicSync = schedulePeriodicSync)

        composeRule.runOnIdle {
            verify(exactly = 1) { schedulePeriodicSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-059 로그아웃되어 계정이 바뀌면 주기 동기화 예약을 다시 정한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val schedulePeriodicSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(accountFlow, mockk(relaxed = true), schedulePeriodicSync = schedulePeriodicSync)

        composeRule.runOnIdle { accountFlow.value = Account.Guest }

        composeRule.runOnIdle {
            verify(exactly = 2) { schedulePeriodicSync() }
        }
    }

    private fun setSyncEffect(
        accountFlow: MutableStateFlow<Account>,
        requestSync: (SyncTrigger) -> Unit,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
        schedulePeriodicSync: () -> Unit = mockk(relaxed = true),
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SyncEffect(
                    requestSync = requestSync,
                    schedulePeriodicSync = schedulePeriodicSync,
                    account = accountFlow,
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
