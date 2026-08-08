package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarSelectHapticTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-011 날짜 선택을 시작하면 촉각 피드백을 받는다`() {
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        composeRule.setCalendar(hapticFeedback = hapticFeedback)

        composeRule.performLongPress(composeRule.dayCenter(day = 15))

        verify(exactly = 1) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        verify(exactly = 0) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-012 드래그로 선택 기간이 달라지면 추가 촉각 피드백을 받는다`() {
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        composeRule.setCalendar(hapticFeedback = hapticFeedback)
        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        clearMocks(hapticFeedback, answers = false)

        composeRule.performMoveTo(composeRule.dayCenter(day = 17))

        verify(exactly = 1) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-013 드래그해도 선택 기간이 그대로이면 추가 촉각 피드백을 받지 않는다`() {
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        composeRule.setCalendar(hapticFeedback = hapticFeedback)
        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        val end = composeRule.dayCenter(day = 17)
        composeRule.performMoveTo(end)
        clearMocks(hapticFeedback, answers = false)

        composeRule.performMoveTo(end + Offset(x = 1F, y = 1F))

        verify(exactly = 0) {
            hapticFeedback.performHapticFeedback(any())
        }
    }
}
