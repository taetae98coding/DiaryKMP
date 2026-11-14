package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MusicDetailScreenProgressTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-418fdb5f-3a74-468a-a30a-7110fb585c24 조회가 끝나기 전에는 조회 진행 표시를 보여 준다`() {
        setMusicDetailScreen(uiState = MutableStateFlow(MusicDetailUiState.Loading))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `TC-0aee8edc-ff76-4fa3-93a6-65a162a9b45b 수정에 성공하면 화면과 입력을 유지하고 성공 피드백을 표시한다`() {
        val effect = MutableSharedFlow<MusicDetailEffect>(extraBufferCapacity = 1)
        var navigateUpCount = 0
        setMusicDetailScreen(uiState = contentUiState(), effect = effect, navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { effect.tryEmit(MusicDetailEffect.UpdateSucceeded) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-f453fd31-eee2-42b8-bf14-94ab5693f604 수정을 처리하는 동안 수정 버튼이 진행 표시로 바뀐다`() {
        val uiState = contentUiState()
        setMusicDetailScreen(uiState = uiState)
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isUpdateInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isUpdateInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-abbc197b-b8ec-44d8-b4ef-e1b1ab4cfc6d 수정에 실패하면 화면과 입력을 유지하고 다시 수정할 수 있다`() {
        val uiState = contentUiState()
        var navigateUpCount = 0
        val viewModel = setMusicDetailScreen(uiState = uiState, navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()
        composeRule.clickUpdate()

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isUpdateInProgress = true) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { uiState.value = uiState.content().copy(isUpdateInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.artistInput().assert(hasText(STORED_ARTIST))
        composeRule.linkInput().assert(hasText(STORED_LINK))
        navigateUpCount shouldBe 0

        composeRule.clickUpdate()

        verify(exactly = 2) { viewModel.update(detail = testMusicDetail(title = EDITING_TITLE)) }
    }

    @Test
    fun `TC-0de5a3ea-5a17-4ad0-b04b-b0ea03a9d9d7 다시 불러오기를 처리하는 동안 불러오기 버튼이 진행 표시로 바뀐다`() {
        val uiState = contentUiState()
        setMusicDetailScreen(uiState = uiState)
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertExists()

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isLinkFetchInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertDoesNotExist()

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isLinkFetchInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertExists()
    }

    @Test
    fun `TC-5821617e-dd84-4d81-a046-c09757c3cee6 삭제를 처리하는 동안 삭제 버튼이 진행 표시로 바뀌고 다른 동작은 실행할 수 있다`() {
        val uiState = contentUiState()
        val viewModel = setMusicDetailScreen(uiState = uiState)

        composeRule.runOnIdle { uiState.value = uiState.content().copy(isDeleteInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())

        composeRule.titleInput().performTextReplacement(EDITING_TITLE)
        composeRule.waitForIdle()
        composeRule.titleInput().assert(hasText(EDITING_TITLE))
        composeRule.clickUpdate()
        composeRule.clickFetch()

        verify(exactly = 1) { viewModel.update(detail = testMusicDetail(title = EDITING_TITLE)) }
        verify(exactly = 1) { viewModel.fetchLink(link = STORED_LINK) }
    }

    @Test
    fun `TC-4deab72f-3495-4fda-ac27-98130928f0a0 삭제에 실패하면 화면에 머무르고 진행 표시만 해제되어 다시 삭제할 수 있다`() {
        val uiState = contentUiState()
        var navigateUpCount = 0
        val viewModel = setMusicDetailScreen(uiState = uiState, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClickAndWait()
        composeRule.runOnIdle { uiState.value = uiState.content().copy(isDeleteInProgress = true) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { uiState.value = uiState.content().copy(isDeleteInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(STORED_TITLE))
        navigateUpCount shouldBe 0

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClickAndWait()

        verify(exactly = 2) { viewModel.delete() }
    }

    private fun SemanticsNodeInteraction.performClickAndWait() {
        performClick()
        composeRule.waitForIdle()
    }

    private fun MutableStateFlow<MusicDetailUiState>.content(): MusicDetailUiState.Content = value as MusicDetailUiState.Content

    private fun contentUiState(): MutableStateFlow<MusicDetailUiState> = MutableStateFlow(MusicDetailUiState.Content(id = Uuid.random(), detail = testMusicDetail()))

    private fun setMusicDetailScreen(
        uiState: MutableStateFlow<MusicDetailUiState>,
        effect: Flow<MusicDetailEffect> = emptyFlow(),
        navigateUp: () -> Unit = {},
    ): MusicDetailViewModel {
        val viewModel = detailViewModel(uiState = uiState, effect = effect)

        composeRule.setContent {
            MusicTagTestTheme {
                MusicDetailScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { MusicDetailScaffoldComponentVisible() },
                    musicViewModel = viewModel,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = detailTagScreenTestViewModel(),
                    contentSharerFactory = testContentSharerFactory(),
                )
            }
        }
        composeRule.waitForIdle()

        return viewModel
    }

    private companion object {
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Music updated."
        private const val DEFAULT_FETCH_BUTTON_LABEL = "Fetch"
    }
}
