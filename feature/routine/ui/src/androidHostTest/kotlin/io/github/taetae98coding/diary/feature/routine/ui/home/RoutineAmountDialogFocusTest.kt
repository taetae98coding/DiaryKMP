package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import androidx.test.platform.app.InstrumentationRegistry
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 물리 키보드로 빠른 기록 버튼에 초점을 두고 양 입력 창을 연 뒤, 창을 닫는 방법마다 초점이 돌아오는 자리를 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineAmountDialogFocusTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(TODAY)
        InstrumentationRegistry.getInstrumentation().setInTouchMode(false)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `양 입력 창을 확인으로 닫으면 창을 연 빠른 기록 버튼으로 초점이 돌아간다`() {
        assertFocusReturned {
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        }
    }

    @Test
    fun `양 입력 창을 취소로 닫으면 창을 연 빠른 기록 버튼으로 초점이 돌아간다`() {
        assertFocusReturned {
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CANCEL)).performClick()
        }
    }

    @Test
    fun `양 입력 창 밖으로 벗어나 닫으면 창을 연 빠른 기록 버튼으로 초점이 돌아간다`() {
        assertFocusReturned {
            composeRule.runOnIdle { dismissRoutineAmountDialogOutside() }
        }
    }

    private fun assertFocusReturned(close: () -> Unit) {
        environment.setSummaryList(listOf(summary(title = FIRST_TITLE), summary(title = SECOND_TITLE)))
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(SECOND_TITLE))
        composeRule.onNode(routineHomeQuickRecordButton(SECOND_TITLE)).requestFocus()
        composeRule.onNode(routineHomeQuickRecordButton(SECOND_TITLE)).assertIsFocused()

        composeRule.onNode(routineHomeQuickRecordButton(SECOND_TITLE)).performKeyInput {
            keyDown(Key.ShiftLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.ShiftLeft)
        }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineAmountDialogField())

        close()
        composeRule.waitForIdle()

        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onNode(routineHomeQuickRecordButton(SECOND_TITLE)).assertIsFocused()
        composeRule.onNode(routineHomeQuickRecordButton(FIRST_TITLE)).assertIsNotFocused()
    }

    private fun summary(title: String): RoutineSummary = routineHomeQuickRecordSummary(title = title, startDate = START_DATE)

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val FIRST_TITLE = "물 마시기"
        const val SECOND_TITLE = "운동"
    }
}
