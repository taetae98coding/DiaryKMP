package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
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
class ChromeSessionImportEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CHROME-SESSION-IMPORT-DOMAIN-018 앱이 보이게 될 때마다 가져오기를 요청한다`() {
        val requestImport = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setEffect(requestImport = requestImport)

        composeRule.runOnIdle { verify(exactly = 1) { requestImport() } }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle { verify(exactly = 2) { requestImport() } }
    }

    @Test
    fun `보이는 채로 포커스만 오가는 것은 계기가 아니다`() {
        val requestImport = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setEffect(requestImport = requestImport, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle { verify(exactly = 1) { requestImport() } }
    }

    @Test
    fun `보이지 않는 동안에는 요청하지 않는다`() {
        val requestImport = mockk<() -> Unit>(relaxed = true)
        setEffect(requestImport = requestImport, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { verify(exactly = 0) { requestImport() } }
    }

    private fun setEffect(
        requestImport: () -> Unit,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ChromeSessionImportEffect(requestImport = requestImport)
            }
        }

        return lifecycleOwner
    }
}
