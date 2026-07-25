package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DialogStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `처음에는 닫힌 상태다`() {
        DialogState().isVisible shouldBe false
    }

    @Test
    fun `show와 hide로 열림 여부를 바꾼다`() {
        val state = DialogState()

        state.show()
        state.isVisible shouldBe true

        state.hide()
        state.isVisible shouldBe false
    }

    @Test
    fun `화면이 재생성되어도 열린 상태가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DialogState

        restorationTester.setContent { state = rememberDialogState() }

        composeRule.runOnIdle { state.show() }
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.isVisible shouldBe true }
    }

    @Test
    fun `화면이 재생성되어도 닫힌 상태가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DialogState

        restorationTester.setContent { state = rememberDialogState(initialVisible = true) }

        composeRule.runOnIdle { state.hide() }
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.isVisible shouldBe false }
    }
}
