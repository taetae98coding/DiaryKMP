package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-43f05702-0b18-4db1-8c28-0633d0bbea97 디데이 카드를 삭제 방향으로 밀면 삭제되고 안내와 실행 취소가 표시된다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE), homeDDayOf(title = SECOND_TITLE)))
        val target = environment.ddayList.first()

        swipeCard(title = target.detail.title)

        verify(exactly = 1) { environment.viewModel.delete(id = target.id) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(FIRST_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-43f05702-0b18-4db1-8c28-0633d0bbea97 기본 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)

        verify(exactly = 1) { environment.viewModel.delete(id = environment.ddayList.first().id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-e9494e1b-7937-4570-9859-73160a611797 디데이 카드를 반대 방향으로 밀면 아무 동작도 실행하지 않는다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE)))

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(FIRST_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(FIRST_TITLE)).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-64fdb2b6-d512-4ef3-b256-2d25899ab739 삭제를 실행 취소하면 그 디데이가 제목 순서에 맞는 자리에 다시 나타난다`() {
        val ddayList = listOf(homeDDayOf(title = FIRST_TITLE), homeDDayOf(title = SECOND_TITLE), homeDDayOf(title = THIRD_TITLE))
        val environment = setDDayHomeScreen(ddayList)
        val target = ddayList[1]

        swipeCard(title = target.detail.title)
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(SECOND_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(SECOND_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        verify(exactly = 1) { environment.viewModel.restore(id = target.id) }
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(SECOND_TITLE) and hasText(TODAY_CARD_TEXT)).assertExists()
        composeRule.homeCardTitleList(ddayList.map { dday -> dday.detail.title }) shouldBe listOf(FIRST_TITLE, SECOND_TITLE, THIRD_TITLE)
    }

    @Test
    fun `TC-41d2cac8-ac1d-41d6-ac79-a766125b0d64 안내가 보이는 동안 다른 디데이를 삭제하면 마지막 삭제의 안내만 남는다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE), homeDDayOf(title = SECOND_TITLE)))
        val (first, second) = environment.ddayList

        swipeCard(title = first.detail.title)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        swipeCard(title = second.detail.title)

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = second.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = first.id) }
    }

    @Test
    fun `TC-89ecf60a-56a4-4681-806b-7efde2cf0d29 삭제가 저장되지 못하면 안내를 표시하지 않는다`() {
        val dday = homeDDayOf(title = FIRST_TITLE)
        val pageDDayUseCase = mockk<PageDDayUseCase>()
        every { pageDDayUseCase(parameter = Unit) } returns flowOf(Result.success(homeDDayPagingDataOf(listOf(dday))))
        val getDDayFilterUseCase = mockk<GetDDayFilterUseCase>()
        every { getDDayFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val getDDayFilterTagIdUseCase = mockk<GetDDayFilterTagIdUseCase>()
        every { getDDayFilterTagIdUseCase(parameter = Unit) } returns flowOf(Result.success(emptySet()))
        val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
        coEvery { deleteDDayUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
        val viewModel =
            DDayHomeViewModel(
                getDDayFilterUseCase = getDDayFilterUseCase,
                getDDayFilterTagIdUseCase = getDDayFilterTagIdUseCase,
                pageDDayUseCase = pageDDayUseCase,
                deleteDDayUseCase = deleteDDayUseCase,
                restoreDDayUseCase = mockk(),
            )
        setDDayHomeScreenContent(viewModel = viewModel, firstTitle = FIRST_TITLE)

        swipeCard(title = FIRST_TITLE)

        coVerify(exactly = 1) { deleteDDayUseCase(parameter = dday.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0d65afad-04e1-4f4b-bb3c-3aa425596e3a 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-ed2db912-5bc8-4d32-b213-2c472895cfca 마지막 디데이가 사라지면 빈 상태가 되고 실행 취소로 돌아오면 빈 상태가 사라진다`() {
        val environment = setDDayHomeScreen(listOf(homeDDayOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        verify(exactly = 1) { environment.viewModel.restore(id = environment.ddayList.first().id) }
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(FIRST_TITLE)).assertIsDisplayed()
    }

    private fun swipeCard(title: String) {
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    // 삭제와 실행 취소가 저장되면 저장소의 목록이 바뀌어 조회 결과가 다시 전달되는 것을 테스트가 대신한다.
    private fun setDDayHomeScreen(ddayList: List<DDay>): Environment {
        val todayDDayList = ddayList.map { dday -> dday.copy(detail = dday.detail.copy(baseDate = Clock.System.todayIn(TimeZone.currentSystemDefault()))) }
        val visibleIdSet = MutableStateFlow(todayDDayList.map { dday -> dday.id }.toSet())
        val pagingDataFlow = MutableStateFlow<PagingData<DDay>>(homeDDayPagingDataOf(todayDDayList))
        val effectChannel = Channel<DDayListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<DDayHomeViewModel>()
        every { viewModel.ddayPagingData } returns pagingDataFlow
        every { viewModel.filterUiState } returns MutableStateFlow(DDayHomeScaffoldFilterUiState())
        every { viewModel.effect } returns effectChannel.receiveAsFlow()

        fun publish(idSet: Set<Uuid>) {
            visibleIdSet.value = idSet
            pagingDataFlow.value = homeDDayPagingDataOf(todayDDayList.filter { dday -> dday.id in idSet })
        }
        every { viewModel.delete(id = any()) } answers {
            publish(visibleIdSet.value - firstArg<Uuid>())
            effectChannel.trySend(DDayListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        every { viewModel.restore(id = any()) } answers { publish(visibleIdSet.value + firstArg<Uuid>()) }

        setDDayHomeScreenContent(viewModel = viewModel, firstTitle = todayDDayList.first().detail.title)

        return Environment(viewModel = viewModel, ddayList = todayDDayList)
    }

    private fun setDDayHomeScreenContent(
        viewModel: DDayHomeViewModel,
        firstTitle: String,
    ) {
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
            composeRule.onAllNodesWithText(firstTitle).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private class Environment(
        val viewModel: DDayHomeViewModel,
        val ddayList: List<DDay>,
    )

    private companion object {
        const val DEFAULT_DELETED_MESSAGE = "D-Day deleted."
        const val KOREAN_DELETED_MESSAGE = "디데이가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val DEFAULT_EMPTY_TITLE = "No D-Days yet"
        const val TODAY_CARD_TEXT = "D-Day"
        const val FIRST_TITLE = "Alpha"
        const val SECOND_TITLE = "Bravo"
        const val THIRD_TITLE = "Charlie"
        const val TIMEOUT_MILLIS = 5_000L
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
    }
}
