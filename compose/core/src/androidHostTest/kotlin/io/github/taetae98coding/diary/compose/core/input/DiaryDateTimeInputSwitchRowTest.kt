package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
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
class DiaryDateTimeInputSwitchRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `라벨 영역을 누르면 사용 여부가 전환된다`() {
        setDiaryDateTimeInput()

        val switchRow = composeRule.onNode(hasRole(Role.Switch))

        switchRow.performTouchInput {
            click(Offset(x = width * 0.05F, y = height / 2F))
        }

        switchRow.assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertExists()

        switchRow.performTouchInput {
            click(Offset(x = width * 0.05F, y = height / 2F))
        }

        switchRow.assertIsOff()
        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()
    }

    @Test
    fun `라벨과 스위치 사이의 빈 영역을 누르면 사용 여부가 전환된다`() {
        setDiaryDateTimeInput(initialValue = allDayValue())

        val switchRow = composeRule.onNode(hasRole(Role.Switch))

        switchRow.performTouchInput {
            click(Offset(x = width * 0.75F, y = height / 2F))
        }

        switchRow.assertIsOff()
        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()

        switchRow.performTouchInput {
            click(Offset(x = width * 0.75F, y = height / 2F))
        }

        switchRow.assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertExists()
    }

    @Test
    fun `카드 좌우 가장자리를 누르면 사용 여부가 전환된다`() {
        setDiaryDateTimeInput()

        val switchRow = composeRule.onNode(hasRole(Role.Switch))
        val switchRowCenterY =
            switchRow
                .fetchSemanticsNode()
                .boundsInRoot.center.y
        val root = composeRule.onRoot()

        root.performTouchInput {
            click(Offset(x = 1F, y = switchRowCenterY))
        }

        switchRow.assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertExists()

        root.performTouchInput {
            click(Offset(x = width - 1F, y = switchRowCenterY))
        }

        switchRow.assertIsOff()
        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()
    }

    @Test
    fun `상단 행은 환경의 최소 조작 높이를 따른다`() {
        val minimumInteractiveComponentSize = 64.dp

        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides minimumInteractiveComponentSize,
                ) {
                    DiaryDateTimeInput(state = rememberDiaryDateTimeInputState())
                }
            }
        }

        composeRule
            .onNode(hasRole(Role.Switch))
            .assertHeightIsAtLeast(minimumInteractiveComponentSize)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-021 끄기 전환 중 기간 선택 항목은 비활성화되고 완료 후 사라진다`() {
        composeRule.mainClock.autoAdvance = false
        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNode(hasRole(Role.Switch)).performTouchInput {
            click(center)
        }
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNode(hasRole(Role.Checkbox)).assertIsNotEnabled()
        composeRule.onNodeWithText(START_DATE_TEXT).assertIsNotEnabled()

        composeRule.mainClock.advanceTimeBy(ANIMATION_SETTLE_MILLIS)

        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()
        composeRule.onNodeWithText(START_DATE_TEXT).assertDoesNotExist()
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue? = null) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }
    }
}
