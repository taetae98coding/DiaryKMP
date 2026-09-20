package io.github.taetae98coding.diary.compose.core.effect

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
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
class CollectEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본값은 STARTED부터 수집하고 RESUMED 사이의 전이는 다시 수집하지 않는다`() {
        val effect = MutableStateFlow(Unit)
        val onEffect = mockk<(Unit) -> Unit>(relaxed = true)
        val lifecycleOwner = setCollectEffect(effect, onEffect, initialState = Lifecycle.State.RESUMED)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 1) { onEffect(Unit) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-068 RESUMED 기준이면 STARTED로 내려갔다가 RESUMED로 돌아올 때 마지막 값을 다시 수집한다`() {
        val effect = MutableStateFlow(Unit)
        val onEffect = mockk<(Unit) -> Unit>(relaxed = true)
        val lifecycleOwner =
            setCollectEffect(
                effect,
                onEffect,
                minActiveState = Lifecycle.State.RESUMED,
                initialState = Lifecycle.State.RESUMED,
            )

        composeRule.runOnIdle {
            verify(exactly = 1) { onEffect(Unit) }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }

        composeRule.runOnIdle {
            verify(exactly = 1) { onEffect(Unit) }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 2) { onEffect(Unit) }
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-069 RESUMED 기준이면 STARTED 동안 방출된 값은 수집하지 않고 RESUMED가 된 뒤 마지막 값만 수집한다`() {
        val effect = MutableStateFlow(0)
        val onEffect = mockk<(Int) -> Unit>(relaxed = true)
        val lifecycleOwner =
            setCollectEffect(
                effect,
                onEffect,
                minActiveState = Lifecycle.State.RESUMED,
                initialState = Lifecycle.State.STARTED,
            )

        composeRule.runOnIdle { effect.value = 1 }
        composeRule.runOnIdle { effect.value = 2 }

        composeRule.runOnIdle {
            verify(exactly = 0) { onEffect(any()) }
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            verify(exactly = 1) { onEffect(any()) }
            verify(exactly = 1) { onEffect(2) }
        }
    }

    private fun <T> setCollectEffect(
        effect: MutableStateFlow<T>,
        onEffect: (T) -> Unit,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        initialState: Lifecycle.State = Lifecycle.State.STARTED,
    ): TestLifecycleOwner {
        val lifecycleOwner = TestLifecycleOwner(initialState)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                CollectEffect(
                    effect = effect,
                    minActiveState = minActiveState,
                    onEffect = { value -> onEffect(value) },
                )
            }
        }

        return lifecycleOwner
    }
}
