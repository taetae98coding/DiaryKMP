package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
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
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-025 웹 카드를 삭제 방향으로 밀면 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val environment = setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-HOME-FEATURE-025 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-027 웹 카드를 반대 방향으로 밀면 삭제를 요청하지 않고 안내도 표시하지 않는다`() {
        val environment = setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-026 삭제를 실행 취소하면 그 웹 항목의 삭제 되돌리기를 요청한다`() {
        val environment = setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-029 안내가 보이는 동안 다른 웹 항목을 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val environment = setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        environment.effectChannel.trySend(WebListEffect.Deleted(id = environment.second.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-030 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setWebHomeScreen()

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-032 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setWebHomeScreen(isShownProvider = { isShown })

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    private fun setWebHomeScreen(isShownProvider: () -> Boolean = { true }): Environment {
        val first = testWeb(title = FIRST_TITLE)
        val second = testWeb(title = SECOND_TITLE)
        val effectChannel = Channel<WebListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<WebHomeViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { viewModel.webPagingData } returns MutableStateFlow(webPagingDataOf(listOf(first, second)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(WebListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restore(id = any()) }
        val syncViewModel = mockk<WebHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(WebHomeUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    WebHomeScreen(
                        navigateUp = {},
                        navigateToSearch = {},
                        navigateToAdd = {},
                        navigateToDetail = {},
                        componentVisibleProvider = { WebHomeScaffoldComponentVisible() },
                        webViewModel = viewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        return Environment(viewModel = viewModel, effectChannel = effectChannel, first = first, second = second)
    }

    private class Environment(
        val viewModel: WebHomeViewModel,
        val effectChannel: Channel<WebListEffect>,
        val first: Web,
        val second: Web,
    )

    private companion object {
        const val FIRST_TITLE = "WebHomeDeleteFirst"
        const val SECOND_TITLE = "WebHomeDeleteSecond"
        const val DEFAULT_DELETED_MESSAGE = "Web deleted."
        const val KOREAN_DELETED_MESSAGE = "웹이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
