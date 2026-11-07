package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.feature.playlist.ui.MusicTagTestTheme
import io.github.taetae98coding.diary.feature.playlist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.playlist.ui.addTagScreenTestViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenProgressTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-d238d5f0-3c05-415c-9fe4-b902861ca981 추가를 처리하는 동안 진행 표시를 보이고 끝나면 숨긴다`() {
        val uiState = MutableStateFlow(MusicAddUiState())
        setMusicAddScreen(uiState = uiState)
        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()

        composeRule.runOnIdle { uiState.value = MusicAddUiState(isInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertExists()

        composeRule.runOnIdle { uiState.value = MusicAddUiState(isInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-56ed0cae-fbed-437c-9328-febed1ba5ddb 불러오기를 처리하는 동안 불러오기 버튼의 라벨이 진행 표시로 바뀌고 끝나면 되돌아온다`() {
        val uiState = MutableStateFlow(MusicAddUiState())
        setMusicAddScreen(uiState = uiState)
        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertExists()

        composeRule.runOnIdle { uiState.value = MusicAddUiState(isLinkFetchInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertDoesNotExist()

        composeRule.runOnIdle { uiState.value = MusicAddUiState(isLinkFetchInProgress = false) }
        composeRule.waitForIdle()

        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FETCH_BUTTON_LABEL).assertExists()
    }

    private fun setMusicAddScreen(uiState: MutableStateFlow<MusicAddUiState>) {
        val viewModel = mockk<MusicAddViewModel>(relaxed = true)
        every { viewModel.uiState } returns uiState
        every { viewModel.effect } returns emptyFlow()

        composeRule.setContent {
            MusicTagTestTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun progressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)

    private companion object {
        const val DEFAULT_FETCH_BUTTON_LABEL = "Fetch"
    }
}
