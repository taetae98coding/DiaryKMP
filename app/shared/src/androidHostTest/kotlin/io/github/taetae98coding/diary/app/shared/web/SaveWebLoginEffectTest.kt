package io.github.taetae98coding.diary.app.shared.web

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

// Android는 앱이 화면에 보이는 동안을 활성 상태로 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SaveWebLoginEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-908e16b2-fd82-4d9d-83b8-9d6431d795a4 TC-f2f9cbff-6f3d-47fe-a8cd-e5e8a527031e 앱이 활성 상태에서 벗어날 때마다 보관을 요청한다`() {
        val save = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSaveWebLoginEffect(save = save, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { verify(exactly = 1) { save() } }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { verify(exactly = 2) { save() } }
    }

    @Test
    fun `활성 상태 안에서 옮겨 가는 것은 벗어난 것이 아니다`() {
        val save = mockk<() -> Unit>(relaxed = true)
        val lifecycleOwner = setSaveWebLoginEffect(save = save, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle { verify(exactly = 0) { save() } }
    }

    @Test
    fun `처음부터 활성 상태가 아니면 보관하지 않는다`() {
        val save = mockk<() -> Unit>(relaxed = true)
        setSaveWebLoginEffect(save = save, initialState = Lifecycle.State.CREATED)

        composeRule.runOnIdle { verify(exactly = 0) { save() } }
    }

    private fun setSaveWebLoginEffect(
        save: () -> Unit,
        initialState: Lifecycle.State,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SaveWebLoginEffect(save = save)
            }
        }

        return lifecycleOwner
    }
}
