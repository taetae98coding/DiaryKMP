package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

// 화면을 떠났다 돌아올 때의 상태는 전환 이력의 항목마다 보관되므로, 앱과 같은 방식으로 항목을 그리는 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagFinishedListExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey)
    private val syncViewModel = screenTestSyncViewModel()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-030 화면에 들어오거나 다른 화면에서 돌아오는 것만으로는 새로고침하지 않는다`() {
        setFinishedListNavDisplay(tagList = tagList())

        openFinishedList()
        composeRule.runOnIdle { backStack.add(TagDetailNavKey(id = fixtureMonkey.giveMeOne())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-031 태그 상세에서 돌아오면 목록에서 보던 자리가 그대로다`() {
        val tagList = tagList()
        setFinishedListNavDisplay(tagList = tagList)
        openFinishedList()
        scrollList(tagList = tagList)

        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        assertScrolledPosition(tagList = tagList)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-DOMAIN-011 TagHome 목록으로 돌아갔다가 다시 진입하면 처음 정렬로 맨 위부터 보인다`() {
        val tagList = tagList()
        setFinishedListNavDisplay(tagList = tagList)
        openFinishedList()
        selectRecentlyUpdatedSort()
        scrollList(tagList = tagList)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertIsDisplayed()
        openFinishedList()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(tagList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-DOMAIN-012 앱이 백그라운드에 다녀와도 정렬 선택과 보던 자리가 그대로다`() {
        val tagList = tagList()
        setFinishedListNavDisplay(tagList = tagList)
        openFinishedList()
        selectRecentlyUpdatedSort()
        scrollList(tagList = tagList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        assertScrolledPosition(tagList = tagList)
    }

    private fun openFinishedList() {
        composeRule.runOnIdle { backStack.add(TagFinishedListNavKey) }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_TITLE_SORT).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectRecentlyUpdatedSort() {
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun scrollList(tagList: List<Tag>) {
        composeRule.onNodeWithTag(TAG_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLL_INDEX)
        composeRule.waitForIdle()
        assertScrolledPosition(tagList = tagList)
    }

    private fun assertScrolledPosition(tagList: List<Tag>) {
        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList.first().detail.title).assertDoesNotExist()
    }

    private fun setFinishedListNavDisplay(tagList: List<Tag>) {
        val pageFinishedTagUseCase = mockk<PageFinishedTagUseCase>()
        every { pageFinishedTagUseCase(parameter = any()) } answers { flowOf(Result.success(tagPagingDataOf(tagList))) }

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<TagHomeNavKey> { Text(text = TAG_HOME_CONTENT) }
                            entry<TagFinishedListNavKey> {
                                TagFinishedListScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
                                    tagViewModel =
                                        viewModel {
                                            TagFinishedListViewModel(
                                                pageFinishedTagUseCase = pageFinishedTagUseCase,
                                                finishTagUseCase = mockk(),
                                                restartTagUseCase = mockk(),
                                                deleteTagUseCase = mockk(),
                                                restoreTagUseCase = mockk(),
                                            )
                                        },
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<TagDetailNavKey> { Text(text = DETAIL_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun tagList(): List<Tag> {
        val titlePrefix = fixtureText(prefix = "FinishedTag")

        return List(TAG_COUNT) { index -> finishedTag(title = "${titlePrefix}Index$index") }
    }

    private fun finishedTag(title: String): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
            .setExp(Tag::isFinished, true)
            .setExp(Tag::isDeleted, false)
            .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
            .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
            .sample()

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val TAG_COUNT = 60
        private const val SCROLL_INDEX = 50
        private const val TAG_HOME_CONTENT = "TagHomeContent"
        private const val DETAIL_CONTENT = "TagDetailContent"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
