package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeScaffoldRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-a0d2d609-161f-429e-9ca9-5316a5689869 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(
            webList = listOf(testWeb(title = WEB_TITLE)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(WebHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-e27e3473-1d9f-486a-9951-efd7b309ae1f 진행 표시 상태이면 진행 표시가 나타난다`() {
        setWebHomeScaffold(isRefreshingProvider = { true })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-e27e3473-1d9f-486a-9951-efd7b309ae1f 동기화가 끝나면 진행 표시가 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setWebHomeScaffold(isRefreshingProvider = { isRefreshing.value })
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setWebHomeScaffold(
        webList: List<Web> = emptyList(),
        isRefreshingProvider: () -> Boolean = { false },
        onEvent: (WebHomeScaffoldEvent) -> Unit = {},
    ) {
        val webPagingDataFlow = MutableStateFlow(webPagingDataOf(webList))

        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = onEvent,
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { SyncRefreshUiState(isRefreshing = isRefreshingProvider()) },
                )
            }
        }
    }

    private companion object {
        private const val WEB_TITLE = "RefreshWebTitle"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
