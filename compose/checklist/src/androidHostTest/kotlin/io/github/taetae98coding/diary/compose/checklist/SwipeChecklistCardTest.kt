package io.github.taetae98coding.diary.compose.checklist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklistSummary
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SwipeChecklistCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c7d4827a-f896-4048-b403-21e2cf5f12c9 체크리스트 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        setCard(summary = Arb.checklistSummary().next(), onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
    }

    @Test
    fun `TC-c9b6b398-b2e1-4f21-ba60-9eaa2d97ad70 TC-46d2b79a-3029-4b06-97f0-0cd9e132e627 체크리스트 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        val summary = Arb.checklistSummary().next()
        setCard(summary = summary, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithText(summary.checklist.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-eb5fc8b1-67e0-48ea-8931-8734903b6b3f TC-9bc778b6-ac23-45ae-944a-dc013365c7b7 아직 준비되지 않은 자리는 밀어도 삭제되지 않는다`() {
        var deleteCount = 0
        setCard(summary = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-09bb4ade-e45c-4478-8a7b-0a6f8eb3449f TC-265611ed-f90e-4668-a299-1176cd13ac05 아직 준비되지 않은 자리는 선택해도 이동하지 않는다`() {
        var clickCount = 0
        setCard(summary = null, onClick = { clickCount += 1 })

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    @Test
    fun `준비된 체크리스트 카드를 누르면 선택 동작을 한 번 실행한다`() {
        var clickCount = 0
        setCard(summary = Arb.checklistSummary().next(), onClick = { clickCount += 1 })

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    private fun setCard(
        summary: ChecklistSummary?,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeChecklistCard(
                    onEvent = { event ->
                        when (event) {
                            is ChecklistListEvent.ClickChecklist -> onClick()
                            is ChecklistListEvent.SwipeDelete -> onDelete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    summary = summary,
                )
            }
        }
    }
}
