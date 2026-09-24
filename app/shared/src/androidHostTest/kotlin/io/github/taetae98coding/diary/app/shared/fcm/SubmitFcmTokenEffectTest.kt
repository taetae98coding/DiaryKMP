package io.github.taetae98coding.diary.app.shared.fcm

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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SubmitFcmTokenEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-FCM-TOKEN-DOMAIN-005 앱이 시작되어 계정이 확인되면 토큰 제출을 요청한다`() {
        val submit = mockk<() -> Unit>(relaxed = true)
        setSubmitFcmTokenEffect(MutableStateFlow(fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)), submit)

        composeRule.runOnIdle {
            verify(exactly = 1) { submit() }
        }
    }

    @Test
    fun `TC-FCM-TOKEN-DOMAIN-005 앱이 다시 화면에 보이게 되면 토큰 제출을 다시 요청한다`() {
        val submit = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSubmitFcmTokenEffect(MutableStateFlow(fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)), submit)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 2) { submit() }
        }
    }

    @Test
    fun `앱이 화면에서 보이지 않는 동안에는 토큰 제출을 요청하지 않는다`() {
        val accountFlow = MutableStateFlow<Account>(Account.Guest)
        val submit = mockk<() -> Unit>(relaxed = true)
        setSubmitFcmTokenEffect(accountFlow, submit, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { accountFlow.value = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true) }

        composeRule.runOnIdle {
            verify(exactly = 0) { submit() }
        }
    }

    private fun setSubmitFcmTokenEffect(
        accountFlow: MutableStateFlow<Account>,
        submit: () -> Unit,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SubmitFcmTokenEffect(
                    submit = submit,
                    account = accountFlow,
                )
            }
        }

        return lifecycleOwner
    }
}
