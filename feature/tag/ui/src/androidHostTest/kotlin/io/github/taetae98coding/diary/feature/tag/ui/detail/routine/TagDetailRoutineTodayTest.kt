package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.routine.usecase.PageTagRoutineUseCase
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

// TagDetail은 루틴 탭의 오늘을 화면이 시작될 때마다 다시 정하는 Effect, 그 오늘을 목록 조회에 넘기는 Effect와 그 오늘로 카드 문구를 그리는 탭으로 구성한다.
// 기기 날짜를 바꿀 수 없으므로 같은 구성을 날짜를 정할 수 있는 시계로 만들어 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class TagDetailRoutineTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val clock = mockk<Clock>()
    private val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
    private var isTagDetailShown by mutableStateOf(true)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-91435776-71d2-476b-b211-619f1bf8faf0 다른 화면에서 돌아오면 바뀐 오늘로 카드 문구와 순서를 다시 정한다`() {
        setTodayTab()
        assertBeforeReturn()

        composeRule.runOnIdle { isTagDetailShown = false }
        composeRule.waitForIdle()
        setToday(MARCH_20)
        composeRule.runOnIdle { isTagDetailShown = true }

        assertAfterReturn()
    }

    @Test
    fun `TC-91435776-71d2-476b-b211-619f1bf8faf0 다른 앱에서 돌아오면 바뀐 오늘로 카드 문구와 순서를 다시 정한다`() {
        setTodayTab()
        assertBeforeReturn()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        setToday(MARCH_20)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        assertAfterReturn()
    }

    @Test
    fun `TC-bdfcd20b-ecb1-4559-a9f7-13d0ae2fe74d 화면을 보는 동안 자정이 지나도 카드 문구와 순서를 다시 정하지 않는다`() {
        setTodayTab()
        assertBeforeReturn()

        setToday(MARCH_20)
        composeRule.mainClock.advanceTimeBy(AFTER_MIDNIGHT_MILLIS)
        composeRule.waitForIdle()

        assertBeforeReturn()
    }

    private fun setTodayTab() {
        setToday(MARCH_19)
        val tagId = Arb.kotlinUuid().next()
        val started = tagRoutine(title = STARTED_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 1))
        val starting = tagRoutine(title = STARTING_TITLE, startDate = MARCH_20)
        val page = mockk<PageTagRoutineUseCase>()
        every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = MARCH_19)) } returns
            flowOf(Result.success(tagEntityPagingData(itemList = listOf(started, starting))))
        every { page(parameter = PageTagRoutineUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, today = MARCH_20)) } returns
            flowOf(Result.success(tagEntityPagingData(itemList = listOf(starting, started))))
        val viewModel = TagDetailRoutineViewModel(tagId = tagId, pageTagRoutineUseCase = page, finishRoutineUseCase = mockk(), restartRoutineUseCase = mockk(), deleteRoutineUseCase = mockk(), restoreRoutineUseCase = mockk())
        val state = RoutineListState(initialToday = MARCH_19, clock = clock)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    if (isTagDetailShown) {
                        UpdateRoutineListTodayEffect(state = state)
                        UpdateTagDetailRoutineTodayEffect(routineViewModel = viewModel, listState = state)
                        TagDetailRoutineTab(
                            onEvent = {},
                            onRoutineListEvent = {},
                            modifier = Modifier.fillMaxSize(),
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
        waitForCard(title = STARTING_TITLE, text = NOT_STARTED_TEXT)
        assertCard(title = STARTED_TITLE, text = IN_PROGRESS_TEXT)
        (top(STARTED_TITLE) < top(STARTING_TITLE)) shouldBe true
    }

    private fun assertAfterReturn() {
        waitForCard(title = STARTING_TITLE, text = IN_PROGRESS_TEXT)
        assertCard(title = STARTED_TITLE, text = IN_PROGRESS_TEXT)
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) { top(STARTING_TITLE) < top(STARTED_TITLE) }
    }

    private fun waitForCard(
        title: String,
        text: String,
    ) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title) and hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
        assertCard(title = title, text = text)
    }

    private fun assertCard(
        title: String,
        text: String,
    ) {
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title) and hasText(text)).assertIsDisplayed()
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
        const val IN_PROGRESS_TEXT = "오늘 0회"
        const val NOT_STARTED_TEXT = "시작 전"
        const val AFTER_MIDNIGHT_MILLIS = 60_000L
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val MARCH_19 = LocalDate(year = 2026, month = 3, day = 19)
        val MARCH_20 = LocalDate(year = 2026, month = 3, day = 20)
        val NOON = LocalTime(hour = 12, minute = 0)
    }
}
