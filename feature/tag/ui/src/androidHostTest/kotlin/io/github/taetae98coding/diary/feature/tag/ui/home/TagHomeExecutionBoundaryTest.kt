package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomeExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-036 태그를 선택하면 그 태그의 TagDetail 화면으로 이동한다`() {
        val tagList = tagList()
        val navigatedIdList = mutableListOf<Uuid>()
        val viewModel = screenTestViewModel(tagList = tagList)
        composeRule.setContent { Home(viewModel = viewModel, navigateToDetail = navigatedIdList::add) }

        composeRule.onNodeWithText(tagList.first().detail.title).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(tagList.first().id)
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-013 화면이 회전하거나 재생성되어도 필터 선택, 정렬 선택과 보던 자리가 그대로다`() {
        val tagList = tagList()
        val viewModel =
            screenTestViewModel(
                tagList = tagList,
                sort = ListSort.RECENTLY_UPDATED,
                filterUiState = TagHomeScaffoldFilterUiState(isApplied = true),
            )
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { Home(viewModel = viewModel) }
        scrollList()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        assertScrolledPosition(tagList)
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-020 시스템이 앱을 정리한 뒤 다시 만들면 필터 선택은 이어지고 정렬은 처음으로 돌아가며 보던 위치는 다시 보인다`() {
        val tagList = tagList()
        val filterUiState = TagHomeScaffoldFilterUiState(isApplied = true)
        var nextViewModel = screenTestViewModel(tagList = tagList, sort = ListSort.RECENTLY_UPDATED, filterUiState = filterUiState)
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 정렬을 들고 있던 ViewModel도 사라지므로, 복원으로 컴포지션을 다시 만들 때만 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModel = remember { nextViewModel }
            Home(viewModel = viewModel)
        }
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        scrollList()
        // 필터 선택은 기기에 저장된 값으로 다시 조회되므로 새 ViewModel도 켜진 선택을 받는다.
        nextViewModel = screenTestViewModel(tagList = tagList, filterUiState = filterUiState)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertDoesNotExist()
        assertScrolledPosition(tagList)
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-014 앱이 백그라운드에 다녀와도 보던 자리가 그대로다`() {
        val tagList = tagList()
        val viewModel = screenTestViewModel(tagList = tagList)
        composeRule.setContent { Home(viewModel = viewModel) }
        scrollList()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledPosition(tagList)
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-015 태그 상세, 검색이나 완료된 태그 확인에 다녀와도 보던 자리가 그대로다`() {
        val tagList = tagList()
        val viewModel = screenTestViewModel(tagList = tagList)
        var isTagHomeOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isTagHomeOnTop) {
                saveableStateHolder.SaveableStateProvider(key = TAG_HOME_ENTRY_KEY) {
                    Home(viewModel = viewModel)
                }
            }
        }
        scrollList()

        composeRule.runOnIdle { isTagHomeOnTop = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isTagHomeOnTop = true }
        composeRule.waitForIdle()

        assertScrolledPosition(tagList)
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-049 화면에 들어오거나 다른 화면에서 돌아오는 것만으로는 새로고침하지 않는다`() {
        val viewModel = screenTestViewModel(tagList = tagList())
        val syncViewModel = screenTestSyncViewModel()
        var isTagHomeOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isTagHomeOnTop) {
                saveableStateHolder.SaveableStateProvider(key = TAG_HOME_ENTRY_KEY) {
                    Home(viewModel = viewModel, syncViewModel = syncViewModel)
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { isTagHomeOnTop = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isTagHomeOnTop = true }
        composeRule.waitForIdle()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Composable
    private fun Home(
        viewModel: TagHomeViewModel,
        navigateToDetail: (Uuid) -> Unit = {},
        syncViewModel: TagHomeSyncViewModel = screenTestSyncViewModel(),
    ) {
        DiaryTheme {
            TagHomeScreen(
                navigateToAdd = {},
                navigateToDetail = navigateToDetail,
                navigateToFilter = {},
                navigateToFinishedList = {},
                navigateToSearch = {},
                gridState = rememberLazyGridState(),
                tagViewModel = viewModel,
                syncViewModel = syncViewModel,
                componentVisibleProvider = { TagHomeScaffoldComponentVisible() },
            )
        }
    }

    private fun scrollList() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_HOME_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
    }

    private fun assertScrolledPosition(tagList: List<Tag>) {
        composeRule.onNodeWithText(tagList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList.first().detail.title).assertDoesNotExist()
    }

    private fun screenTestViewModel(
        tagList: List<Tag>,
        sort: ListSort = ListSort.TITLE,
        filterUiState: TagHomeScaffoldFilterUiState = TagHomeScaffoldFilterUiState(),
    ): TagHomeViewModel {
        val viewModel = mockk<TagHomeViewModel>(relaxed = true)
        every { viewModel.sort } returns MutableStateFlow(sort)
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(tagList))
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.filterUiState } returns MutableStateFlow(filterUiState)
        return viewModel
    }

    private fun tagList(): List<Tag> {
        val titlePrefix = fixtureText(prefix = "TagHomeTag")

        return List(TAG_COUNT) { index -> tag(title = "${titlePrefix}Index$index") }
    }

    private fun tag(title: String): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
            .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
            .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
            .sample()

    private companion object {
        private const val TAG_HOME_ENTRY_KEY = "TagHome"
        private const val TAG_COUNT = 60
        private const val SCROLLED_INDEX = 50
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_TITLE_SORT = "Title"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
