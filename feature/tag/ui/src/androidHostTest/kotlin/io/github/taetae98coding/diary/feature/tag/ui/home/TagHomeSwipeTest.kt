package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomeSwipeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-039 태그 카드를 좌에서 우로 밀면 완료을 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val environment = setScreen()

        swipeRight(title = FIRST_TITLE)

        verify(exactly = 1) { environment.viewModel.finish(id = environment.first.id) }
        composeRule.onNodeWithText(DEFAULT_START_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-039 한국어 환경에서 완료 안내와 실행 취소를 표시한다`() {
        setScreen()

        swipeRight(title = FIRST_TITLE)

        composeRule.onNodeWithText(KOREAN_START_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-040 태그 카드를 우에서 좌로 밀면 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val environment = setScreen()

        swipeLeft(title = FIRST_TITLE)

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-040 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        setScreen()

        swipeLeft(title = FIRST_TITLE)

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-041 완료을 실행 취소하면 그 태그의 다시 시작를 요청한다`() {
        val environment = setScreen()

        swipeRight(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restart(id = environment.first.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-042 삭제를 실행 취소하면 그 태그의 삭제 되돌리기를 요청한다`() {
        val environment = setScreen()

        swipeLeft(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
        verify(exactly = 0) { environment.viewModel.restart(id = any()) }
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-044 안내가 보이는 동안 다른 태그를 삭제하면 마지막 동작에만 실행 취소가 적용된다`() {
        val environment = setScreen()

        swipeRight(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_START_MESSAGE).assertExists()

        environment.effectChannel.trySend(TagListEffect.Deleted(id = environment.second.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_START_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.id) }
        verify(exactly = 0) { environment.viewModel.restart(id = any()) }
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-045 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setScreen()

        swipeLeft(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-047 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setScreen(isShownProvider = { isShown })

        swipeLeft(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `목록에서 태그를 완료하거나 삭제해도 태그 상세로 이동을 요청하지 않는다`() {
        val navigatedIdList = mutableListOf<Uuid>()
        val environment = setScreen(navigateToDetail = navigatedIdList::add)

        swipeRight(title = FIRST_TITLE)
        swipeLeft(title = SECOND_TITLE)

        verify(exactly = 1) { environment.viewModel.finish(id = environment.first.id) }
        verify(exactly = 1) { environment.viewModel.delete(id = environment.second.id) }
        navigatedIdList shouldBe emptyList()
    }

    private fun swipeRight(title: String) {
        composeRule.onNode(hasTestTag(TAG_CARD_TEST_TAG) and hasText(title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
    }

    private fun swipeLeft(title: String) {
        composeRule.onNode(hasTestTag(TAG_CARD_TEST_TAG) and hasText(title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun setScreen(
        isShownProvider: () -> Boolean = { true },
        navigateToDetail: (Uuid) -> Unit = {},
    ): Environment {
        val first = fixtureMonkey.tag(title = FIRST_TITLE, isFinished = false)
        val second = fixtureMonkey.tag(title = SECOND_TITLE, isFinished = false)
        val effectChannel = Channel<TagListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<TagHomeViewModel>()
        every { viewModel.filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState())
        every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(listOf(first, second)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.finish(id = any()) } answers {
            effectChannel.trySend(TagListEffect.Finished(id = firstArg())).getOrThrow()
        }
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(TagListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restart(id = any()) }
        justRun { viewModel.restore(id = any()) }

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    TagHomeScreen(
                        navigateToAdd = {},
                        navigateToDetail = navigateToDetail,
                        navigateToFilter = {},
                        navigateToFinishedList = {},
                        navigateToSearch = {},
                        componentVisibleProvider = { TagHomeScaffoldComponentVisible() },
                        gridState = rememberLazyGridState(),
                        tagViewModel = viewModel,
                        syncViewModel = screenTestSyncViewModel(),
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
        val viewModel: TagHomeViewModel,
        val effectChannel: Channel<TagListEffect>,
        val first: Tag,
        val second: Tag,
    )

    private companion object {
        const val FIRST_TITLE = "TagHomeSwipeTestFirst"
        const val SECOND_TITLE = "TagHomeSwipeTestSecond"
        const val DEFAULT_START_MESSAGE = "Tag finished."
        const val KOREAN_START_MESSAGE = "태그가 완료되었습니다."
        const val DEFAULT_DELETED_MESSAGE = "Tag deleted."
        const val KOREAN_DELETED_MESSAGE = "태그가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
