package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-022 태그 내용이 준비되지 않으면 로딩 상태를 표시한다`() {
        setTagDetailScaffold(uiStateProvider = { TagDetailUiState.Loading })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(REMOVED_KOREAN_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-001 제목이 조회되면 상단 바에 조회한 제목을 표시한다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_DETAIL_TITLE)) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(TAG_DETAIL_TITLE).assertExists()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 뒤로가기 버튼 이름은 뒤로가기이다`() {
        setTagDetailScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 뒤로가기 버튼 이름은 Navigate up이다`() {
        setTagDetailScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setTagDetailScaffold(
            uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_DETAIL_TITLE)) },
            componentVisibleProvider = { TagDetailScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(TAG_DETAIL_TITLE).assertExists()
    }

    private fun setTagDetailScaffold(
        uiStateProvider: () -> TagDetailUiState = { tagDetailUiState() },
        componentVisibleProvider: () -> TagDetailScaffoldComponentVisible = { TagDetailScaffoldComponentVisible() },
    ) {
        composeRule.setContent {
            val state = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY)

            DiaryTheme {
                TagDetailScaffold(
                    onEvent = {},
                    uiStateProvider = uiStateProvider,
                    state = state,
                    componentVisibleProvider = componentVisibleProvider,
                    tabFloatingActionButton = {},
                ) { tab ->
                    TagDetailTestTabContent(
                        tab = tab,
                        uiStateProvider = uiStateProvider,
                        state = state,
                    )
                }
            }
        }
    }

    public companion object {
        private const val REMOVED_KOREAN_DEFAULT_TITLE = "태그"
        private const val REMOVED_DEFAULT_TITLE = "Tag"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val TAG_DETAIL_TITLE = "TagDetailTitle"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScaffoldActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-015 기본 환경 완료되지 않은 태그의 완료 버튼 이름은 Finish tag이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-015 기본 환경 완료된 태그의 완료 버튼 이름은 Restart tag이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-FEATURE-015 한국어 완료되지 않은 태그의 완료 버튼 이름은 태그 완료이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(KOREAN_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-FEATURE-015 한국어 완료된 태그의 완료 버튼 이름은 태그 다시 시작이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true) })

        composeRule.onNodeWithContentDescription(KOREAN_RESTART_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경 삭제 버튼 이름은 Delete tag이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 삭제 버튼 이름은 태그 삭제이다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(KOREAN_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setTagDetailScaffold(uiStateProvider: () -> TagDetailUiState = { tagDetailUiState() }) {
        composeRule.setContent {
            val state = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY)

            DiaryTheme {
                TagDetailScaffold(
                    onEvent = {},
                    uiStateProvider = uiStateProvider,
                    state = state,
                    tabFloatingActionButton = {},
                ) { tab ->
                    TagDetailTestTabContent(
                        tab = tab,
                        uiStateProvider = uiStateProvider,
                        state = state,
                    )
                }
            }
        }
    }

    public companion object {
        private const val KOREAN_FINISH_BUTTON_DESCRIPTION = "태그 완료"
        private const val KOREAN_RESTART_BUTTON_DESCRIPTION = "태그 다시 시작"
        private const val KOREAN_DELETE_BUTTON_DESCRIPTION = "태그 삭제"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScaffoldInProgressTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-021 완료를 처리하는 동안 완료 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinishInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-021 다시 시작을 처리하는 동안 다시 시작 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true, isFinishInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertIsOn()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-021 삭제를 처리하는 동안 삭제 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isDeleteInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertIsOff()
    }

    private fun setTagDetailScaffold(uiStateProvider: () -> TagDetailUiState = { tagDetailUiState() }) {
        composeRule.setContent {
            val state = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY)

            DiaryTheme {
                TagDetailScaffold(
                    onEvent = {},
                    uiStateProvider = uiStateProvider,
                    state = state,
                    tabFloatingActionButton = {},
                ) { tab ->
                    TagDetailTestTabContent(
                        tab = tab,
                        uiStateProvider = uiStateProvider,
                        state = state,
                    )
                }
            }
        }
    }
}
