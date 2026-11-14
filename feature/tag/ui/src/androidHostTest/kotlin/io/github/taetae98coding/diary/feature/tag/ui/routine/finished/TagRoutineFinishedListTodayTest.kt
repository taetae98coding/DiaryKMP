package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.UpdateRoutineListTodayEffect
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

// TagRoutineFinishedList는 오늘을 화면이 시작될 때마다 다시 정하는 Effect, 그 오늘을 목록 조회에 넘기는 Effect와 그 오늘로 카드를 그리는 화면으로 구성한다.
// 기기 날짜를 바꿀 수 없으므로 같은 구성을 날짜를 정할 수 있는 시계로 만들어 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class TagRoutineFinishedListTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val clock = mockk<Clock>()
    private val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
    private var isFinishedListShown by mutableStateOf(true)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-97a3a421-ee15-4c00-8b37-8970debded9d 다른 화면에서 돌아오면 바뀐 오늘로 순서를 다시 정하고 카드 문구는 완료 그대로다`() {
        setTodayScreen()
        assertBeforeReturn()

        composeRule.runOnIdle { isFinishedListShown = false }
        composeRule.waitForIdle()
        setToday(MARCH_20)
        composeRule.runOnIdle { isFinishedListShown = true }

        assertAfterReturn()
    }

    @Test
    fun `TC-97a3a421-ee15-4c00-8b37-8970debded9d 다른 앱에서 돌아오면 바뀐 오늘로 순서를 다시 정하고 카드 문구는 완료 그대로다`() {
        setTodayScreen()
        assertBeforeReturn()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        setToday(MARCH_20)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        assertAfterReturn()
    }

    @Test
    fun `TC-3286f51d-5711-4e45-b0a5-e9cb7e44a4e1 화면을 보는 동안 자정이 지나도 순서를 다시 정하지 않는다`() {
        setTodayScreen()
        assertBeforeReturn()

        setToday(MARCH_20)
        composeRule.mainClock.advanceTimeBy(AFTER_MIDNIGHT_MILLIS)
        composeRule.waitForIdle()

        assertBeforeReturn()
    }

    private fun setTodayScreen() {
        setToday(MARCH_19)
        val tagId = Arb.kotlinUuid().next()
        val started = tagRoutine(title = STARTED_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 1), isFinished = true)
        val starting = tagRoutine(title = STARTING_TITLE, startDate = MARCH_20, isFinished = true)
        val page = mockk<PageFinishedTagRoutineUseCase>()
        every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = MARCH_19)) } returns
            flowOf(Result.success(tagEntityPagingData(itemList = listOf(started, starting))))
        every { page(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tagId, today = MARCH_20)) } returns
            flowOf(Result.success(tagEntityPagingData(itemList = listOf(starting, started))))
        val findTagUseCase = mockk<FindTagUseCase>()
        every { findTagUseCase(parameter = tagId) } returns flowOf(Result.success(null))
        val viewModel =
            TagRoutineFinishedListViewModel(
                tagId = tagId,
                pageFinishedTagRoutineUseCase = page,
                findTagUseCase = findTagUseCase,
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = mockk(),
                restoreRoutineUseCase = mockk(),
            )
        val state = RoutineListState(initialToday = MARCH_19, clock = clock)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    if (isFinishedListShown) {
                        UpdateRoutineListTodayEffect(state = state)
                        UpdateTagRoutineFinishedListTodayEffect(routineViewModel = viewModel, listState = state)
                        TagRoutineFinishedListScaffold(
                            onEvent = {},
                            onRoutineListEvent = {},
                            listState = state,
                            routinePagingItems = viewModel.routinePagingData.collectAsLazyPagingItems(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun setToday(date: LocalDate) {
        every { clock.now() } returns LocalDateTime(date = date, time = NOON).toInstant(TimeZone.currentSystemDefault())
    }

    private fun assertBeforeReturn() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(STARTING_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        assertFinishedCards()
        (top(STARTED_TITLE) < top(STARTING_TITLE)) shouldBe true
    }

    private fun assertAfterReturn() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(STARTING_TITLE)).fetchSemanticsNodes().isNotEmpty() &&
                top(STARTING_TITLE) < top(STARTED_TITLE)
        }
        assertFinishedCards()
    }

    private fun assertFinishedCards() {
        listOf(STARTED_TITLE, STARTING_TITLE).forEach { title ->
            composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title) and hasText(FINISHED_TEXT)).assertIsDisplayed()
        }
    }

    private fun top(title: String): Float =
        composeRule
            .onNodeWithText(title)
            .fetchSemanticsNode()
            .boundsInRoot
            .top

    private companion object {
        const val STARTED_TITLE = "운동 나"
        const val STARTING_TITLE = "운동 가"
        const val FINISHED_TEXT = "완료"
        const val AFTER_MIDNIGHT_MILLIS = 60_000L
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val MARCH_19 = LocalDate(year = 2026, month = 3, day = 19)
        val MARCH_20 = LocalDate(year = 2026, month = 3, day = 20)
        val NOON = LocalTime(hour = 12, minute = 0)
    }
}
