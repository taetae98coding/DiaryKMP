package io.github.taetae98coding.diary.compose.memo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeMemoCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-002 TC-TAG-DETAIL-MEMO-FEATURE-004 TC-MEMO-FINISHED-LIST-FEATURE-005 TC-TAG-MEMO-FINISHED-LIST-FEATURE-008 자리 표시 카드는 선택하거나 스와이프할 수 없다`() {
        var clickCount = 0
        var finishCount = 0
        var deleteCount = 0
        setSwipeMemoCard(
            memo = null,
            onClick = { clickCount += 1 },
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText("").performClick()
        composeRule.onNodeWithText("").performTouchInput { swipeRight() }
        composeRule.onNodeWithText("").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        clickCount shouldBe 0
        finishCount shouldBe 0
        deleteCount shouldBe 0
        composeRule.onNodeWithText("").assertExists()
    }

    @Test
    fun `실행 기준을 넘지 않은 스와이프는 아무 일도 일으키지 않는다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeMemoCard(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput {
            swipe(
                start = centerLeft + Offset(SHORT_SWIPE_START_OFFSET, 0f),
                end = centerLeft + Offset(SHORT_SWIPE_END_OFFSET, 0f),
                durationMillis = SHORT_SWIPE_DURATION_MILLIS,
            )
        }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 0
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Test
    fun `기본 환경 좌에서 우로 절반을 넘으면 완료 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = 1f,
            shownDescription = DEFAULT_FINISH_DESCRIPTION,
            hiddenDescription = DEFAULT_DELETE_DESCRIPTION,
        )
    }

    @Test
    fun `기본 환경 우에서 좌로 절반을 넘으면 삭제 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = -1f,
            shownDescription = DEFAULT_DELETE_DESCRIPTION,
            hiddenDescription = DEFAULT_FINISH_DESCRIPTION,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 좌에서 우로 절반을 넘으면 완료 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = 1f,
            shownDescription = KOREAN_FINISH_DESCRIPTION,
            hiddenDescription = KOREAN_DELETE_DESCRIPTION,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 우에서 좌로 절반을 넘으면 삭제 아이콘만 표시한다`() {
        assertActionIconShownAfterHalfSwipe(
            direction = -1f,
            shownDescription = KOREAN_DELETE_DESCRIPTION,
            hiddenDescription = KOREAN_FINISH_DESCRIPTION,
        )
    }

    private fun assertActionIconShownAfterHalfSwipe(
        direction: Float,
        shownDescription: String,
        hiddenDescription: String,
    ) {
        setSwipeMemoCard()

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput {
            down(center)
            moveBy(delta = Offset(direction * PRE_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(shownDescription).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(hiddenDescription).assertDoesNotExist()

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput {
            moveBy(delta = Offset(direction * PAST_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(shownDescription).assertExists()
        composeRule.onNodeWithContentDescription(hiddenDescription).assertDoesNotExist()
    }

    @Test
    fun `좌에서 우로 스와이프하면 완료 동작을 한 번 실행한다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeMemoCard(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        finishCount shouldBe 1
        deleteCount shouldBe 0
    }

    @Test
    fun `우에서 좌로 스와이프하면 삭제 동작을 한 번 실행한다`() {
        var finishCount = 0
        var deleteCount = 0
        setSwipeMemoCard(
            onFinish = { finishCount += 1 },
            onDelete = { deleteCount += 1 },
        )

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        finishCount shouldBe 0
        deleteCount shouldBe 1
    }

    private fun setSwipeMemoCard(
        memo: Memo? = memo(),
        onClick: () -> Unit = {},
        onFinish: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeMemoCard(
                    onEvent = { event ->
                        when (event) {
                            is MemoListEvent.ClickMemo -> onClick()
                            is MemoListEvent.SwipeFinish -> onFinish()
                            is MemoListEvent.SwipeDelete -> onDelete()
                            is MemoListEvent.Refresh -> Unit
                        }
                    },
                    memo = memo,
                )
            }
        }
    }

    public companion object {
        private const val SHORT_SWIPE_START_OFFSET = 1f
        private const val SHORT_SWIPE_END_OFFSET = 21f
        private const val SHORT_SWIPE_DURATION_MILLIS = 1_000L
        private const val PRE_HALF_DRAG_OFFSET = 40f
        private const val PAST_HALF_DRAG_OFFSET = 160f
        private const val DRAG_DELAY_MILLIS = 300L
        private const val DEFAULT_FINISH_DESCRIPTION = "Finish memo"
        private const val DEFAULT_DELETE_DESCRIPTION = "Delete memo"
        private const val KOREAN_FINISH_DESCRIPTION = "메모 완료"
        private const val KOREAN_DELETE_DESCRIPTION = "메모 삭제"
        private const val MEMO_TITLE = "SwipeMemoCardTitle"

        private fun memo(): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = MEMO_TITLE))
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
