package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.remember
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
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
    fun `TC-TAG-ADD-FEATURE-012 화면이 회전하거나 창 크기가 바뀌어도 이모지 제목 설명 컬러와 고른 연결을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        // 고른 연결은 회전 중에도 살아 있는 상태 보관자가 들고 있다.
        val linkViewModel =
            screenTestLinkViewModel(
                uiState = MutableStateFlow(TagLinkInputUiState(linkedTagList = listOf(linkedTag))),
                linkedTagIdSet = MutableStateFlow(setOf(linkedTag.id)),
            )
        restorationTester.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = linkViewModel,
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
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-025 메모리 정리 뒤 복원하면 입력 내용은 복원하고 고른 연결은 복원하지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        val linkedTag = testTag(title = fixtureText(prefix = "LinkedTag"))
        val typedTitle = fixtureText(prefix = "TagTitle")
        val typedDescription = fixtureText(prefix = "TagDescription")
        lateinit var linkViewModel: TagAddLinkViewModel
        restorationTester.setContent {
            // 메모리 정리 뒤에는 고른 연결을 들고 있던 상태 보관자도 새로 만들어진다.
            linkViewModel = remember { realLinkViewModel(tagList = listOf(linkedTag)) }
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = linkViewModel,
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }
        composeRule.runOnIdle { linkViewModel.link(id = linkedTag.id) }
        val initialColorHex = currentColorHex()
        composeRule.onNodeWithText(initialColorHex, substring = true).performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction() and hasText(initialColorHex)).performTextReplacement(BLUE_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.titleInput().performTextInput(typedTitle)
        composeRule.descriptionInput().performTextInput(typedDescription)
        composeRule.onNodeWithText(linkedTag.detail.title).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(typedTitle))
        composeRule.emojiInput().assert(hasText(TYPED_EMOJI))
        composeRule.descriptionInput().assert(hasText(typedDescription))
        composeRule.onNodeWithText(BLUE_HEX, substring = true).assertExists()
        composeRule.onNodeWithText(linkedTag.detail.title).assertDoesNotExist()
        composeRule.runOnIdle { linkViewModel.linkedTagIdSet.value.shouldBeEmpty() }
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-026 저장에 실패하면 안내 없이 작성 내용과 고른 연결을 유지하고 같은 내용으로 다시 추가할 수 있다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        val useCase = mockk<AddTagUseCase>()
        coEvery { useCase(any()) } returns Result.failure(IllegalStateException("저장 실패"))
        composeRule.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = TagAddViewModel(addTagUseCase = useCase),
                    linkViewModel =
                        screenTestLinkViewModel(
                            uiState = MutableStateFlow(TagLinkInputUiState(linkedTagList = listOf(linkedTag))),
                            linkedTagIdSet = MutableStateFlow(setOf(linkedTag.id)),
                        ),
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }
        changeColor(hex = BLUE_HEX)
        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.emojiInput().assert(hasText(TYPED_EMOJI))
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        currentColorHex() shouldBe BLUE_HEX
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val expected =
            AddTagUseCase.Parameter(
                detail =
                    TagDetail(
                        emoji = TYPED_EMOJI,
                        title = TYPED_TITLE,
                        description = TYPED_DESCRIPTION,
                        color = BLUE_COLOR,
                    ),
                linkedTagIdSet = setOf(linkedTag.id),
            )
        coVerify(exactly = 2) { useCase(expected) }
    }

    @Test
    fun `TC-TAG-ADD-DATA-008 컬러를 바꾸지 않고 추가하면 처음 제시한 컬러로 추가를 요청한다`() {
        val viewModel = screenTestViewModel()
        val detailSlot = slot<TagDetail>()
        every { viewModel.add(capture(detailSlot), any()) } just Runs
        setTagAddScreen(viewModel = viewModel)
        val initialColorHex = currentColorHex()

        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        "#%06X".format(detailSlot.captured.color.toInt() and RGB_MASK) shouldBe initialColorHex
        detailSlot.captured.description shouldBe ""
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

        changeColor(hex = BLUE_HEX)
        composeRule.inputEmoji(emoji = TYPED_EMOJI)
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        currentColorHex() shouldBe BLUE_HEX

        triggerAdd()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertDoesNotExist()
        composeRule.emojiInput().assert(hasText(DEFAULT_EMOJI_LABEL))
        composeRule.descriptionInput().assert(hasText(""))
        // 새 무작위 컬러가 고른 컬러와 우연히 같을 확률은 약 1,600만 분의 1이라 새 컬러 제시를 값의 변화로 판정한다.
        currentColorHex() shouldNotBe BLUE_HEX
        composeRule.titleInput().assertIsFocused()
    }

    private fun changeColor(hex: String) {
        val currentHex = currentColorHex()
        composeRule.onNodeWithText(currentHex, substring = true).performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction() and hasText(currentHex)).performTextReplacement(hex)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()
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

    private fun realLinkViewModel(tagList: List<Tag>): TagAddLinkViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val tagIdSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(tagList.filter { tag -> tag.id in tagIdSet }))
        }
        return TagAddLinkViewModel(pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase)
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
        private val BLUE_COLOR: Long = 0xFF0000FF.toInt().toLong()
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Tag added."
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private const val RGB_MASK = 0xFFFFFF

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
