package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import io.mockk.every
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 완료·다시 시작·삭제는 선택한 탭과 관계없이 상단 바에서 실행하므로, 실제 탭 배선을 거치는 화면 단위로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailActionTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-062 메모 탭에서 완료해도 메모 탭이 선택된 채로 다시 시작 동작이 제공된다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = false))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.finish() } answers { uiState.value = uiState.value.copy(isFinished = true) }
        composeRule.setTagDetailScreen(viewModel = viewModel)
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-062 웹 탭에서 다시 시작해도 웹 탭이 선택된 채로 완료 동작이 제공된다`() {
        val uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = true))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.restart() } answers { uiState.value = uiState.value.copy(isFinished = false) }
        composeRule.setTagDetailScreen(viewModel = viewModel)
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-062 장소 탭에서 삭제하면 탭과 관계없이 진입하기 전 화면으로 돌아가는 동작이 한 번 실행된다`() {
        var navigateUpCount = 0
        val effectChannel = Channel<TagDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE), isFinished = false)),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.delete() } answers { effectChannel.trySend(TagDetailEffect.DeleteSucceeded).getOrThrow() }
        composeRule.setTagDetailScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
    }
}
