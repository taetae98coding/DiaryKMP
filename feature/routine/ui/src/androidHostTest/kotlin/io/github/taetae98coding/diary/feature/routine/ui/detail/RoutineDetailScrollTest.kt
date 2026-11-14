package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 기록이 한 화면에 다 들어가지 않는 창에서, 본문을 내려 본 기록의 자리가 그대로인지 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h891dp")
class RoutineDetailScrollTest {
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
    fun `TC-7a7349a1-e605-4a3f-9d4d-603a58eb54c0 기록 넣기를 처리해도 보고 있던 화면 위치를 유지한다`() {
        val environment = scrollEnvironment()
        setScreen(environment)
        // 넣기를 실행한 뒤 저장이 끝나기 전에 화면을 내려 뒤쪽 기록을 본다.
        composeRule.routineRecordScrollToInput()
        composeRule.routineRecordClickAdd()
        verify(exactly = 1) { environment.recordViewModel.add(date = TODAY, amount = environment.detail.amountPerRecord) }
        val top = scrollToViewed(environment)

        composeRule.runOnIdle { environment.completeAdd(date = TODAY, amount = environment.detail.amountPerRecord) }
        composeRule.waitForIdle()

        environment.recordList.value.size shouldBe RECORD_COUNT + 1
        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-7a7349a1-e605-4a3f-9d4d-603a58eb54c0 기록 빼기를 처리해도 보고 있던 화면 위치를 유지한다`() {
        val environment = scrollEnvironment()
        environment.storeOnDelete()
        setScreen(environment)
        val top = scrollToViewed(environment)
        val removed = environment.recordList.value[VIEWED_INDEX + 1]

        composeRule.routineRecordClickRemove(date = removed.date, amountText = removed.amountText())

        verify(exactly = 1) { environment.recordViewModel.delete(recordId = removed.id) }
        composeRule.routineRecordRowNameList() shouldNotContain removed.rowName()
        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-7a7349a1-e605-4a3f-9d4d-603a58eb54c0 수정을 반영해도 보고 있던 화면 위치를 유지한다`() {
        val environment = scrollEnvironment()
        environment.storeOnUpdate()
        setScreen(environment)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()
        val top = scrollToViewed(environment)

        composeRule.routineRecordClickUpdate()

        environment.currentDetail.title shouldBe ROUTINE_RECORD_TITLE_EDITED
        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-7a7349a1-e605-4a3f-9d4d-603a58eb54c0 저장된 제목이 다른 경로에서 바뀌어도 보고 있던 화면 위치를 유지한다`() {
        val environment = scrollEnvironment()
        setScreen(environment)
        val top = scrollToViewed(environment)

        composeRule.runOnIdle { environment.updateDetail { detail -> detail.copy(title = ROUTINE_RECORD_TITLE_EDITED) } }
        composeRule.waitForIdle()

        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-7a7349a1-e605-4a3f-9d4d-603a58eb54c0 삭제에 실패해도 보고 있던 화면 위치를 유지한다`() {
        val environment = scrollEnvironment()
        setScreen(environment)
        val top = scrollToViewed(environment)

        // 삭제를 처리했지만 저장하지 못하면 진행 상태만 풀리고 화면에 머무른다.
        composeRule.routineRecordClickDelete()
        composeRule.runOnIdle { environment.uiState.value = RoutineDetailUiState.Content(id = environment.id, detail = environment.detail, isFinished = false, isDeleteInProgress = true) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { environment.uiState.value = RoutineDetailUiState.Content(id = environment.id, detail = environment.detail, isFinished = false) }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.routineViewModel.delete() }
        environment.navigateUpCount shouldBe 0
        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-0059cb49-216c-4ba8-893c-2e9b9f6c078b 화면이 회전해도 기록 내역에서 보던 자리를 유지한다`() {
        assertViewedKeptAcrossBoundary(isRecreate = true)
    }

    @Test
    fun `TC-0059cb49-216c-4ba8-893c-2e9b9f6c078b 다른 앱에 다녀와도 기록 내역에서 보던 자리를 유지한다`() {
        assertViewedKeptAcrossBoundary(isRecreate = false)
    }

    private fun assertViewedKeptAcrossBoundary(isRecreate: Boolean) {
        val environment = scrollEnvironment()
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RoutineRecordTestScreen(environment = environment)
            }
        }
        composeRule.waitForIdle()
        val top = scrollToViewed(environment)

        if (isRecreate) {
            restorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        assertViewedKept(environment = environment, top = top)
    }

    // RoutineHome의 빠른 기록, AI 도구, 다른 기기에서 넣은 기록은 모두 기기에 저장된 기록이 늘어나는 같은 변경으로 이 화면에 전해진다.
    @Test
    fun `TC-6719e1a4-585d-463f-9a2f-617b95bdb1e1 다른 경로에서 넣은 기록은 순서에 맞는 자리에 나타나고 보던 자리는 옮기지 않는다`() {
        val environment = scrollEnvironment()
        setScreen(environment)
        val top = scrollToViewed(environment)
        val before = environment.recordList.value[VIEWED_INDEX + 1]
        val after = environment.recordList.value[VIEWED_INDEX + 2]
        val added = routineRecordOf(date = before.date.minus(1, DateTimeUnit.DAY), amount = ADDED_AMOUNT, routineId = environment.id)

        composeRule.runOnIdle { environment.setRecordList(environment.recordList.value + added) }
        composeRule.waitForIdle()

        composeRule.routineRecordRowNameList().shouldContainInOrder(before.rowName(), added.rowName(), after.rowName())
        composeRule.onNode(routineRecordRow(date = added.date, amountText = added.amountText())).assertIsDisplayed()
        assertViewedKept(environment = environment, top = top)
    }

    @Test
    fun `TC-6719e1a4-585d-463f-9a2f-617b95bdb1e1 다른 기기에서 뺀 기록은 사라지고 보던 자리는 옮기지 않는다`() {
        val environment = scrollEnvironment()
        setScreen(environment)
        val top = scrollToViewed(environment)
        val removed = environment.recordList.value[VIEWED_INDEX + 2]
        composeRule.onNode(routineRecordRow(date = removed.date, amountText = removed.amountText())).assertIsDisplayed()

        composeRule.runOnIdle { environment.setRecordList(environment.recordList.value - removed) }
        composeRule.waitForIdle()

        composeRule.routineRecordRowNameList() shouldNotContain removed.rowName()
        assertViewedKept(environment = environment, top = top)
    }

    // 날짜가 이틀 간격인 기록을 날짜가 늦은 순으로 둔다. 그 사이의 날짜에 새 기록이 놓일 수 있다.
    private fun scrollEnvironment(): RoutineRecordTestEnvironment =
        RoutineRecordTestEnvironment(
            detail = routineRecordDetailOf(startDate = LocalDate(2025, 1, 1)),
            recordList = List(RECORD_COUNT) { index -> routineRecordOf(date = LAST_RECORD_DATE.minus(index * 2, DateTimeUnit.DAY), amount = index + 1) },
        )

    // 본문을 내려 뒤쪽 기록을 화면 맨 위에 두고 그 기록의 자리를 돌려준다.
    private fun scrollToViewed(environment: RoutineRecordTestEnvironment): Float {
        val viewed = environment.recordList.value[VIEWED_INDEX]

        composeRule.routineRecordScrollToRecord(VIEWED_INDEX)
        composeRule.onNode(routineRecordRow(date = viewed.date, amountText = viewed.amountText())).assertIsDisplayed()
        return composeRule.onNode(routineRecordRow(date = viewed.date, amountText = viewed.amountText())).routineRecordTop()
    }

    private fun assertViewedKept(
        environment: RoutineRecordTestEnvironment,
        top: Float,
    ) {
        val viewed = environment.recordList.value.first { record -> record.amount == VIEWED_INDEX + 1 }

        composeRule.onNode(routineRecordRow(date = viewed.date, amountText = viewed.amountText())).assertIsDisplayed()
        composeRule.onNode(routineRecordRow(date = viewed.date, amountText = viewed.amountText())).routineRecordTop() shouldBe top
    }

    private fun RoutineRecord.amountText(): String = "${amount}ml"

    private fun RoutineRecord.rowName(): String = routineRecordRowName(date = date, amountText = amountText())

    private fun setScreen(environment: RoutineRecordTestEnvironment) {
        composeRule.setContent { RoutineRecordTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val LAST_RECORD_DATE: LocalDate = LocalDate(2026, 3, 18)
        const val RECORD_COUNT = 40
        const val VIEWED_INDEX = 20
        const val ADDED_AMOUNT = 9_000
    }
}
