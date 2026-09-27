@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.TAG_HOME_CONTENT_KEY
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.navigateToTagDetail
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagHomeListPaneMetadata
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldContainExactly
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

// 목록에서 민 결과가 상세 영역에 놓인 화면을 바꾸는지는 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagHomeListDetailSwipeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-048 목록·상세 배치에서 상세에 열린 태그를 목록에서 완료해도 상세 영역의 화면이 그대로 남는다`() {
        val environment = setListDetail()

        composeRule.onNode(hasTestTag(TAG_CARD_TEST_TAG) and hasText(OPENED_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.opened.id) }
        environment.assertDetailRetained()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-048 목록·상세 배치에서 상세에 열린 태그를 목록에서 삭제해도 상세 영역의 화면이 그대로 남는다`() {
        val environment = setListDetail()

        composeRule.onNode(hasTestTag(TAG_CARD_TEST_TAG) and hasText(OPENED_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.opened.id) }
        environment.assertDetailRetained()
    }

    private fun Environment.assertDetailRetained() {
        backStack.toList() shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = opened.id))
        composeRule.onNodeWithText(detailContent(opened = opened)).assertExists()
    }

    private fun setListDetail(): Environment {
        val opened = fixtureMonkey.tag(title = OPENED_TITLE, isFinished = false)
        val other = fixtureMonkey.tag(title = OTHER_TITLE, isFinished = false)
        val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey, TagDetailNavKey(id = opened.id))
        val effectChannel = Channel<TagListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<TagHomeViewModel>()
        every { viewModel.filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState())
        every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(listOf(opened, other)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.finish(id = any()) } answers {
            effectChannel.trySend(TagListEffect.Finished(id = firstArg())).getOrThrow()
        }
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(TagListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restart(id = any()) }
        justRun { viewModel.restore(id = any()) }
        val syncViewModel = screenTestSyncViewModel()

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<TagHomeNavKey>(
                                clazzContentKey = { TAG_HOME_CONTENT_KEY },
                                metadata = tagHomeListPaneMetadata(backStack = backStack),
                            ) {
                                // 제품의 목록·상세 배치와 같이 목록의 태그 선택은 상세 영역으로 이어지게 둔다.
                                TagHomeScreen(
                                    navigateToAdd = {},
                                    navigateToDetail = { id -> backStack.navigateToTagDetail(id) },
                                    navigateToFilter = {},
                                    navigateToFinishedList = {},
                                    navigateToSearch = {},
                                    componentVisibleProvider = { TagHomeScaffoldComponentVisible() },
                                    gridState = rememberLazyGridState(),
                                    tagViewModel = viewModel,
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<TagDetailNavKey>(
                                metadata = ListDetailSceneStrategy.detailPane(sceneKey = TagHomeNavKey),
                            ) { key ->
                                Text(text = detailContent(id = key.id.toString()))
                            }
                        },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(OPENED_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(detailContent(opened = opened)).assertExists()

        return Environment(viewModel = viewModel, backStack = backStack, opened = opened)
    }

    private class Environment(
        val viewModel: TagHomeViewModel,
        val backStack: NavBackStack<ScreenNavKey>,
        val opened: Tag,
    )

    private companion object {
        const val OPENED_TITLE = "TagHomeListDetailSwipeTestOpened"
        const val OTHER_TITLE = "TagHomeListDetailSwipeTestOther"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun detailContent(opened: Tag): String = detailContent(id = opened.id.toString())

        fun detailContent(id: String): String = "TagDetail-$id"
    }
}
