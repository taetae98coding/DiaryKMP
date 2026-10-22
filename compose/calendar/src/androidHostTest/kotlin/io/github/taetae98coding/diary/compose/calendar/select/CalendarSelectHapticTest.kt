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
    fun `TC-cd832836-0235-4443-9a5b-c5290f9b5372 날짜 선택을 시작하면 촉각 피드백을 받는다`() {
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
    fun `TC-4506cdc3-b2ec-4487-ae81-6087efa5ad21 드래그로 선택 기간이 달라지면 추가 촉각 피드백을 받는다`() {
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
    fun `TC-67044a6f-131d-498e-bc78-715522a207b2 드래그해도 선택 기간이 그대로이면 추가 촉각 피드백을 받지 않는다`() {
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
