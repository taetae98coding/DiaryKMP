package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class ReconcileFileUploadEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `계정이 확인되기 전에는 올리기 상태를 맞추지 않는다`() {
        val uiStateFlow = MutableStateFlow<AppFileUploadUiState>(AppFileUploadUiState.Loading)
        val reconcile = mockk<(Account) -> Unit>(relaxed = true)
        setReconcileFileUploadEffect(uiStateFlow, reconcile)

        composeRule.runOnIdle {
            verify(exactly = 0) { reconcile(any()) }
        }
    }

    @Test
    fun `계정이 확인되면 그 계정으로 올리기 상태를 맞춘다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val uiStateFlow = MutableStateFlow<AppFileUploadUiState>(AppFileUploadUiState.Loading)
        val reconcile = mockk<(Account) -> Unit>(relaxed = true)
        setReconcileFileUploadEffect(uiStateFlow, reconcile)

        composeRule.runOnIdle { uiStateFlow.value = AppFileUploadUiState.Confirmed(account = account) }

        composeRule.runOnIdle {
            verify(exactly = 1) { reconcile(any()) }
            verify(exactly = 1) { reconcile(account) }
        }
    }

    private fun setReconcileFileUploadEffect(
        uiStateFlow: MutableStateFlow<AppFileUploadUiState>,
        reconcile: (Account) -> Unit,
    ) {
        val lifecycleOwner = TestLifecycleOwner()

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ReconcileFileUploadEffect(
                    reconcile = reconcile,
                    uiState = uiStateFlow,
                )
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
