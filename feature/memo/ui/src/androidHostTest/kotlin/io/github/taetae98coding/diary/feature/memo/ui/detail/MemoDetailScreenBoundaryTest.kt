package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.add.colorHexText
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-080 제목을 비우고 수정해도 성공을 안내하고 수정 동작을 계속 제공한다`() {
        assertBlankTitleUpdate(typedTitle = "")
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-080 제목에 공백만 두고 수정해도 성공을 안내하고 수정 동작을 계속 제공한다`() {
        assertBlankTitleUpdate(typedTitle = BLANK_TITLE)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-081 다른 앱에 다녀와도 수정 중이던 내용이 그대로다`() {
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))))
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        composeRule.waitForIdle()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-082 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        val uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
        composeRule.setContent { Detail(viewModel = screenTestViewModel(uiState = uiState)) }
        composeRule.waitForIdle()

        uiState.value = memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE))
        composeRule.onAllNodes(hasSetTextAction() and isFocused()).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-083 수정 저장에 실패하면 안내 없이 입력을 유지하고 진행 상태만 해제한다`() {
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))))
        every { viewModel.update(any()) } just Runs
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        verify(exactly = 2) { viewModel.update(any()) }
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-084 시스템이 앱을 정리한 뒤 화면을 복원해도 수정 중이던 내용을 복원한다`() {
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val uiStateList = mutableListOf<MutableStateFlow<MemoDetailUiState>>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 화면 상태를 들고 있던 객체도 새로 만들어져 메모를 다시 조회하므로, 복원할 때마다 로딩부터 시작하는 새 인스턴스를 쓴다.
            val viewModel =
                remember {
                    val uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
                    uiStateList.add(uiState)
                    screenTestViewModel(uiState = uiState)
                }

            Detail(viewModel = viewModel)
        }
        composeRule.runOnIdle { uiStateList.last().value = memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        val savedColor = composeRule.colorHexText()
        val editedColor = generateSequence { randomColorHex() }.first { color -> color != savedColor }
        composeRule.onNode(hasText(savedColor, substring = true) and hasClickAction()).performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(editedColor)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.colorHexText() shouldBe editedColor

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        uiStateList.size shouldBe 2
        composeRule.runOnIdle { uiStateList.last().value = memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)) }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.colorHexText() shouldBe editedColor
        composeRule.colorHexText() shouldNotBe savedColor
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-085 삭제 저장에 실패하면 안내 없이 화면에 남는다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))))
        // 저장에 실패하면 삭제 성공 Effect가 오지 않는다.
        every { viewModel.delete() } just Runs
        composeRule.setContent { Detail(viewModel = viewModel, navigateUp = { navigateUpCount += 1 }) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.delete() }
        navigateUpCount shouldBe 0
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE))
        assertNoFeedbackShown()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-086 완료 저장에 실패하면 안내 없이 완료 동작이 그대로다`() {
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = false)))
        every { viewModel.finish() } just Runs
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.finish() }
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
        assertNoFeedbackShown()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-086 다시 시작 저장에 실패하면 안내 없이 다시 시작 동작이 그대로다`() {
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = true)))
        every { viewModel.restart() } just Runs
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.restart() }
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        assertNoFeedbackShown()
    }

    // 스낵바는 닫기 동작을 제공하므로, 닫기 동작을 가진 노드와 알려진 안내 문구가 없으면 안내가 표시되지 않은 것이다.
    private fun assertNoFeedbackShown() {
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).assertCountEquals(0)
        FEEDBACK_MESSAGE_LIST.forEach { message -> composeRule.onNodeWithText(message).assertDoesNotExist() }
    }

    private fun assertBlankTitleUpdate(typedTitle: String) {
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))),
                effect = effectChannel.receiveAsFlow(),
            )
        val updatedTitleList = mutableListOf<String>()
        every { viewModel.update(any()) } answers {
            updatedTitleList.add(firstArg<MemoDetail>().title)
            effectChannel.trySend(MemoDetailEffect.UpdateSucceeded).getOrThrow()
        }
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextClearance()
        if (typedTitle.isNotEmpty()) {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(typedTitle)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        updatedTitleList shouldBe listOf(typedTitle)
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(typedTitle))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Composable
    private fun Detail(
        viewModel: MemoDetailViewModel,
        navigateUp: () -> Unit = {},
    ) {
        MemoDetailScreenTestTheme {
            MemoDetailScreen(
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                detailViewModel = viewModel,
                tagViewModel = screenTestTagViewModel(),
                webViewModel = screenTestWebViewModel(),
                contactViewModel = screenTestContactViewModel(),
                placeViewModel = screenTestPlaceViewModel(),
                placeMapViewModel = screenTestPlaceMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
                navigateUp = navigateUp,
                navigateToCopiedMemo = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToWebAdd = {},
                navigateToWebDetail = {},
                navigateToContactAdd = {},
                navigateToContactDetail = {},
                navigateToPlaceAdd = {},
                navigateToPlaceDetail = {},
                componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }

    private companion object {
        private const val BLANK_TITLE = "   "
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Memo updated."
        private const val DEFAULT_CONFIRM = "Confirm"
        private val FEEDBACK_MESSAGE_LIST: List<String> =
            listOf(DEFAULT_UPDATE_SUCCEEDED_MESSAGE, DEFAULT_COPY_SUCCEEDED_MESSAGE, "Memo finished.", "Memo restarted.", "Memo deleted.", "Undo")
        private const val COLOR_RGB_MASK = 0xFFFFFF
        private const val COLOR_HEX_LENGTH = 6

        private fun randomColorHex(): String =
            "#" +
                (fixtureMonkey.giveMeOne<Int>() and COLOR_RGB_MASK)
                    .toString(radix = 16)
                    .uppercase()
                    .padStart(length = COLOR_HEX_LENGTH, padChar = '0')

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}
