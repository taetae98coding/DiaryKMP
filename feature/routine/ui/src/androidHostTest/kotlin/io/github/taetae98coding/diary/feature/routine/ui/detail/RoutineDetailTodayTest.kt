package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 기록 넣기의 날짜를 정하는 오늘은 기기의 날짜이므로, 기기 시계만 테스트가 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        routineRecordSetToday(TODAY)
    }

    @After
    fun tearDown() {
        routineRecordReleaseToday()
    }

    @Test
    fun `TC-bf821646-2715-4e9a-a588-861284e95102 화면을 보는 동안 자정이 지나도 오늘을 다시 정하지 않는다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1)))
        composeRule.setContent { RoutineRecordTestScreen(environment = environment) }
        composeRule.waitForIdle()
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordInputAmount("500")

        routineRecordSetToday(TOMORROW)
        composeRule.mainClock.advanceTimeBy(ONE_MINUTE_MILLIS)
        composeRule.waitForIdle()
        composeRule.routineRecordClickAdd()

        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.onNode(routineRecordDateButton(TOMORROW)).assertDoesNotExist()
        verify(exactly = 1) { environment.recordViewModel.add(date = TODAY, amount = 500) }
        verify(exactly = 1) { environment.recordViewModel.add(date = any(), amount = any()) }
    }

    @Test
    fun `TC-54e64fd8-a6a7-4b69-a220-e2d2df1e6944 백그라운드에 갔다가 돌아와도 오늘을 다시 정하지 않는다`() {
        assertTodayKept(isRecreate = false)
    }

    @Test
    fun `TC-54e64fd8-a6a7-4b69-a220-e2d2df1e6944 화면이 재생성되어도 오늘을 다시 정하지 않는다`() {
        assertTodayKept(isRecreate = true)
    }

    private fun assertTodayKept(isRecreate: Boolean) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1)))
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RoutineRecordTestScreen(environment = environment)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()

        routineRecordSetToday(TOMORROW)
        if (isRecreate) {
            restorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.onNode(routineRecordDateButton(TOMORROW)).assertDoesNotExist()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val TOMORROW: LocalDate = LocalDate(2026, 3, 21)
        const val ONE_MINUTE_MILLIS = 60_000L
    }
}
