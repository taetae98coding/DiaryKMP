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
import kotlin.uuid.Uuid

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
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()))
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(uiStateFlow, requestSync)

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync() }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 2) { requestSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-035 Android와 iOS에서 앱이 화면에서 보이지 않는 동안에는 동기화를 요청하지 않는다`() {
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Loading)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(uiStateFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()) }

        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-086 Android와 iOS에서 보이지 않는 동안 바뀐 계정은 다시 보이게 될 때 동기화를 한 번 요청한다`() {
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Loading)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(uiStateFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()) }
        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync() }
            lifecycleOwner.currentState = Lifecycle.State.STARTED
        }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync() }
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
            )
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)

        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SyncEffect(
                    requestSync = viewModel::requestSync,
                    uiState = viewModel.uiState,
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
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Authenticated(accountId = fixtureMonkey.giveMeOne<Uuid>()))
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(uiStateFlow, requestSync, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync() }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-011 TC-DATA-SYNC-DOMAIN-092 인증이 풀렸다가 같은 계정으로 다시 인증되면 동기화를 다시 요청한다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Authenticated(accountId = accountId))
        val requestSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(uiStateFlow, requestSync)

        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Unauthenticated }
        composeRule.runOnIdle { uiStateFlow.value = AppSyncUiState.Authenticated(accountId = accountId) }

        composeRule.runOnIdle {
            verify(exactly = 2) { requestSync() }
        }
    }

    @Test
    fun `앱이 화면에 보이더라도 인증된 계정이 확인되기 전에는 동기화를 요청하지 않는다`() {
        val uiStateFlow = MutableStateFlow<AppSyncUiState>(AppSyncUiState.Loading)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(uiStateFlow, requestSync)

        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync() }
        }
    }

    private fun setSyncEffect(
        uiStateFlow: MutableStateFlow<AppSyncUiState>,
        requestSync: () -> Unit,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SyncEffect(
                    requestSync = requestSync,
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
