package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.add.colorHexText
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiStep
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenFormTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-007 화면이 재생성되어도 수정 중이던 컬러와 기간이 유지된다`() {
        val title = randomText(prefix = TITLE_PREFIX)
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(title))))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()
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

        composeRule.colorHexText() shouldBe editedColor
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-087 작성 도우미로 반영한 내용이 저장된 내용과 다르면 수정 동작이 나타난다`() {
        val savedTitle = randomText(prefix = SAVED_PREFIX)
        val draftTitle = randomText(prefix = DRAFT_PREFIX)
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(savedTitle))))
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        composeRule.setContent { Detail(viewModel = viewModel, geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = MemoDraft(title = draftTitle, description = "", dateTime = null))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Apply $TITLE_LABEL").performClick()
        geminiUiState.value = MemoGeminiUiState()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(draftTitle))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
        composeRule.onAllNodesWithText(savedTitle).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-089 수정에 성공해 입력이 저장된 내용과 같아지면 수정 동작이 사라진다`() {
        val savedTitle = randomText(prefix = SAVED_PREFIX)
        val suffix = EDITED_PREFIX + fixtureMonkey.giveMeOne<Int>()
        val uiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(savedTitle)))
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(uiState = uiState, effect = effectChannel.receiveAsFlow())
        // 수정이 저장되면 저장된 내용이 입력한 내용으로 바뀌어 다시 조회된다.
        every { viewModel.update(any()) } answers {
            uiState.value = memoDetailUiState(id = FIRST_MEMO_ID, detail = firstArg())
            effectChannel.trySend(MemoDetailEffect.UpdateSucceeded).getOrThrow()
        }
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(suffix)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(savedTitle + suffix))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-DATA-013 입력을 수정한 채 복사해도 수정 중이던 내용을 복사 요청에 싣지 않는다`() {
        val savedTitle = randomText(prefix = SAVED_PREFIX)
        val suffix = EDITED_PREFIX + fixtureMonkey.giveMeOne<Int>()
        val viewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(savedTitle))))
        every { viewModel.copy() } just Runs
        composeRule.setContent { Detail(viewModel = viewModel) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(suffix)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        // 복사 요청은 상세 대상만 가리키고, 저장된 내용은 복사하는 쪽이 원본에서 읽는다.
        verify(exactly = 1) { viewModel.copy() }
        verify(exactly = 0) { viewModel.update(any<MemoDetail>()) }
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-090 내용을 표시한 뒤 조회가 잠시 끊겨도 수정 중이던 내용을 유지한다`() {
        val savedTitle = randomText(prefix = SAVED_PREFIX)
        val description = randomText(prefix = DESCRIPTION_PREFIX)
        val suffix = EDITED_PREFIX + fixtureMonkey.giveMeOne<Int>()
        val content = memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(savedTitle))
        val uiState = MutableStateFlow<MemoDetailUiState>(content)
        composeRule.setContent { Detail(viewModel = screenTestViewModel(uiState = uiState)) }
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(suffix)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        composeRule.waitForIdle()
        val savedColor = composeRule.colorHexText()
        val editedColor = generateSequence { randomColorHex() }.first { color -> color != savedColor }
        composeRule.onNode(hasText(savedColor, substring = true) and hasClickAction()).performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(editedColor)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()

        uiState.value = MemoDetailUiState.Loading
        composeRule.waitForIdle()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        uiState.value = content
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(savedTitle + suffix))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.colorHexText() shouldBe editedColor
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Composable
    private fun Detail(
        viewModel: MemoDetailViewModel,
        geminiViewModel: MemoGeminiViewModel = screenTestGeminiViewModel(),
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
                geminiViewModel = geminiViewModel,
                navigateUp = {},
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
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private const val DEFAULT_CONFIRM = "Confirm"
        private const val TITLE_LABEL = "Title"
        private const val COLOR_RGB_MASK = 0xFFFFFF
        private const val COLOR_HEX_LENGTH = 6
        private const val TITLE_PREFIX = "title-"
        private const val SAVED_PREFIX = "saved-"
        private const val DRAFT_PREFIX = "draft-"
        private const val DESCRIPTION_PREFIX = "description-"
        private const val EDITED_PREFIX = "edited-"

        // 생성한 문자열은 비어 있을 수 있으므로 앞에 고정 문자열을 붙여 빈 값이 되지 않게 한다.
        private fun randomText(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>()

        private fun randomColorHex(): String =
            "#" +
                (fixtureMonkey.giveMeOne<Int>() and COLOR_RGB_MASK)
                    .toString(radix = 16)
                    .uppercase()
                    .padStart(length = COLOR_HEX_LENGTH, padChar = '0')

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}
