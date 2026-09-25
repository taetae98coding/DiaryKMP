package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
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
class WebHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-001 조회한 웹 항목을 목록에 표시한다`() {
        val web = testWeb(title = WEB_TITLE, url = WEB_URL)

        setWebHomeScreen(webList = listOf(web))

        composeRule.onNodeWithText(WEB_TITLE).assertExists()
        composeRule.onNodeWithText(WEB_URL).assertExists()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-008 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0

        setWebHomeScreen(navigateUp = { navigateUpCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-005 웹 추가를 선택하면 웹 추가 화면으로 이동한다`() {
        var navigateToAddCount = 0

        setWebHomeScreen(navigateToAdd = { navigateToAddCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-016 검색을 선택하면 검색 화면으로 이동한다`() {
        var navigateToSearchCount = 0

        setWebHomeScreen(navigateToSearch = { navigateToSearchCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()

        navigateToSearchCount shouldBe 1
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-017 웹 항목을 선택하면 그 웹 항목의 상세 화면으로 이동한다`() {
        val web = testWeb(title = WEB_TITLE, url = WEB_URL)
        val navigatedIdList = mutableListOf<Uuid>()

        setWebHomeScreen(webList = listOf(web), navigateToDetail = navigatedIdList::add)
        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).onFirst().performClick()

        navigatedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-013 목록을 당기면 새로고침을 한 번 실행한다`() {
        val syncViewModel = syncViewModel()

        setWebHomeScreen(webList = listOf(testWeb(title = WEB_TITLE, url = WEB_URL)), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-015 동기화가 진행 중이면 진행 표시가 나타난다`() {
        setWebHomeScreen(syncViewModel = syncViewModel(uiState = WebHomeUiState(isRefreshing = true)))

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun setWebHomeScreen(
        webList: List<Web> = emptyList(),
        navigateUp: () -> Unit = {},
        navigateToSearch: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        componentVisible: WebHomeScaffoldComponentVisible = WebHomeScaffoldComponentVisible(),
        syncViewModel: WebHomeSyncViewModel = syncViewModel(),
    ) {
        val webViewModel = mockk<WebHomeViewModel>()
        every { webViewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { webViewModel.webPagingData } returns MutableStateFlow(webPagingDataOf(webList))
        every { webViewModel.effect } returns emptyFlow()

        composeRule.setContent {
            DiaryTheme {
                WebHomeScreen(
                    navigateUp = navigateUp,
                    navigateToSearch = navigateToSearch,
                    navigateToAdd = navigateToAdd,
                    navigateToDetail = navigateToDetail,
                    componentVisibleProvider = { componentVisible },
                    webViewModel = webViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
    }

    private companion object {
        private const val WEB_TITLE = "WebHomeScreenTitle"
        private const val WEB_URL = "https://screen.example.com"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add web"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"

        private fun syncViewModel(uiState: WebHomeUiState = WebHomeUiState()): WebHomeSyncViewModel {
            val viewModel = mockk<WebHomeSyncViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            justRun { viewModel.refresh() }

            return viewModel
        }
    }
}
