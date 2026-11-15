package io.github.taetae98coding.diary.compose.routine

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SwipeRoutineCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-b2aa114d-4bae-48c8-96e0-c52dca307491 루틴 카드를 우에서 좌로 밀면 그 루틴의 삭제를 한 번 전달한다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        val summary = summary()
        setCard(summary = summary, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.SwipeDelete(id = summary.routine.id))
    }

    @Test
    fun `완료되지 않은 루틴 카드를 좌에서 우로 밀면 그 루틴의 완료를 한 번 전달한다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        val summary = summary(isFinished = false)
        setCard(summary = summary, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.SwipeFinish(id = summary.routine.id))
    }

    @Test
    fun `완료된 루틴 카드를 좌에서 우로 밀면 그 루틴의 다시 시작을 한 번 전달한다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        val summary = summary(isFinished = true)
        setCard(summary = summary, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.SwipeRestart(id = summary.routine.id))
    }

    @Test
    fun `TC-0669af90-226a-46ea-b894-f44015f39613 아직 준비되지 않은 자리는 밀어도 완료되지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setCard(summary = null, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-802769f3-1dad-4fd2-bde5-7a246557d115 아직 준비되지 않은 자리는 밀어도 삭제되지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setCard(summary = null, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `아직 준비되지 않은 자리는 선택할 수 없다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setCard(summary = null, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

    @Test
    fun `준비된 루틴 카드를 누르면 그 루틴의 선택을 한 번 전달한다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        val summary = summary()
        setCard(summary = summary, onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.ClickRoutine(id = summary.routine.id))
    }

    @Test
    fun `아직 준비되지 않은 자리는 원형 표시, 제목과 카드 문구를 비워 둔다`() {
        setCard(summary = null)

        composeRule.onNodeWithTag(ROUTINE_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
        composeRule
            .onNodeWithTag(ROUTINE_CARD_TEST_TAG)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .map { text -> text.text }
            .filter { text -> text.isNotEmpty() }
            .shouldBeEmpty()
    }

    @Test
    fun `준비된 루틴 카드는 컬러 원형 표시를 둔다`() {
        setCard(summary = summary())

        composeRule.onNodeWithTag(ROUTINE_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true).assertExists()
    }

    private fun summary(isFinished: Boolean = false): RoutineSummary {
        val summary = Arb.routineSummary().next()

        return summary.copy(routine = summary.routine.copy(detail = summary.routine.detail.copy(title = ROUTINE_TITLE), isFinished = isFinished, isDeleted = false))
    }

    private fun setCard(
        summary: RoutineSummary?,
        onEvent: (RoutineListEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeRoutineCard(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                    state = rememberRoutineListState(initialToday = TODAY),
                    summary = summary,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val ROUTINE_TITLE = "Water"
    }
}
