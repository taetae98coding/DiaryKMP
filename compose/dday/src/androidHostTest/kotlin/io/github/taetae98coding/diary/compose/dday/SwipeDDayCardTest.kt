package io.github.taetae98coding.diary.compose.dday

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
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
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
class SwipeDDayCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-43f05702-0b18-4db1-8c28-0633d0bbea97 디데이 카드를 우에서 좌로 밀면 그 디데이의 삭제를 한 번 전달한다`() {
        val eventList = mutableListOf<DDayListEvent>()
        val dday = dday()
        setCard(dday = dday, onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayListEvent.SwipeDelete(id = dday.id))
    }

    @Test
    fun `TC-e9494e1b-7937-4570-9859-73160a611797 디데이 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        val eventList = mutableListOf<DDayListEvent>()
        setCard(dday = dday(), onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-91e3b501-d853-4a87-ad0a-701d8c88647b 아직 준비되지 않은 자리는 밀어도 삭제되지 않는다`() {
        val eventList = mutableListOf<DDayListEvent>()
        setCard(dday = null, onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-19a0c296-a283-4d3c-8b2b-f0d67297582e TC-f37b3a3e-09fd-4ad0-bd7f-db2f0cb9e101 자리 표시는 선택할 수 없다`() {
        val eventList = mutableListOf<DDayListEvent>()
        setCard(dday = null, onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

    @Test
    fun `준비된 디데이 카드를 누르면 그 디데이의 선택을 한 번 전달한다`() {
        val eventList = mutableListOf<DDayListEvent>()
        val dday = dday()
        setCard(dday = dday, onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayListEvent.ClickDDay(id = dday.id))
    }

    @Test
    fun `아직 준비되지 않은 자리는 원형 표시, 제목과 카드 문구를 비워 둔다`() {
        setCard(dday = null)

        composeRule.onNodeWithTag(DDAY_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
        composeRule
            .onNodeWithTag(DDAY_CARD_TEST_TAG)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .map { text -> text.text }
            .filter { text -> text.isNotEmpty() }
            .shouldBeEmpty()
    }

    @Test
    fun `준비된 디데이 카드는 컬러 원형 표시를 둔다`() {
        setCard(dday = dday())

        composeRule.onNodeWithTag(DDAY_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true).assertExists()
    }

    private fun dday(): DDay =
        Arb
            .dday()
            .next()
            .copy(detail = Arb.ddayDetail().next().copy(title = DDAY_TITLE), isDeleted = false)

    private fun setCard(
        dday: DDay?,
        onEvent: (DDayListEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeDDayCard(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                    state = rememberDDayListState(initialToday = TODAY),
                    dday = dday,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 3, day = 20)
        const val DDAY_TITLE = "Anniversary"
    }
}
