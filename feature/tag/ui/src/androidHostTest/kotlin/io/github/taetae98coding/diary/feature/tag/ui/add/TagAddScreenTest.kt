package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-ADD-FEATURE-015 진입하면 제목 입력 칸에 초점이 맞춰지고 이모지 입력에는 맞춰지지 않는다`() {
        setTagAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
        composeRule.emojiInput().assertIsNotFocused()
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-006 뒤로가기 버튼을 선택하면 TagHome으로 돌아가는 행동을 실행한다`() {
        var navigateUpCount = 0
        setTagAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-007 추가 버튼으로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        }
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-007 단축키로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.titleInput().performKeyInput {
                keyDown(Key.MetaLeft)
                keyDown(Key.Enter)
                keyUp(Key.Enter)
                keyUp(Key.MetaLeft)
            }
        }
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-010 제목이 비어 있으면 설명과 컬러를 유지하고 제목에 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialTitle = "")
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-010 제목이 공백이면 모든 입력을 유지하고 제목에 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialTitle = WHITESPACE_TITLE)
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-016 추가에 성공하면 제목 입력으로 초점을 옮긴다`() {
        assertFocusMovesToTitle(effect = TagAddEffect.AddSucceeded(id = Uuid.random()))
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-017 제목 미입력으로 추가하면 제목 입력으로 초점을 옮긴다`() {
        assertFocusMovesToTitle(effect = TagAddEffect.TitleBlank)
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-012 화면이 재생성되어도 제목 설명 컬러를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        restorationTester.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = screenTestLinkViewModel(),
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }

        val initialColorHex = currentColorHex()
        composeRule.onNodeWithText(initialColorHex, substring = true).performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction() and hasText(initialColorHex)).performTextReplacement(BLUE_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.onNode(hasHexText() and hasClickAction()).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
        composeRule.emojiInput().assert(hasText(TYPED_EMOJI))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.onNodeWithText(BLUE_HEX, substring = true).assertExists()
    }

    private fun assertFocusMovesToTitle(effect: TagAddEffect) {
        val effectChannel = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        setTagAddScreen(viewModel = viewModel)

        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.descriptionInput().assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
        composeRule.descriptionInput().assertIsNotFocused()
    }

    private fun assertSuccessResetsInput(triggerAdd: () -> Unit) {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow() }
        setTagAddScreen(viewModel = viewModel)

        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)

        triggerAdd()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertDoesNotExist()
        composeRule.emojiInput().assert(hasText(DEFAULT_EMOJI_LABEL))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.onNode(hasHexText() and hasClickAction()).assertExists()
        composeRule.titleInput().assertIsFocused()
    }

    private fun assertTitleBlankRetainsInput(initialTitle: String) {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.TitleBlank).getOrThrow() }
        setTagAddScreen(viewModel = viewModel)

        if (initialTitle.isNotEmpty()) {
            composeRule.titleInput().performTextInput(initialTitle)
        }
        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        val initialColorHex = currentColorHex()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), any()) }
        composeRule
            .titleInput()
            .assert(hasText(initialTitle))
            .assertIsFocused()
        composeRule.emojiInput().assert(hasText(TYPED_EMOJI))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.onNodeWithText(initialColorHex, substring = true).assertExists()
    }

    private fun currentColorHex(): String =
        composeRule
            .onNode(hasHexText() and hasClickAction())
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .firstNotNullOf { text -> hexRegex.find(text.text)?.value }

    private fun setTagAddScreen(
        viewModel: TagAddViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = navigateUp,
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = screenTestLinkViewModel(),
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }
    }

    public companion object {
        private const val TYPED_TITLE = "TagTitleInput"
        private const val TYPED_DESCRIPTION = "TagDescriptionInput"
        private const val TYPED_EMOJI = "\uD83C\uDFC3"
        private const val WHITESPACE_TITLE = "   "
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private const val DEFAULT_CONFIRM = "Confirm"
        private const val BLUE_HEX = "#0000FF"

        private val hexRegex = Regex(pattern = "#[0-9A-F]{6}")

        private fun hasHexText(): SemanticsMatcher =
            SemanticsMatcher(description = "Hex color text") { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    ?.any { hexRegex.containsMatchIn(it.text) } == true
            }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagAddScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-ADD-FEATURE-008 기본 환경 추가 성공 안내`() {
        assertMessage(effect = TagAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = DEFAULT_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-ADD-FEATURE-008 한국어 추가 성공 안내`() {
        assertMessage(effect = TagAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-011 기본 환경 제목 미입력 안내`() {
        assertMessage(effect = TagAddEffect.TitleBlank, expectedMessage = DEFAULT_TITLE_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-ADD-FEATURE-011 한국어 제목 미입력 안내`() {
        assertMessage(effect = TagAddEffect.TitleBlank, expectedMessage = KOREAN_TITLE_BLANK_MESSAGE)
    }

    private fun assertMessage(
        effect: TagAddEffect,
        expectedMessage: String,
    ) {
        val effectChannel = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = screenTestLinkViewModel(),
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }

        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.titleInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    public companion object {
        private const val TYPED_TITLE = "TagTitleInput"
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Tag added."
        private const val KOREAN_ADD_SUCCEEDED_MESSAGE = "태그가 추가되었습니다."
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private const val KOREAN_TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
    }
}
