package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-014 화면이 재생성되어도 다이얼로그가 열린 상태로 남는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: PlaceFormState

        restorationTester.setContent {
            DiaryTheme {
                state = rememberPlaceAddFormState()
            }
        }

        composeRule.runOnIdle { state.searchDialogState.show() }
        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe true }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe true }
    }

    @Test
    fun `화면이 재생성되어도 닫은 다이얼로그는 닫힌 상태로 남는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: PlaceFormState

        restorationTester.setContent {
            DiaryTheme {
                state = rememberPlaceAddFormState()
            }
        }

        composeRule.runOnIdle { state.searchDialogState.show() }
        composeRule.runOnIdle { state.searchDialogState.hide() }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe false }
    }
}
