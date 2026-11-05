package io.github.taetae98coding.diary.compose.core.shortcut

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AddShortcutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Meta와 A를 함께 누르면 추가를 한 번 실행한다`() {
        var addCount = 0
        composeRule.setContent {
            Box(modifier = Modifier.size(100.dp).addShortcut { addCount++ })
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        addCount shouldBe 1
    }

    @Test
    fun `A만 누르면 추가를 실행하지 않는다`() {
        var addCount = 0
        composeRule.setContent {
            Box(modifier = Modifier.size(100.dp).addShortcut { addCount++ })
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.A)
            keyUp(Key.A)
        }
        composeRule.waitForIdle()

        addCount shouldBe 0
    }

    @Test
    fun `Ctrl과 A를 함께 누르면 추가를 실행하지 않는다`() {
        var addCount = 0
        composeRule.setContent {
            Box(modifier = Modifier.size(100.dp).addShortcut { addCount++ })
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.CtrlLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.CtrlLeft)
        }
        composeRule.waitForIdle()

        addCount shouldBe 0
    }

    @Test
    fun `추가를 실행할 수 없는 동안에는 Meta와 A를 눌러도 아무 일도 일어나지 않는다`() {
        var addCount = 0
        composeRule.setContent {
            Box(modifier = Modifier.size(100.dp).addShortcut(isEnabledProvider = { false }) { addCount++ })
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        addCount shouldBe 0
    }

    @Test
    fun `추가를 실행할 수 없다가 실행할 수 있게 되면 그때부터 추가를 실행한다`() {
        var addCount = 0
        var isEnabled by mutableStateOf(false)
        composeRule.setContent {
            Box(modifier = Modifier.size(100.dp).addShortcut(isEnabledProvider = { isEnabled }) { addCount++ })
        }

        composeRule.runOnIdle { isEnabled = true }
        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        addCount shouldBe 1
    }
}
