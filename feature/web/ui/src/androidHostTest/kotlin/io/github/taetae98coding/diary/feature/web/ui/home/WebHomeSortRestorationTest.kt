package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeSortRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-WEB-HOME-DOMAIN-013 화면이 재생성되어도 고른 정렬을 유지한다`() {
        val first = testWeb(title = "A-${fixtureMonkey.giveMeOne<String>()}")
        val second = testWeb(title = "B-${fixtureMonkey.giveMeOne<String>()}")
        val pageWebUseCase = mockk<PageWebUseCase>()
        every { pageWebUseCase(parameter = ListSort.TITLE) } returns flowOf(Result.success(webPagingDataOf(listOf(first, second))))
        every { pageWebUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns
            flowOf(Result.success(webPagingDataOf(listOf(second, first))))
        val viewModel =
            WebHomeViewModel(
                pageWebUseCase = pageWebUseCase,
                deleteWebUseCase = mockk<DeleteWebUseCase>(),
                restoreWebUseCase = mockk<RestoreWebUseCase>(),
            )
        val syncViewModel = mockk<WebHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(WebHomeUiState())
        justRun { syncViewModel.refresh() }
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
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
        waitUntilTitleExists(first.detail.title)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        waitUntilTitleExists(first.detail.title)

        viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        cardTitleList() shouldBe listOf(second.detail.title, first.detail.title)
    }

    @Test
    fun `TC-WEB-HOME-DOMAIN-014 시스템이 앱을 정리했다가 다시 만들면 정렬은 제목순으로 돌아가고 보던 목록 위치는 복원한다`() {
        val titleOrderList = List(RESTORATION_WEB_COUNT) { index -> testWeb(title = "웹-${index.toString().padStart(length = 2, padChar = '0')}") }
        val recentOrderList = titleOrderList.reversed()
        val pageWebUseCase = mockk<PageWebUseCase>()
        every { pageWebUseCase(parameter = ListSort.TITLE) } returns flowOf(Result.success(webPagingDataOf(titleOrderList)))
        every { pageWebUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns flowOf(Result.success(webPagingDataOf(recentOrderList)))
        val syncViewModel = mockk<WebHomeSyncViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(WebHomeUiState())
        justRun { syncViewModel.refresh() }
        var viewModel = webHomeViewModel(pageWebUseCase = pageWebUseCase)
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
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
        waitUntilTitleExists(titleOrderList.first().detail.title)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        waitUntilTitleExists(recentOrderList.first().detail.title)
        composeRule.onNodeWithTag(WEB_HOME_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(recentOrderList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()

        // 시스템이 앱을 정리하면 화면 상태를 보관하던 객체도 사라지므로, 되살린 화면에는 새로 만든 객체를 준다.
        viewModel = webHomeViewModel(pageWebUseCase = pageWebUseCase)
        restorationTester.emulateSavedInstanceStateRestore()
        waitUntilTitleExists(titleOrderList[RESTORATION_SCROLL_INDEX].detail.title)

        viewModel.sort.value shouldBe ListSort.TITLE
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(titleOrderList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(titleOrderList.first().detail.title).assertDoesNotExist()
    }

    private fun webHomeViewModel(pageWebUseCase: PageWebUseCase): WebHomeViewModel =
        WebHomeViewModel(
            pageWebUseCase = pageWebUseCase,
            deleteWebUseCase = mockk<DeleteWebUseCase>(),
            restoreWebUseCase = mockk<RestoreWebUseCase>(),
        )

    private fun waitUntilTitleExists(title: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun cardTitleList(): List<String> =
        composeRule
            .onAllNodes(hasTestTag(WEB_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .mapNotNull { node ->
                node.config
                    .getOrElse(SemanticsProperties.Text) { emptyList() }
                    .firstOrNull()
                    ?.text
            }

    private companion object {
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val RESTORATION_WEB_COUNT = 60
        const val RESTORATION_SCROLL_INDEX = 50
    }
}
