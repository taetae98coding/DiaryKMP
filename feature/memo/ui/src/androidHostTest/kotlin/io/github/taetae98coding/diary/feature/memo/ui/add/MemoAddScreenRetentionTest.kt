package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-ADD-FEATURE-066 화면이 다시 만들어져 복원되어도 입력한 제목, 설명, 컬러, 기간을 유지한다`() {
        val title = "title-${fixtureMonkey.giveMeOne<String>()}"
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toDefaultDisplayText()
        val viewModels = screenTestViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MemoAddScreenTestTheme {
                TestMemoAddScreen(viewModels = viewModels)
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(title)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()
        val color = composeRule.colorHexText()

        // 화면 구성 변경과 메모리 정리 뒤 복원은 모두 저장해 둔 화면 상태로 입력 내용을 다시 채운다.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(title))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.colorHexText() shouldBe color
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onAllNodesWithText(today).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-067 저장에 실패하면 안내 없이 작성 내용을 유지하고 진행 상태만 해제한다`() {
        val title = "title-${fixtureMonkey.giveMeOne<String>()}"
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val addMemoUseCase = mockk<AddMemoUseCase>()
        coEvery { addMemoUseCase(any<AddMemoUseCase.Parameter>()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        val viewModels = screenTestRealViewModel(addMemoUseCase = addMemoUseCase)
        composeRule.setContent {
            MemoAddScreenTestTheme {
                TestMemoAddScreen(viewModels = viewModels)
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(title)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        val color = composeRule.colorHexText()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(title))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.colorHexText() shouldBe color
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Memo added."
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private val DEFAULT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

        private fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"
    }
}

@Composable
private fun TestMemoAddScreen(viewModels: MemoAddScreenViewModels) {
    MemoAddScreen(
        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
        addViewModel = viewModels.viewModel,
        tagViewModel = viewModels.tagViewModel,
        webViewModel = viewModels.webViewModel,
        contactViewModel = viewModels.contactViewModel,
        placeViewModel = viewModels.placeViewModel,
        placeMapViewModel = screenTestPlaceMapViewModel(),
        geminiViewModel = screenTestGeminiViewModel(),
        navigateUp = {},
        navigateToTagAdd = {},
        navigateToTagDetail = {},
        navigateToWebAdd = {},
        navigateToWebDetail = {},
        navigateToContactAdd = {},
        navigateToContactDetail = {},
        navigateToPlaceAdd = {},
        navigateToPlaceDetail = {},
        initialDateRange = null,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
        isStandalone = true,
    )
}
