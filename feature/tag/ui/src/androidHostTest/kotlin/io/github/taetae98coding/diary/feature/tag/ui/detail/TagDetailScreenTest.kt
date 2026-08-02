package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-001 첫진입 시 조회한 제목이 상단 바와 입력 칸에 채워진다`() {
        val uiState = MutableStateFlow<TagDetailUiState>(TagDetailUiState.Loading)
        setTagDetailScreen(screenTestViewModel(uiState = uiState))
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        uiState.value = tagDetailUiState(detail = tagDetail(TAG_TITLE))

        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TAG_TITLE))
        composeRule.onAllNodesWithText(TAG_TITLE).assertCountEquals(2)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-022 조회할 수 없으면 로딩 상태를 유지한다`() {
        setTagDetailScreen(screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-003 뒤로가기 버튼을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setTagDetailScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-004 입력 칸을 수정해도 상단 바 제목은 유지된다`() {
        setTagDetailScreen(titleLoadedViewModel())
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodesWithText(TAG_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-005 화면 재생성 후에도 수정 중이던 내용이 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = titleLoadedViewModel()
        restorationTester.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodesWithText(TAG_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-007 다른 태그를 선택하면 새 태그 내용으로 바뀐다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE)))
        setTagDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        uiState.value = tagDetailUiState(id = SECOND_TAG_ID, detail = tagDetail(SECOND_TAG_TITLE))
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(SECOND_TAG_TITLE))
        composeRule.onAllNodesWithText(SECOND_TAG_TITLE).assertCountEquals(2)
        composeRule.onNodeWithText(TAG_TITLE + EDIT_SUFFIX).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-008 같은 태그 제목 변경은 상단 바에 반영하고 입력값은 유지한다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE)))
        setTagDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        uiState.value = tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(CHANGED_TAG_TITLE))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CHANGED_TAG_TITLE).assertExists()
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    private fun setTagDetailScreen(
        viewModel: TagDetailViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = navigateUp,
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
    }

    public companion object {
        private const val REMOVED_DEFAULT_TITLE = "Tag"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val SECOND_TAG_TITLE = "SecondTagDetailTitle"
        private const val CHANGED_TAG_TITLE = "ChangedTagDetailTitle"
        private val SECOND_TAG_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")

        private fun titleLoadedViewModel(): TagDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE))))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScreenUpdateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-009 수정 버튼으로 수정을 실행해도 수정 중이던 내용이 유지된다`() {
        val viewModel = titleLoadedViewModel()
        every { viewModel.update(any()) } just Runs
        setTagDetailScreen(viewModel)
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-009 단축키로 수정을 실행해도 수정 중이던 내용이 유지된다`() {
        val viewModel = titleLoadedViewModel()
        every { viewModel.update(any()) } just Runs
        setTagDetailScreen(viewModel)
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.titleInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-014 수정 버튼은 입력이 저장 내용과 다를 때만 나타난다`() {
        setTagDetailScreen(titleLoadedViewModel())
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-011 수정을 처리하는 동안 수정 버튼이 진행 표시로 바뀐다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isInProgress = true))
        setTagDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun setTagDetailScreen(viewModel: TagDetailViewModel) {
        composeRule.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
    }

    public companion object {
        private fun titleLoadedViewModel(): TagDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE))))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScreenActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-016 완료되지 않은 태그를 완료하면 완료 버튼이 다시 시작 동작으로 바뀐다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = false))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.finish() } answers { uiState.value = uiState.value.copy(isFinished = true) }
        setTagDetailScreen(viewModel)
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-017 완료된 태그를 다시 시작하면 완료 버튼이 완료 동작으로 바뀐다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = true))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.restart() } answers { uiState.value = uiState.value.copy(isFinished = false) }
        setTagDetailScreen(viewModel)
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-019 태그를 삭제하면 뒤로가기와 같은 동작으로 화면에서 빠져나간다`() {
        var navigateUpCount = 0
        val effectChannel = Channel<TagDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = false)),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.delete() } answers { effectChannel.trySend(TagDetailEffect.DeleteSucceeded).getOrThrow() }
        setTagDetailScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    private fun setTagDetailScreen(
        viewModel: TagDetailViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = navigateUp,
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경 수정 버튼 접근성 이름`() {
        assertUpdateButtonDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 수정 버튼 접근성 이름`() {
        assertUpdateButtonDescription(KOREAN_UPDATE_BUTTON_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-010 기본 환경 수정 성공 안내`() {
        assertMessage(effect = TagDetailEffect.UpdateSucceeded, expectedMessage = DEFAULT_UPDATE_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-FEATURE-010 한국어 수정 성공 안내`() {
        assertMessage(effect = TagDetailEffect.UpdateSucceeded, expectedMessage = KOREAN_UPDATE_SUCCEEDED_MESSAGE)
    }

    private fun assertUpdateButtonDescription(expectedDescription: String) {
        composeRule.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = titleLoadedViewModel(),
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(expectedDescription).assertExists()
    }

    private fun assertMessage(
        effect: TagDetailEffect,
        expectedMessage: String,
    ) {
        val effectChannel = Channel<TagDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE))),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.update(any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }

        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
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
        private const val KOREAN_UPDATE_BUTTON_DESCRIPTION = "태그 수정"
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Tag updated."
        private const val KOREAN_UPDATE_SUCCEEDED_MESSAGE = "태그가 수정되었습니다."

        private fun titleLoadedViewModel(): TagDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE))))
    }
}
