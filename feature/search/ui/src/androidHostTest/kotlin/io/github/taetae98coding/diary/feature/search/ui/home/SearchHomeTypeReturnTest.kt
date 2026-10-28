package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebViewModel
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

// 유형을 바꿨을 때 그 유형이 이미 현재 질의의 결과를 갖고 있는지는 실제 ViewModel과 화면이 함께 정하므로,
// 조회만 대역으로 두고 제품 ViewModel로 화면을 띄운다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeTypeReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        stopKoin()
    }

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-46c6c87f-703d-4483-870e-8ddecc6d1770 처음 보는 유형으로 바꾸면 이미 반영된 현재 질의의 결과를 본다`() {
        val memo = resultMemo()
        val tag = resultTag()
        setSearchHomeScreen(queryMemo = memo, queryTag = tag)
        inputQuery(QUERY)
        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()

        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        composeRule.onNodeWithText(tag.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-46c6c87f-703d-4483-870e-8ddecc6d1770 다른 질의의 결과를 보여 준 뒤 떠났던 유형으로 돌아오면 현재 질의의 결과를 본다`() {
        val memo = resultMemo()
        val otherMemo = resultMemo()
        setSearchHomeScreen(queryMemo = memo, otherQueryMemo = otherMemo)
        inputQuery(QUERY)
        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        inputQuery(OTHER_QUERY)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(OTHER_QUERY).assertExists()
        composeRule.onNodeWithText(otherMemo.detail.title).assertIsDisplayed()
        composeRule.onAllNodesWithText(memo.detail.title).fetchSemanticsNodes().isEmpty() shouldBe true
    }

    @Test
    fun `같은 질의의 결과를 보여 준 뒤 떠났던 유형은 다시 나타나면 그 결과를 그대로 보여 준다`() {
        val memo = resultMemo()
        setSearchHomeScreen(queryMemo = memo)
        inputQuery(QUERY)
        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
    }

    private fun inputQuery(query: String) {
        composeRule.onNode(hasSetTextAction()).performTextInput(query)
        // 입력을 멈춘 뒤의 반영까지 기다린다.
        composeRule.mainClock.advanceTimeBy(QUERY_IDLE_WAIT_MILLIS)
        ShadowLooper.idleMainLooper(QUERY_IDLE_WAIT_MILLIS, TimeUnit.MILLISECONDS)
        composeRule.waitForIdle()
    }

    private fun setSearchHomeScreen(
        queryMemo: Memo,
        otherQueryMemo: Memo = resultMemo(),
        queryTag: Tag = resultTag(),
    ) {
        val searchMemoUseCase =
            mockk<SearchMemoUseCase>().apply {
                every { this@apply(any()) } answers {
                    when (firstArg<SearchMemoUseCase.Parameter>().query) {
                        QUERY -> flowOf(Result.success(pagingDataOf(listOf(queryMemo))))
                        OTHER_QUERY -> flowOf(Result.success(pagingDataOf(listOf(otherQueryMemo))))
                        else -> flowOf(Result.success(PagingData.empty()))
                    }
                }
            }
        val searchTagUseCase =
            mockk<SearchTagUseCase>().apply {
                every { this@apply(any()) } answers {
                    if (firstArg<SearchTagUseCase.Parameter>().query == QUERY) {
                        flowOf(Result.success(pagingDataOf(listOf(queryTag))))
                    } else {
                        flowOf(Result.success(PagingData.empty()))
                    }
                }
            }
        val searchPlaceUseCase = mockk<SearchPlaceUseCase>().apply { every { this@apply(any()) } returns flowOf(Result.success(PagingData.empty())) }
        val searchWebUseCase = mockk<SearchWebUseCase>().apply { every { this@apply(any()) } returns flowOf(Result.success(PagingData.empty())) }
        val viewModelModule =
            module {
                factory { SearchHomeMemoViewModel(searchMemoUseCase, mockk(), mockk(), mockk(), mockk()) }
                factory { SearchHomeTagViewModel(searchTagUseCase, mockk(), mockk(), mockk(), mockk()) }
                factory { SearchHomePlaceViewModel(searchPlaceUseCase, mockk(), mockk()) }
                factory { SearchHomeWebViewModel(searchWebUseCase, mockk(), mockk()) }
            }

        composeRule.setContent {
            // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }

            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    DiaryTheme {
                        SearchHomeScreen(
                            navigateUp = {},
                            navigateToMemoDetail = {},
                            navigateToTagDetail = {},
                            navigateToPlaceDetail = {},
                            navigateToWebDetail = {},
                            initialType = SearchHomeType.MEMO,
                            memoViewModel = koinViewModel(),
                            tagViewModel = koinViewModel(),
                            placeViewModel = koinViewModel(),
                            webViewModel = koinViewModel(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val QUERY = "여행"
        const val OTHER_QUERY = "회의"
        const val QUERY_IDLE_WAIT_MILLIS = 1_000L
    }
}
