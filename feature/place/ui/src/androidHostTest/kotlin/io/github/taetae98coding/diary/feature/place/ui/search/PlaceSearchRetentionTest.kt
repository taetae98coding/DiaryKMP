package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
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
    fun `TC-df90467f-d1a1-47b9-a47a-3c17b3d87f8c 화면이 재생성되어도 다이얼로그가 열린 상태로 남는다`() {
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

    @Test
    fun `TC-777314f9-d099-4fe6-96f4-05652d702f7a PlaceAdd 화면을 떠났다 다시 들어오면 다이얼로그가 닫힌 상태로 시작한다`() {
        assertDialogClosedAfterReentry { rememberPlaceAddFormState() }
    }

    @Test
    fun `TC-777314f9-d099-4fe6-96f4-05652d702f7a PlaceDetail 화면을 떠났다 다시 들어오면 다이얼로그가 닫힌 상태로 시작한다`() {
        assertDialogClosedAfterReentry { rememberPlaceDetailFormState(initialDetail = PlaceDetail.EMPTY) }
    }

    private fun assertDialogClosedAfterReentry(rememberState: @Composable () -> PlaceFormState) {
        var isEntered by mutableStateOf(true)
        lateinit var state: PlaceFormState

        composeRule.setContent {
            DiaryTheme {
                if (isEntered) state = rememberState()
            }
        }
        composeRule.runOnIdle { state.searchDialogState.show() }
        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe true }

        composeRule.runOnIdle { isEntered = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isEntered = true }

        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe false }
    }
}
