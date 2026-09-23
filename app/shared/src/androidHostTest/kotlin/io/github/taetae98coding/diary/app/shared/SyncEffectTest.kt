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
import io.kotest.matchers.shouldBe
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
class SyncEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Android는 앱이 화면에 보이는 동안을 활성 상태로 본다`() {
        syncMinActiveState shouldBe Lifecycle.State.STARTED
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-032 Android와 iOS에서 앱이 다시 화면에 보이게 되면 동기화를 다시 요청한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync)

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
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val otherAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(Account.Guest)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { accountFlow.value = account }
        composeRule.runOnIdle { accountFlow.value = otherAccount }

        composeRule.runOnIdle {
            verify(exactly = 0) { requestSync() }
        }
    }

    @Test
    fun `보이지 않는 동안 바뀐 계정은 다시 보이게 될 때 동기화를 요청한다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(Account.Guest)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { accountFlow.value = account }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync() }
        }
    }

    @Test
    fun `Android와 iOS에서는 포커스를 잃고 다시 얻는 것은 계기가 아니다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
        val accountFlow = MutableStateFlow<Account>(account)
        val requestSync = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSyncEffect(accountFlow, requestSync, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 1) { requestSync() }
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
        requestSync: () -> Unit,
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
