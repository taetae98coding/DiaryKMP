package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeToFinishAndDeleteBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-001 좌에서 우로 스와이프하면 완료 동작을 한 번 실행한다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeToFinishAndDeleteBox(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        finishCount shouldBe 1
        deleteCount shouldBe 0
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-002 우에서 좌로 스와이프하면 삭제 동작을 한 번 실행한다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeToFinishAndDeleteBox(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 1
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-006 시작 방향 동작 뒤에도 카드가 남으면 원래 모양으로 돌아와 다시 실행할 수 있다`() {
        var finishCount = 0
        setSwipeToFinishAndDeleteBox(onFinish = { finishCount += 1 })

        repeat(2) {
            composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeRight() }
            composeRule.waitForIdle()
            composeRule.mainClock.advanceTimeBy(RESET_WAIT_MILLIS)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        }

        finishCount shouldBe 2
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-006 끝 방향 동작 뒤에도 카드가 남으면 원래 모양으로 돌아와 다시 실행할 수 있다`() {
        var deleteCount = 0
        setSwipeToFinishAndDeleteBox(onDelete = { deleteCount += 1 })

        repeat(2) {
            composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
            composeRule.mainClock.advanceTimeBy(RESET_WAIT_MILLIS)
            composeRule.waitForIdle()

            composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        }

        deleteCount shouldBe 2
    }

    @Test
    fun `좌에서 우로 절반을 넘으면 완료 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = 1f,
            shownDescription = FINISH_DESCRIPTION,
            hiddenDescription = DELETE_DESCRIPTION,
        )
    }

    @Test
    fun `우에서 좌로 절반을 넘으면 삭제 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = -1f,
            shownDescription = DELETE_DESCRIPTION,
            hiddenDescription = FINISH_DESCRIPTION,
        )
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-004 실행 기준을 넘지 않은 스와이프는 아무 동작도 실행하지 않는다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeToFinishAndDeleteBox(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput {
            swipe(
                start = centerLeft + Offset(SHORT_SWIPE_START_OFFSET, 0f),
                end = centerLeft + Offset(SHORT_SWIPE_END_OFFSET, 0f),
                durationMillis = SHORT_SWIPE_DURATION_MILLIS,
            )
        }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 0
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-FEATURE-005 스와이프가 비활성화된 카드는 스와이프해도 동작이 실행되지 않는다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeToFinishAndDeleteBox(
            gesturesEnabled = false,
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeRight() }
        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 0
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-001 다른 항목을 표시하면 스와이프 상태가 초기화된다`() {
        val firstKey = fixtureMonkey.giveMeOne<Long>()
        var key by mutableStateOf(firstKey)
        var finishCount = 0
        composeRule.setContent {
            DiaryTheme {
                SwipeToFinishAndDeleteBox(
                    key = key,
                    onFinish = { finishCount += 1 },
                    onDelete = {},
                    finishContentDescription = FINISH_DESCRIPTION,
                    deleteContentDescription = DELETE_DESCRIPTION,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Content()
                }
            }
        }

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        finishCount shouldBe 1

        composeRule.runOnIdle { key = firstKey + 1 }
        composeRule.waitForIdle()

        finishCount shouldBe 1
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-SWIPE-TO-FINISH-AND-DELETE-DOMAIN-002 실행 취소로 다시 나타난 카드는 실행 전 상태로 표시되고 동작을 다시 실행하지 않는다`() {
        val key = fixtureMonkey.giveMeOne<Long>()
        var isShown by mutableStateOf(true)
        var finishCount = 0
        var deleteCount = 0
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    SwipeToFinishAndDeleteBox(
                        key = key,
                        onFinish = { finishCount += 1 },
                        onDelete = { deleteCount += 1 },
                        finishContentDescription = FINISH_DESCRIPTION,
                        deleteContentDescription = DELETE_DESCRIPTION,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Content()
                    }
                }
            }
        }
        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        deleteCount shouldBe 1

        // 동작이 반영되면 카드가 목록에서 사라지고, 실행 취소하면 같은 항목의 카드가 다시 나타난다.
        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 1
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(FINISH_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DELETE_DESCRIPTION).assertDoesNotExist()
    }

    // 구현 계약 회귀 테스트: 실행 기준 전 원형 아이콘은 작게, 기준을 넘은 동작 아이콘은 크게 표시되어야 한다.
    // AnimatedContent에 고정 크기를 주면 자식이 그 크기 제약을 그대로 물려받아 원형 아이콘이 동작 아이콘 크기로
    // 확대되던 결함(Modifier.size는 부모 제약에 종속)의 재발을 막는다.
    @Test
    fun `좌에서 우로 스와이프하면 원형 아이콘은 작게 완료 아이콘은 크게 표시된다`() {
        assertSwipeIconSizes(direction = 1f, actionDescription = FINISH_DESCRIPTION)
    }

    @Test
    fun `우에서 좌로 스와이프하면 원형 아이콘은 작게 삭제 아이콘은 크게 표시된다`() {
        assertSwipeIconSizes(direction = -1f, actionDescription = DELETE_DESCRIPTION)
    }

    private fun assertActionIconShownAfterHalfSwipe(
        direction: Float,
        shownDescription: String,
        hiddenDescription: String,
    ) {
        setSwipeToFinishAndDeleteBox()

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput {
            down(center)
            moveBy(delta = Offset(direction * PRE_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(shownDescription).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(hiddenDescription).assertDoesNotExist()

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput {
            moveBy(delta = Offset(direction * PAST_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(shownDescription).assertExists()
        composeRule.onNodeWithContentDescription(hiddenDescription).assertDoesNotExist()
    }

    private fun assertSwipeIconSizes(
        direction: Float,
        actionDescription: String,
    ) {
        setSwipeToFinishAndDeleteBox()

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput {
            down(center)
            moveBy(delta = Offset(direction * PRE_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(SWIPE_TO_FINISH_AND_DELETE_CIRCLE_ICON_TEST_TAG, useUnmergedTree = true)
            .assertWidthIsEqualTo(CIRCLE_ICON_SIZE)
            .assertHeightIsEqualTo(CIRCLE_ICON_SIZE)

        composeRule.onNodeWithText(CONTENT_TEXT).performTouchInput {
            moveBy(delta = Offset(direction * PAST_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithContentDescription(actionDescription, useUnmergedTree = true)
            .assertWidthIsEqualTo(ACTION_ICON_SIZE)
            .assertHeightIsEqualTo(ACTION_ICON_SIZE)
    }

    private fun setSwipeToFinishAndDeleteBox(
        gesturesEnabled: Boolean = true,
        onFinish: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeToFinishAndDeleteBox(
                    key = null,
                    onFinish = onFinish,
                    onDelete = onDelete,
                    finishContentDescription = FINISH_DESCRIPTION,
                    deleteContentDescription = DELETE_DESCRIPTION,
                    modifier = Modifier.fillMaxWidth(),
                    gesturesEnabled = gesturesEnabled,
                ) {
                    Content()
                }
            }
        }
    }

    @Composable
    private fun Content() {
        Card {
            Text(
                text = CONTENT_TEXT,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    public companion object {
        private const val RESET_WAIT_MILLIS = 2_000L
        private const val SHORT_SWIPE_START_OFFSET = 1f
        private const val SHORT_SWIPE_END_OFFSET = 21f
        private const val SHORT_SWIPE_DURATION_MILLIS = 1_000L
        private const val PRE_HALF_DRAG_OFFSET = 40f
        private const val PAST_HALF_DRAG_OFFSET = 160f
        private const val DRAG_DELAY_MILLIS = 300L
        private const val FINISH_DESCRIPTION = "SwipeToFinishAndDeleteFinish"
        private const val DELETE_DESCRIPTION = "SwipeToFinishAndDeleteDelete"
        private const val CONTENT_TEXT = "SwipeToFinishAndDeleteContent"
        private val CIRCLE_ICON_SIZE = 16.dp
        private val ACTION_ICON_SIZE = 32.dp
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
