package io.github.taetae98coding.diary.compose.core.format

import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val DATES =
    listOf(
        LocalDate(year = 2026, month = 7, day = 19),
        LocalDate(year = 2026, month = 12, day = 25),
        LocalDate(year = 2026, month = 1, day = 5),
    )

private val TIMES =
    listOf(
        LocalTime(hour = 13, minute = 30),
        LocalTime(hour = 9, minute = 5),
        LocalTime(hour = 0, minute = 0),
        LocalTime(hour = 12, minute = 0),
        LocalTime(hour = 23, minute = 59),
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DateTimeDisplayTextExtTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 날짜를 표시한다`() {
        setDates()

        assertTexts("2026. 7. 19.", "2026. 12. 25.", "2026. 1. 5.")
    }

    @Test
    fun `기본 환경에서 날짜를 표시한다`() {
        setDates()

        assertTexts("Jul 19, 2026", "Dec 25, 2026", "Jan 5, 2026")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 시간을 12시간제로 표시한다`() {
        setTimes()

        assertTexts("오후 1:30", "오전 9:05", "오전 12:00", "오후 12:00", "오후 11:59")
    }

    @Test
    fun `기본 환경에서 시간을 12시간제로 표시한다`() {
        setTimes()

        assertTexts("1:30 PM", "9:05 AM", "12:00 AM", "12:00 PM", "11:59 PM")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `기기가 24시간제여도 한국어 환경 시간 표시는 12시간제를 유지한다`() {
        setDeviceTimeFormat24Hour()
        setTimes()

        assertTexts("오후 1:30", "오전 12:00")
    }

    @Test
    fun `기기가 24시간제여도 기본 환경 시간 표시는 12시간제를 유지한다`() {
        setDeviceTimeFormat24Hour()
        setTimes()

        assertTexts("1:30 PM", "12:00 AM")
    }

    private fun assertTexts(vararg texts: String) {
        texts.forEach { composeRule.onNodeWithText(it).assertExists() }
    }

    private fun setDeviceTimeFormat24Hour() {
        Settings.System.putString(
            RuntimeEnvironment.getApplication().contentResolver,
            Settings.System.TIME_12_24,
            "24",
        )
    }

    private fun setDates() {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    DATES.forEach { Text(text = it.toDisplayText()) }
                }
            }
        }
    }

    private fun setTimes() {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    TIMES.forEach { Text(text = it.toDisplayText()) }
                }
            }
        }
    }
}
