package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 필터 선택과 조회 결과는 저장소에서 오는 값이므로 그 흐름만 테스트가 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeFilterScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-993aaf1b-5782-4436-9759-22222931160f 선택되어 있지 않은 태그를 선택하면 목록을 처음부터 다시 본다`() {
        val tagId = Arb.kotlinUuid().next()
        val environment = setScrolledDDayHome()

        changeQuery(environment = environment, filterUiState = appliedFilter(setOf(tagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-993aaf1b-5782-4436-9759-22222931160f 선택되어 있는 태그의 선택을 해제하면 목록을 처음부터 다시 본다`() {
        val firstTagId = Arb.kotlinUuid().next()
        val secondTagId = Arb.kotlinUuid().next()
        val environment = setScrolledDDayHome(filterUiState = appliedFilter(setOf(firstTagId, secondTagId)))

        changeQuery(environment = environment, filterUiState = appliedFilter(setOf(firstTagId)))

        assertBackToTop(environment)
    }

    @Test
    fun `TC-993aaf1b-5782-4436-9759-22222931160f 선택을 전체 해제하면 목록을 처음부터 다시 본다`() {
        val tagId = Arb.kotlinUuid().next()
        val environment = setScrolledDDayHome(filterUiState = appliedFilter(setOf(tagId)))

        changeQuery(environment = environment, filterUiState = DDayHomeScaffoldFilterUiState())

        assertBackToTop(environment)
    }

    @Test
    fun `TC-850622fc-e9ef-4185-b0bb-69e1d5dd170f 좁힌 결과가 놓이기 전에는 목록 위치를 유지한다`() {
        val environment = setScrolledDDayHome()

        composeRule.runOnIdle { environment.filterUiStateFlow.value = appliedFilter(setOf(Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(environment.ddayList, isRefreshing = true) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-ef19a8ea-acfb-47b4-9767-8b86d3c2d103 노출 대상 디데이가 추가되어도 목록 위치를 유지한다`() {
        val environment = setScrolledDDayHome()

        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(listOf(addedDDay()) + environment.ddayList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-ef19a8ea-acfb-47b4-9767-8b86d3c2d103 보고 있던 자리 밖의 디데이가 삭제되어도 목록 위치를 유지한다`() {
        val environment = setScrolledDDayHome()

        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(environment.ddayList.drop(1)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(environment.ddayList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(environment.ddayList[1].detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-ef19a8ea-acfb-47b4-9767-8b86d3c2d103 서버에서 내려받은 디데이가 반영되어도 목록 위치를 유지한다`() {
        val environment = setScrolledDDayHome()
        val syncedList =
            environment.ddayList.mapIndexed { index, dday ->
                if (index == 1) dday.copy(detail = dday.detail.copy(color = dday.detail.color.inv())) else dday
            } + addedDDay()

        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(syncedList) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    @Test
    fun `TC-956a1af5-db3b-4c9e-a381-57acdc695fd5 저장된 필터를 처음 불러와 반영해도 목록 위치를 유지한다`() {
        val environment = setScrolledDDayHome(filterUiState = DDayHomeScaffoldFilterUiState(isLoaded = false))

        composeRule.runOnIdle { environment.filterUiStateFlow.value = appliedFilter(setOf(Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(environment.ddayList + addedDDay()) }
        composeRule.waitForIdle()

        assertPositionKept(environment)
    }

    private fun appliedFilter(tagIdSet: Set<Uuid>): DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = tagIdSet, storedTagIdSet = tagIdSet)

    private fun addedDDay(): DDay = homeDDayOf(title = "${TITLE_PREFIX}Added")

    private fun assertPositionKept(environment: Environment) {
        composeRule.onNodeWithText(environment.ddayList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                environment.ddayList
                    .first()
                    .detail.title,
            ).assertDoesNotExist()
    }

    private fun setScrolledDDayHome(filterUiState: DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState()): Environment {
        val ddayList = List(DDAY_COUNT) { index -> homeDDayOf(title = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        val filterUiStateFlow = MutableStateFlow(filterUiState)
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(ddayList))
        val viewModel = mockk<DDayHomeViewModel>()
        every { viewModel.ddayPagingData } returns pagingDataFlow
        every { viewModel.filterUiState } returns filterUiStateFlow
        every { viewModel.effect } returns emptyFlow()
        val syncViewModel = mockk<SyncRefreshViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                DDayHomeScreen(
                    navigateUp = {},
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    componentVisibleProvider = { DDayHomeScaffoldComponentVisible() },
                    ddayViewModel = viewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ddayList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.homeRefreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ddayList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(ddayList.first().detail.title).assertDoesNotExist()

        return Environment(ddayList = ddayList, filterUiStateFlow = filterUiStateFlow, pagingDataFlow = pagingDataFlow)
    }

    // 필터를 바꾼 뒤 좁힌 결과가 이어서 도착한다. 좁힌 결과에도 보던 디데이가 남아 있어 그 자리를 따라가지 않는지 확인할 수 있다.
    private fun changeQuery(
        environment: Environment,
        filterUiState: DDayHomeScaffoldFilterUiState,
    ) {
        composeRule.runOnIdle { environment.filterUiStateFlow.value = filterUiState }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.pagingDataFlow.value = homeDDayPagingDataOf(environment.narrowedList) }
        composeRule.waitForIdle()
    }

    private fun assertBackToTop(environment: Environment) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(
                    environment.narrowedList
                        .first()
                        .detail.title,
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithText(
                environment.narrowedList
                    .first()
                    .detail.title,
            ).assertIsDisplayed()
        composeRule.onNodeWithText(environment.ddayList[SCROLLED_INDEX].detail.title).assertDoesNotExist()
    }

    private class Environment(
        val ddayList: List<DDay>,
        val filterUiStateFlow: MutableStateFlow<DDayHomeScaffoldFilterUiState>,
        val pagingDataFlow: MutableStateFlow<PagingData<DDay>>,
    ) {
        // 보던 디데이가 들어 있는 좁힌 결과다.
        val narrowedList: List<DDay> = ddayList.filterIndexed { index, _ -> index % 2 == 0 }
    }

    private companion object {
        const val DDAY_COUNT = 60
        const val SCROLLED_INDEX = 50
        const val TIMEOUT_MILLIS = 5_000L
        const val TITLE_PREFIX = "FilterScrollDDay"
    }
}
