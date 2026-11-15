package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.RECORD_EMPTY_TITLE
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.RECORD_UNDO_ACTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.UNIT_COUNT
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 상세 영역에서 바꾼 내용이 같은 화면의 목록과 달력에, 목록에서 넣은 기록이 상세 영역에 반영되는지를 제품 routineEntry로 구성한 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineListDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-c7b51d7d-495b-48d2-9788-40fb1c5d46a3 선택 전 루틴 추가에서 추가에 성공하면 루틴 추가로 남고 목록에 나타난다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.titleInput().performTextInput(ADDED_TITLE)
        composeRule.waitForIdle()

        robot.clickDetailAdd()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.waitUntilExists(robot.card(title = ADDED_TITLE, cardText = "Today 0"))
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-33f2fee8-b467-4a9e-8c47-6aed77dd6255 상세 영역에서 수정하면 목록의 카드 문구와 자리가 같은 화면에서 바뀐다`() {
        val ga = routine(title = "가", unit = "ml", goalAmount = 2000)
        val na = routine(title = "나")
        val da = routine(title = "다")
        robot.setNavDisplay(
            routineList = listOf(ga, na, da),
            recordList = listOf(routineListDetailRecord(routine = ga, date = TODAY, amount = 1500)),
        )
        robot.selectRoutine(ga)
        composeRule.onNode(robot.card(title = "가", cardText = "Today 1500 / 2000ml") and hasText("75%")).assertExists()

        robot.titleInput().performTextReplacement("라")
        robot.goalAmountInput().performTextReplacement("3000")
        composeRule.waitForIdle()
        robot.clickDetailUpdate()

        backStack.last() shouldBe RoutineDetailNavKey(id = ga.id)
        robot.assertDetailOnDetailPane(title = "라")
        robot.waitUntilExists(robot.card(title = "라", cardText = "Today 1500 / 3000ml") and hasText("50%"))
        listOf("나", "다", "라")
            .map { title -> composeRule.onNode(robot.card(title)).fetchSemanticsNode().boundsInRoot }
            .zipWithNext()
            .forEach { (before, after) -> (before.top < after.top || (before.top == after.top && before.left < after.left)) shouldBe true }
    }

    @Test
    fun `TC-49356cc6-7058-48e5-8c73-974dfda4fe58 상세 영역에서 수정하면 달력에 놓인 그 루틴의 기록이 같은 화면에서 바뀐다`() {
        val routine = routine(title = WATER_TITLE, unit = "ml")
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList = listOf(routineListDetailRecord(routine = routine, date = TODAY, amount = 1500)),
        )
        robot.showCalendar()
        robot.selectCalendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)
        robot.assertDetailOnDetailPane(routine)

        robot.titleInput().performTextReplacement(RENAMED_WATER_TITLE)
        robot.inputUnit(unit = UNIT_COUNT)
        robot.clickDetailUpdate()

        robot.waitUntilExists(robot.calendarRecord(title = RENAMED_WATER_TITLE, amountText = "1500회"))
        composeRule.onNode(robot.calendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)).assertDoesNotExist()
        robot.assertCalendarView()
        robot.assertDetailOnDetailPane(title = RENAMED_WATER_TITLE)
    }

    @Test
    fun `TC-45f21e26-926c-4355-ac6b-45031a5312f5 목록 보기에서 상세 영역에 기록을 넣으면 카드 문구가 바뀐다`() {
        val routine = setWaterRoutineOnDetail()
        composeRule.onNode(robot.card(title = WATER_TITLE, cardText = "Today 1500 / 2000ml") and hasText("75%")).assertExists()

        robot.clickRecordAdd()

        robot.waitUntilExists(robot.card(title = WATER_TITLE, cardText = "Today 1750 / 2000ml") and hasText("87%"))
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-45f21e26-926c-4355-ac6b-45031a5312f5 목록 보기에서 상세 영역의 기록을 빼면 카드 문구가 바뀐다`() {
        val routine = setWaterRoutineOnDetail()

        robot.clickRecordRemove(dateText = TODAY_TEXT, amountText = AMOUNT_500_ML_TEXT)

        robot.waitUntilExists(robot.card(title = WATER_TITLE, cardText = "Today 1000 / 2000ml") and hasText("50%"))
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-45f21e26-926c-4355-ac6b-45031a5312f5 달력 보기에서 상세 영역에 기록을 넣으면 그 날짜의 아이템이 바뀐다`() {
        val routine = setWaterRoutineOnDetail()
        robot.showCalendar()
        composeRule.onNode(robot.calendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)).assertExists()

        robot.clickRecordAdd()

        robot.waitUntilExists(robot.calendarRecord(title = WATER_TITLE, amountText = "1750ml"))
        composeRule.onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG)).assertCountEquals(1)
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-45f21e26-926c-4355-ac6b-45031a5312f5 달력 보기에서 상세 영역의 오늘 기록을 모두 빼면 그 날짜의 아이템이 사라진다`() {
        val routine = setWaterRoutineOnDetail()
        robot.showCalendar()
        composeRule.onNode(robot.calendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)).assertExists()

        robot.clickRecordRemove(dateText = TODAY_TEXT, amountText = "1000ml")
        robot.clickRecordRemove(dateText = TODAY_TEXT, amountText = AMOUNT_500_ML_TEXT)

        robot.waitUntilGone(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG))
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-e21a6104-9618-4e5a-b539-09ed720759c2 루틴 추가가 놓인 동안 빠른 기록을 눌러도 상세 영역은 그대로다`() {
        val routine = routine(title = FIRST_TITLE, unit = "ml", amountPerRecord = 250)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        robot.quickRecord(routine)

        environment.storedRecordList.map { record -> record.amount } shouldBe listOf(250)
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe TYPED_ADD_TITLE
    }

    @Test
    fun `TC-e21a6104-9618-4e5a-b539-09ed720759c2 루틴 추가가 놓인 동안 양 입력 창에서 확인해도 상세 영역은 그대로다`() {
        val routine = routine(title = FIRST_TITLE, unit = "ml", amountPerRecord = 250)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        robot.amountRecord(routine = routine, amount = "300")

        environment.storedRecordList.map { record -> record.amount } shouldBe listOf(300)
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe TYPED_ADD_TITLE
    }

    @Test
    fun `TC-e21a6104-9618-4e5a-b539-09ed720759c2 다른 루틴의 상세가 놓인 동안 빠른 기록을 눌러도 상세 영역은 그대로다`() {
        val recorded = routine(title = FIRST_TITLE, unit = "ml", amountPerRecord = 250)
        val opened = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(recorded, opened))
        robot.selectRoutine(opened)

        robot.quickRecord(recorded)

        environment.storedRecordList.map { record -> record.routineId } shouldBe listOf(recorded.id)
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = opened.id))
        robot.assertDetailOnDetailPane(opened)
    }

    @Test
    fun `TC-e21a6104-9618-4e5a-b539-09ed720759c2 기록한 루틴의 상세가 놓인 동안 빠른 기록을 눌러도 상세 영역은 그대로다`() {
        val routine = routine(title = FIRST_TITLE, unit = "ml", amountPerRecord = 250)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)

        robot.quickRecord(routine)

        environment.storedRecordList shouldHaveSize 1
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-8f9a9002-5f38-454d-a541-c10a02cc7146 상세 영역의 루틴에 목록 카드에서 기록하면 기록 내역에 나타나고 실행 취소하면 사라진다`() {
        val routine = routine(title = FIRST_TITLE, unit = "ml", amountPerRecord = 250)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)
        robot.waitUntilExists(hasText(RECORD_EMPTY_TITLE))

        robot.quickRecord(routine)

        robot.waitUntilExists(robot.recordRow(dateText = TODAY_TEXT, amountText = "250ml"))
        composeRule.onNodeWithText(RECORD_EMPTY_TITLE).assertDoesNotExist()

        robot.waitUntilExists(hasText(RECORD_UNDO_ACTION))
        composeRule.onNodeWithText(RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        robot.waitUntilGone(robot.recordRow(dateText = TODAY_TEXT, amountText = "250ml"))
        robot.waitUntilExists(hasText(RECORD_EMPTY_TITLE))
        environment.storedRecordList shouldHaveSize 0
    }

    @Test
    fun `TC-ba58aaf9-092a-4ade-96ef-ff61ccc1672a 보기를 바꿔도 루틴 추가에 적어 둔 내용이 그대로 남는다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE)))
        robot.titleInput().performTextInput("운동")
        robot.goalAmountInput().performTextInput("3")
        composeRule.waitForIdle()

        robot.showCalendar()
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe "운동"
        robot.goalAmountInput().routineListDetailEditableText() shouldBe "3"

        robot.showList()
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe "운동"
        robot.goalAmountInput().routineListDetailEditableText() shouldBe "3"
    }

    @Test
    fun `TC-ba58aaf9-092a-4ade-96ef-ff61ccc1672a 보기를 바꿔도 루틴 상세와 고치던 제목, 상세 선택이 그대로 남는다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)
        robot.titleInput().performTextReplacement(EDITED_TITLE)
        composeRule.waitForIdle()

        robot.showCalendar()
        robot.assertDetailOnDetailPane(title = EDITED_TITLE)

        robot.showList()
        robot.assertDetailOnDetailPane(title = EDITED_TITLE)
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
    }

    @Test
    fun `TC-c3abb960-7a75-43b3-9dea-f17abd2daa62 루틴 추가가 놓여 있어도 필터를 쓸 수 있고 상세 영역과 제목은 그대로다`() {
        val (tag, tagged, untagged) = filterFixture()
        robot.setNavDisplay(routineList = listOf(tagged, untagged))
        robot.titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        robot.toggleFilterTag(tag)
        robot.waitUntilGone(robot.card(untagged.detail.title))
        composeRule.onNode(robot.card(tagged.detail.title)).assertExists()
        robot.toggleFilterTag(tag)
        robot.waitUntilCard(untagged)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineHomeFilterNavKey)
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe TYPED_ADD_TITLE
    }

    @Test
    fun `TC-c3abb960-7a75-43b3-9dea-f17abd2daa62 루틴 상세가 놓여 있어도 필터를 쓸 수 있고 상세 영역은 그대로다`() {
        val (tag, tagged, untagged) = filterFixture()
        robot.setNavDisplay(routineList = listOf(tagged, untagged))
        robot.selectRoutine(tagged)

        robot.toggleFilterTag(tag)
        robot.waitUntilGone(robot.card(untagged.detail.title))
        composeRule.onNode(robot.card(tagged.detail.title)).assertExists()
        robot.toggleFilterTag(tag)
        robot.waitUntilCard(untagged)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = tagged.id), RoutineHomeFilterNavKey)
        robot.assertDetailOnDetailPane(tagged)
    }

    @Test
    fun `TC-f34358bf-2ff2-40f0-8e18-cfaa15371025 상세의 루틴이 필터에 맞지 않아 목록에서 사라져도 상세가 남는다`() {
        val (tag, tagged, untagged) = filterFixture()
        robot.setNavDisplay(routineList = listOf(tagged, untagged))
        robot.selectRoutine(untagged)

        robot.toggleFilterTag(tag)

        robot.waitUntilGone(robot.card(untagged.detail.title))
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = untagged.id), RoutineHomeFilterNavKey)
        robot.assertDetailOnDetailPane(untagged)
    }

    @Test
    fun `TC-f34358bf-2ff2-40f0-8e18-cfaa15371025 상세의 루틴이 필터에 맞지 않아 달력에서 사라져도 상세가 남는다`() {
        val (tag, tagged, untagged) = filterFixture()
        robot.setNavDisplay(
            routineList = listOf(tagged, untagged),
            recordList =
                listOf(
                    routineListDetailRecord(routine = tagged, date = TODAY, amount = 1),
                    routineListDetailRecord(routine = untagged, date = TODAY, amount = 2),
                ),
        )
        robot.selectRoutine(untagged)
        robot.showCalendar()
        composeRule.onNode(robot.calendarRecord(title = untagged.detail.title, amountText = "2회")).assertExists()

        robot.toggleFilterTag(tag)

        robot.waitUntilGone(robot.calendarRecord(title = untagged.detail.title, amountText = "2회"))
        composeRule.onNode(robot.calendarRecord(title = tagged.detail.title, amountText = "1회")).assertExists()
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = untagged.id), RoutineHomeFilterNavKey)
        robot.assertDetailOnDetailPane(untagged)
    }

    // 주기 매일, 단위 ml, 목표량 2000인 진행 중인 루틴에 오늘 기록 1000과 500을 두고 그 상세를 상세 영역에 놓는다.
    private fun setWaterRoutineOnDetail(): Routine {
        val routine = routine(title = WATER_TITLE, unit = "ml", goalAmount = 2000, amountPerRecord = 250)
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList =
                listOf(
                    routineListDetailRecord(routine = routine, date = TODAY, amount = 1000),
                    routineListDetailRecord(routine = routine, date = TODAY, amount = 500),
                ),
        )
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.waitUntilExists(robot.recordRow(dateText = TODAY_TEXT, amountText = AMOUNT_500_ML_TEXT))

        return routine
    }

    private fun filterFixture(): Triple<Tag, Routine, Routine> {
        val tag = Arb.tag(title = ROUTINE_LIST_DETAIL_TAG_TITLE).next()
        val tagged = routine(title = FIRST_TITLE)
        val untagged = routine(title = SECOND_TITLE)
        environment.tagList = listOf(tag)
        environment.routineTagMap = mapOf(tagged.id to setOf(tag.id))
        return Triple(tag, tagged, untagged)
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val TODAY_TEXT = "Jul 15, 2026"
        const val FIRST_TITLE = "A-list-routine"
        const val SECOND_TITLE = "B-list-routine"
        const val WATER_TITLE = "물 마시기"
        const val RENAMED_WATER_TITLE = "수분 보충"
        const val ADDED_TITLE = "added-title"
        const val TYPED_ADD_TITLE = "typed-add-title"
        const val EDITED_TITLE = "edited-title"

        fun routine(
            title: String,
            unit: String = "회",
            goalAmount: Int? = null,
            amountPerRecord: Int = 1,
        ): Routine = routineListDetailRoutine(title = title, today = TODAY, unit = unit, goalAmount = goalAmount, amountPerRecord = amountPerRecord)
    }
}

private const val AMOUNT_1500_ML_TEXT = "1500ml"

private const val AMOUNT_500_ML_TEXT = "500ml"
