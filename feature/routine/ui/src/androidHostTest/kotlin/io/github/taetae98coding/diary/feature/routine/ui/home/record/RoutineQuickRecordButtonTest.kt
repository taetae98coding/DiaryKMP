package io.github.taetae98coding.diary.feature.routine.ui.home.record

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.test.platform.app.InstrumentationRegistry
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 누름 표시가 늦게 시작되는 스크롤 목록 안에서도 길게 누르기를 누른 순간부터 재는지 보도록 버튼을 목록에 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineQuickRecordButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
    private var clickCount = 0
    private var amountInputRequestCount = 0

    @Test
    fun `길게 누르기 인식 시간 직후에 양 입력을 요청하고 손을 떼어도 누르기는 실행하지 않는다`() {
        setButton()

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + AFTER_LONG_PRESS_MILLIS)
            moveBy(Offset.Zero)
        }
        composeRule.runOnIdle {
            amountInputRequestCount shouldBe 1
            verify(exactly = 1) { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }
        }

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performTouchInput { up() }

        composeRule.runOnIdle {
            clickCount shouldBe 0
            amountInputRequestCount shouldBe 1
        }
    }

    @Test
    fun `길게 누르기 인식 시간 전에 손을 떼면 누르기만 실행한다`() {
        setButton()

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis - BEFORE_LONG_PRESS_MILLIS)
            up()
        }

        composeRule.runOnIdle {
            clickCount shouldBe 1
            amountInputRequestCount shouldBe 0
            verify(exactly = 0) { hapticFeedback.performHapticFeedback(any()) }
        }
    }

    @Test
    fun `길게 누른 채 버튼 밖으로 벗어나 떼면 두 조작 모두 실행하지 않는다`() {
        setButton()

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performTouchInput {
            down(center)
            moveTo(Offset(x = centerX, y = bottom + OUTSIDE_DISTANCE_PX))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + AFTER_LONG_PRESS_MILLIS)
            up()
        }

        composeRule.runOnIdle {
            clickCount shouldBe 0
            amountInputRequestCount shouldBe 0
        }
    }

    @Test
    fun `Enter를 오래 눌러도 길게 누르기로 보지 않고 떼면 누르기를 실행한다`() {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(false)
        setButton()
        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).requestFocus()
        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).assertIsFocused()
        var longPressTimeoutMillis = 0L
        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performTouchInput { longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis }

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performKeyInput { keyDown(Key.Enter) }
        composeRule.mainClock.advanceTimeBy(longPressTimeoutMillis * 2)
        composeRule.runOnIdle {
            clickCount shouldBe 0
            amountInputRequestCount shouldBe 0
        }
        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performKeyInput { keyUp(Key.Enter) }

        composeRule.runOnIdle {
            clickCount shouldBe 1
            amountInputRequestCount shouldBe 0
        }
    }

    @Test
    fun `Shift와 Enter를 함께 누르면 누르기 없이 양 입력을 요청한다`() {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(false)
        setButton()
        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).requestFocus()

        composeRule.onNodeWithTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG).performKeyInput {
            keyDown(Key.ShiftLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.ShiftLeft)
        }

        composeRule.runOnIdle {
            clickCount shouldBe 0
            amountInputRequestCount shouldBe 1
            verify(exactly = 0) { hapticFeedback.performHapticFeedback(any()) }
        }
    }

    private fun setButton() {
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalHapticFeedback provides hapticFeedback) {
                    LazyColumn {
                        item {
                            RoutineQuickRecordButton(
                                onClick = { clickCount += 1 },
                                onAmountInputRequest = { amountInputRequestCount += 1 },
                                title = "물 마시기",
                                amount = 250,
                                unit = "ml",
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val AFTER_LONG_PRESS_MILLIS = 1L
        const val BEFORE_LONG_PRESS_MILLIS = 50L
        const val OUTSIDE_DISTANCE_PX = 200f
    }
}
