package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.defaultTimeText
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.hasRole
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ANIMATION_SETTLE_MILLIS = 1_000L

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputAllDayRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `좌우 여백을 누르면 종일 여부가 전환된다`() {
        val defaultTimeText = defaultTimeText()

        setDiaryDateTimeInput()

        val allDayRow = composeRule.onNode(hasRole(Role.Checkbox))

        allDayRow.performTouchInput {
            click(Offset(x = 1F, y = height / 2F))
        }

        allDayRow.assertIsOff()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(2)

        allDayRow.performTouchInput {
            click(Offset(x = width - 1F, y = height / 2F))
        }

        allDayRow.assertIsOn()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(0)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-006 종일 전환 중 시간은 비활성화되고 완료 후 사라진다`() {
        composeRule.mainClock.autoAdvance = false
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNode(hasRole(Role.Checkbox)).performTouchInput {
            click(center)
        }
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNodeWithText(START_TIME_TEXT).assertIsNotEnabled()
        composeRule.onNodeWithText(END_TIME_TEXT).assertIsNotEnabled()

        composeRule.mainClock.advanceTimeBy(ANIMATION_SETTLE_MILLIS)

        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(END_TIME_TEXT).assertDoesNotExist()
    }

    @Test
    fun `종일 행은 환경의 최소 조작 높이를 따른다`() {
        val minimumInteractiveComponentSize = 64.dp

        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides minimumInteractiveComponentSize,
                ) {
                    DiaryDateTimeInput(
                        state = rememberDiaryDateTimeInputState(initialValue = allDayValue()),
                    )
                }
            }
        }

        composeRule
            .onNode(hasRole(Role.Checkbox))
            .assertHeightIsAtLeast(minimumInteractiveComponentSize)
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue = allDayValue()) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(
                    state = rememberDiaryDateTimeInputState(initialValue = initialValue),
                )
            }
        }
    }
}
