package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-9441c865-0db9-40eb-901f-445b04bcad44 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        val navigation = setDDayHomeScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigation.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-d14ac637-3a8d-4a0f-be17-7cfaf6e01948 목록의 디데이를 선택하면 그 디데이의 DDayDetail 화면으로 한 번 이동한다`() {
        val ddayList = listOf(homeDDayOf(title = FIRST_TITLE), homeDDayOf(title = SECOND_TITLE))
        val navigation = setDDayHomeScreen(pagingData = homeDDayPagingDataOf(ddayList))

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(SECOND_TITLE)).performClick()
        composeRule.waitForIdle()

        navigation.detailIdList shouldBe listOf(ddayList.last().id)
    }

    @Test
    fun `TC-6fc7c64c-2593-4766-937e-3e099eefa320 디데이가 표시된 목록에서 추가를 선택하면 DDayAdd로 한 번 이동한다`() {
        assertAddNavigated(pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))))
    }

    @Test
    fun `TC-6fc7c64c-2593-4766-937e-3e099eefa320 빈 상태 안내가 표시된 목록에서 추가를 선택하면 DDayAdd로 한 번 이동한다`() {
        assertAddNavigated(pagingData = homeDDayPagingDataOf(emptyList()))
    }

    @Test
    fun `TC-6fc7c64c-2593-4766-937e-3e099eefa320 태그 필터를 적용해 둔 목록에서 추가를 선택하면 DDayAdd로 한 번 이동한다`() {
        val tagId = Arb.kotlinUuid().next()
        assertAddNavigated(
            pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
        )
    }

    @Test
    fun `TC-e09e831f-7933-4d3e-875e-28451999cba3 추가 단축키를 누르면 DDayAdd로 한 번 이동한다`() {
        val navigation = setDDayHomeScreen(pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))))

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        navigation.addCount shouldBe 1
    }

    @Test
    fun `TC-cf634e0f-ad76-4cfe-9089-1cbbc5af69d2 디데이가 표시된 목록을 당기면 서버 동기화를 한 번 요청한다`() {
        val syncViewModel = syncViewModel()
        setDDayHomeScreen(pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-cf634e0f-ad76-4cfe-9089-1cbbc5af69d2 태그 필터로 좁힌 목록을 당기면 서버 동기화를 한 번 요청한다`() {
        val tagId = Arb.kotlinUuid().next()
        val syncViewModel = syncViewModel()
        setDDayHomeScreen(
            pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
            syncViewModel = syncViewModel,
        )
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-569af3c7-cb86-4584-9c3a-f7bced46947d 아직 디데이가 없는 빈 상태에서도 추가, 필터 열기와 새로고침을 실행할 수 있다`() {
        assertEmptyActionsAvailable(filterUiState = DDayHomeScaffoldFilterUiState(), emptyTitle = DEFAULT_EMPTY_TITLE)
    }

    @Test
    fun `TC-569af3c7-cb86-4584-9c3a-f7bced46947d 조건에 맞는 디데이가 없는 빈 상태에서도 추가, 필터 열기와 새로고침을 실행할 수 있다`() {
        val tagId = Arb.kotlinUuid().next()
        assertEmptyActionsAvailable(
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
            emptyTitle = DEFAULT_FILTERED_EMPTY_TITLE,
        )
    }

    @Test
    fun `TC-f273c576-c4c4-4491-8d4a-309452679950 DDayHome 진입만으로는 서버 동기화를 요청하지 않는다`() {
        val syncViewModel = syncViewModel()
        setDDayHomeScreen(pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = FIRST_TITLE))), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertExists()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-3c1ca627-98c7-4412-b8f9-f4232bbab368 목록을 조회하지 못하면 오류 안내 없이 빈 상태로 표시한다`() {
        val pageDDayUseCase = mockk<PageDDayUseCase>()
        every { pageDDayUseCase() } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
        val getDDayFilterUseCase = mockk<GetDDayFilterUseCase>()
        every { getDDayFilterUseCase() } returns flowOf(Result.success(emptyList()))
        val getDDayFilterTagIdUseCase = mockk<GetDDayFilterTagIdUseCase>()
        every { getDDayFilterTagIdUseCase() } returns flowOf(Result.success(emptySet()))
        val viewModel =
            DDayHomeViewModel(
                getDDayFilterUseCase = getDDayFilterUseCase,
                getDDayFilterTagIdUseCase = getDDayFilterTagIdUseCase,
                pageDDayUseCase = pageDDayUseCase,
                deleteDDayUseCase = mockk(),
                restoreDDayUseCase = mockk(),
            )
        setDDayHomeScreenContent(ddayViewModel = viewModel, syncViewModel = syncViewModel(), navigation = Navigation())

        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertDoesNotExist()
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    private fun assertAddNavigated(
        pagingData: PagingData<DDay>,
        filterUiState: DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState(),
    ) {
        val navigation = setDDayHomeScreen(pagingData = pagingData, filterUiState = filterUiState)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigation.addCount shouldBe 1
    }

    private fun assertEmptyActionsAvailable(
        filterUiState: DDayHomeScaffoldFilterUiState,
        emptyTitle: String,
    ) {
        val syncUiStateFlow = MutableStateFlow(SyncRefreshUiState())
        val syncViewModel = mockk<SyncRefreshViewModel>()
        every { syncViewModel.uiState } returns syncUiStateFlow
        every { syncViewModel.refresh() } answers { syncUiStateFlow.value = SyncRefreshUiState(isRefreshing = true) }
        val navigation = setDDayHomeScreen(pagingData = homeDDayPagingDataOf(emptyList()), filterUiState = filterUiState, syncViewModel = syncViewModel)
        composeRule.onNodeWithText(emptyTitle).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        navigation.addCount shouldBe 1
        navigation.filterCount shouldBe 1
        verify(exactly = 1) { syncViewModel.refresh() }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(emptyTitle).assertExists()
    }

    private fun syncViewModel(): SyncRefreshViewModel {
        val viewModel = mockk<SyncRefreshViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { viewModel.refresh() }

        return viewModel
    }

    private fun setDDayHomeScreen(
        pagingData: PagingData<DDay> = homeDDayPagingDataOf(emptyList()),
        filterUiState: DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState(),
        syncViewModel: SyncRefreshViewModel = syncViewModel(),
    ): Navigation {
        val ddayViewModel = mockk<DDayHomeViewModel>()
        every { ddayViewModel.ddayPagingData } returns MutableStateFlow(pagingData)
        every { ddayViewModel.filterUiState } returns MutableStateFlow(filterUiState)
        every { ddayViewModel.effect } returns emptyFlow()
        val navigation = Navigation()

        setDDayHomeScreenContent(ddayViewModel = ddayViewModel, syncViewModel = syncViewModel, navigation = navigation)

        return navigation
    }

    private fun setDDayHomeScreenContent(
        ddayViewModel: DDayHomeViewModel,
        syncViewModel: SyncRefreshViewModel,
        navigation: Navigation,
    ) {
        composeRule.setContent {
            DiaryTheme {
                DDayHomeScreen(
                    navigateUp = { navigation.navigateUpCount += 1 },
                    navigateToAdd = { navigation.addCount += 1 },
                    navigateToDetail = navigation.detailIdList::add,
                    navigateToFilter = { navigation.filterCount += 1 },
                    componentVisibleProvider = { DDayHomeScaffoldComponentVisible() },
                    ddayViewModel = ddayViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private class Navigation {
        var navigateUpCount = 0
        var addCount = 0
        var filterCount = 0
        val detailIdList = mutableListOf<Uuid>()
    }

    private companion object {
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_ADD_DESCRIPTION = "Add D-Day"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter by tag"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_EMPTY_TITLE = "No D-Days yet"
        const val DEFAULT_FILTERED_EMPTY_TITLE = "No D-Days match the filter"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val FIRST_TITLE = "Anniversary"
        const val SECOND_TITLE = "Exam"
        const val TIMEOUT_MILLIS = 5_000L
    }
}
