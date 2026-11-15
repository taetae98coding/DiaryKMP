package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
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

// 화면이 고른 보기 범위와 오늘로 목록을 조회하고 받은 목록을 그대로 보이는지를 제품 화면과 RoutineHomeViewModel로 확인한다.
// 보기 범위와 오늘마다 조회가 돌려줄 목록은 테스트가 정해 둔다. 저장된 루틴을 좁히는 조회 자체는 RoutineHomeViewScopeDaoTest와 RoutineHomeViewScopeUseCaseTest가 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeViewScopeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(WEDNESDAY)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `TC-049a798b-fd5a-4713-bbe2-899bef51c34e 처음 상태에서는 완료되지 않은 루틴 가운데 오늘 할 루틴만 목록에 보인다`() {
        setRoutineHome(
            summary(title = "가", cycle = RoutineCycle.DAILY),
            summary(title = "나", cycle = RoutineCycle.WEEKDAYS),
            summary(title = "다", cycle = RoutineCycle.WEEKENDS),
            summary(title = "라", cycle = RoutineCycle.WEEKLY),
            summary(title = "마", cycle = RoutineCycle.MONTHLY, startDate = WEDNESDAY),
            summary(title = "바", cycle = RoutineCycle.DAILY, startDate = THURSDAY),
            summary(title = "사", cycle = RoutineCycle.WEEKDAYS, startDate = THURSDAY),
            summary(title = "아", cycle = RoutineCycle.DAILY, isFinished = true),
            page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(GA, NA, "라", "마"))),
        )

        environment.viewScope.value shouldBe RoutineViewScope.TODAY
        environment.pageTodayList.distinct() shouldBe listOf(WEDNESDAY)
        cardTitleList() shouldBe listOf(GA, NA, "라", "마")
    }

    @Test
    fun `TC-a747891d-2da5-46e2-8422-93afd70d71b9 평일만 루틴의 카드는 매일 루틴과 같은 문구와 빠른 기록을 보여 준다`() {
        setRoutineHomeQuickRecordToday(FRIDAY)
        // 7월 10일의 기록은 날짜가 바뀌면 이번 주기에 들지 않는다.
        environment.cycleAmountByToday = mapOf(MONDAY to mapOf(READING_TITLE to 0L))
        setRoutineHome(summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS, unit = "쪽", goalAmount = 20, cycleAmount = 15L))

        composeRule.onNode(routineHomeQuickRecordCard(READING_TITLE, "오늘 15 / 20쪽", "75%")).assertExists()
        composeRule.onNode(routineHomeQuickRecordButton(READING_TITLE)).assertExists()

        returnFromBackground(today = MONDAY)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(READING_TITLE, TODAY_0_OF_20_PAGE_TEXT, "0%"))
        composeRule.onNode(routineHomeQuickRecordButton(READING_TITLE)).assertExists()
    }

    @Test
    fun `TC-a747891d-2da5-46e2-8422-93afd70d71b9 주말만 루틴의 카드는 매일 루틴과 같은 문구와 빠른 기록을 보여 준다`() {
        setRoutineHomeQuickRecordToday(SATURDAY)
        environment.cycleAmountByToday = mapOf(SUNDAY to mapOf(HIKING_TITLE to 0L))
        setRoutineHome(summary(title = HIKING_TITLE, cycle = RoutineCycle.WEEKENDS, unit = "회", cycleAmount = 1L))

        composeRule.routineHomeQuickRecordCardTextList(HIKING_TITLE) shouldBe listOf(HIKING_TITLE, "오늘 1회")
        composeRule.onNode(routineHomeQuickRecordButton(HIKING_TITLE)).assertExists()

        returnFromBackground(today = SUNDAY)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(HIKING_TITLE, "오늘 0회"))
        composeRule.onNode(routineHomeQuickRecordButton(HIKING_TITLE)).assertExists()
    }

    @Test
    fun `TC-92d3f5ec-33b9-44b6-8e1b-9bbc36e9cd4a 모든 루틴이면 하는 요일이 아닌 루틴도 같은 카드 문구와 빠른 기록으로 보인다`() {
        setRoutineHomeQuickRecordToday(SATURDAY)
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(
            summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS, unit = "쪽", goalAmount = 20),
            summary(title = HIKING_TITLE, cycle = RoutineCycle.WEEKENDS, unit = "쪽", goalAmount = 20),
        )

        // 토요일과 일요일에는 평일만 루틴이, 수요일에는 주말만 루틴이 하는 요일이 아니다.
        listOf(SATURDAY, SUNDAY, WEDNESDAY).forEachIndexed { index, today ->
            if (index > 0) returnFromBackground(today = today)

            listOf(READING_TITLE, HIKING_TITLE).forEach { title ->
                composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(title))
                composeRule.routineHomeQuickRecordCardTextList(title) shouldBe listOf(title, TODAY_0_OF_20_PAGE_TEXT, "0%")
                composeRule.onNode(routineHomeQuickRecordButton(title)).assertExists()
            }
        }
    }

    @Test
    fun `TC-abf5e756-2eba-4d3c-91bb-bf5ff87dac3f 하는 요일이 아닌 날에도 빠른 기록으로 오늘의 기록을 넣는다`() {
        val reading = setSaturdayWeekdaysReading()

        composeRule.clickRoutineHomeQuickRecord(READING_TITLE)

        environment.savedRecordList.map { record -> Triple(record.routineId, record.date, record.amount) } shouldBe listOf(Triple(reading.routine.id, SATURDAY, 5))
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(READING_TITLE, "오늘 5 / 20쪽", "25%"))
    }

    @Test
    fun `TC-abf5e756-2eba-4d3c-91bb-bf5ff87dac3f 하는 요일이 아닌 날에도 양을 입력해 오늘의 기록을 넣는다`() {
        val reading = setSaturdayWeekdaysReading()

        composeRule.longPressRoutineHomeQuickRecord(READING_TITLE)
        composeRule.onNode(routineAmountDialogField()).performTextReplacement("10")
        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()

        environment.savedRecordList.map { record -> Triple(record.routineId, record.date, record.amount) } shouldBe listOf(Triple(reading.routine.id, SATURDAY, 10))
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(READING_TITLE, "오늘 10 / 20쪽", "50%"))
    }

    private fun setSaturdayWeekdaysReading(): RoutineSummary {
        val reading = summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS, unit = "쪽", goalAmount = 20, amountPerRecord = 5)
        setRoutineHomeQuickRecordToday(SATURDAY)
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(reading)
        composeRule.onNode(routineHomeQuickRecordCard(READING_TITLE, TODAY_0_OF_20_PAGE_TEXT, "0%")).assertExists()

        return reading
    }

    @Test
    fun `TC-9b897c89-a9df-46f1-ac07-e2c8a54dc8b1 단위가 비어 있고 목표량이 있는 루틴은 카드 문구와 기록 안내의 양을 숫자만으로 보여 준다`() {
        setRoutineHome(summary(title = EXERCISE_TITLE, unit = "", goalAmount = 3, cycleAmount = 2L))
        composeRule.routineHomeQuickRecordCardTextList(EXERCISE_TITLE) shouldBe listOf(EXERCISE_TITLE, "오늘 2 / 3", "66%")

        composeRule.clickRoutineHomeQuickRecord(EXERCISE_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(EXERCISE_TITLE, "오늘 3 / 3", "100%"))
        composeRule.routineHomeQuickRecordCardTextList(EXERCISE_TITLE) shouldBe listOf(EXERCISE_TITLE, "오늘 3 / 3", "100%")
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EXERCISE_RECORDED_TEXT))
    }

    @Test
    fun `TC-9b897c89-a9df-46f1-ac07-e2c8a54dc8b1 단위가 비어 있고 목표량이 없는 루틴은 카드 문구와 기록 안내의 양을 숫자만으로 보여 준다`() {
        setRoutineHome(summary(title = EXERCISE_TITLE, unit = ""))
        composeRule.routineHomeQuickRecordCardTextList(EXERCISE_TITLE) shouldBe listOf(EXERCISE_TITLE, "오늘 0")

        composeRule.clickRoutineHomeQuickRecord(EXERCISE_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(EXERCISE_TITLE, "오늘 1"))
        composeRule.routineHomeQuickRecordCardTextList(EXERCISE_TITLE) shouldBe listOf(EXERCISE_TITLE, "오늘 1")
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EXERCISE_RECORDED_TEXT))
    }

    @Test
    fun `TC-be0a277f-164c-45df-b4b8-7d7ebec4217e 빠른 기록과 그 실행 취소는 목록 노출을 바꾸지 않고 오늘 목표를 채운 루틴도 남는다`() {
        setRoutineHome(summary(title = WATER_TITLE, unit = "ml", goalAmount = 2_000, amountPerRecord = 250, cycleAmount = 1_750L))
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1750 / 2000ml", "87%")).assertExists()

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 2000 / 2000ml", "100%"))
        composeRule.onNode(routineHomeQuickRecordButton(WATER_TITLE)).assertExists()

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).performClick()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1750 / 2000ml", "87%"))
        composeRule.onNode(routineHomeQuickRecordButton(WATER_TITLE)).assertExists()
    }

    @Test
    fun `TC-07fef83b-4cac-4e3a-8c90-ed97fcd1cf84 모든 루틴으로 바꾸면 완료되지 않은 루틴이 모두 보이고 오늘 할 루틴으로 되돌리면 다시 좁혀진다`() {
        setRoutineHome(
            summary(title = "가"),
            summary(title = "나", cycle = RoutineCycle.WEEKENDS),
            summary(title = "다", startDate = THURSDAY),
            summary(title = "라", isFinished = true),
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(GA)),
                    Page(RoutineViewScope.ALL, WEDNESDAY, listOf(GA, NA, DA)),
                ),
        )
        cardTitleList() shouldBe listOf("가")

        setViewScope(RoutineViewScope.ALL)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard("다"))
        cardTitleList() shouldBe listOf("가", "나", "다")

        setViewScope(RoutineViewScope.TODAY)

        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard("다"))
        cardTitleList() shouldBe listOf("가")
    }

    @Test
    fun `TC-181573d3-b753-4a1f-b3d6-5e1962cf1c46 보기 범위는 목록 순서를 바꾸지 않는다`() {
        setRoutineHomeQuickRecordToday(SATURDAY)
        setRoutineHome(
            summary(title = "마", startDate = SUNDAY),
            summary(title = "다"),
            summary(title = "나", cycle = RoutineCycle.WEEKDAYS),
            summary(title = "라", cycle = RoutineCycle.WEEKENDS, startDate = LocalDate(2026, 7, 18)),
            summary(title = "가", cycle = RoutineCycle.WEEKENDS),
            // 목록 순서는 조회가 정하고, 화면은 받은 순서를 바꾸지 않는다.
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, SATURDAY, listOf(GA, DA)),
                    Page(RoutineViewScope.ALL, SATURDAY, listOf(GA, NA, DA, "라", "마")),
                ),
        )
        cardTitleList() shouldBe listOf("가", "다")

        setViewScope(RoutineViewScope.ALL)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard("마"))
        cardTitleList() shouldBe listOf("가", "나", "다", "라", "마")
    }

    @Test
    fun `TC-24ebfe5d-b3a9-48c4-9f6e-c1d6b3d152d9 완료되지 않은 루틴은 있지만 오늘 할 루틴이 없으면 오늘 할 루틴이 없음을 알린다`() {
        setRoutineHome(summary(title = "가", startDate = THURSDAY), page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, emptyList())))

        listOf(
            listOf(summary(title = "가", startDate = THURSDAY)),
            listOf(summary(title = "가", cycle = RoutineCycle.WEEKENDS)),
            listOf(summary(title = "가", startDate = THURSDAY), summary(title = "나", isFinished = true)),
        ).forEach { summaryList ->
            composeRule.runOnIdle { environment.setSummaryList(summaryList) }
            composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))
            composeRule.waitForIdle()

            composeRule.onNodeWithText(TODAY_EMPTY_DESCRIPTION).assertExists()
            composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertDoesNotExist()
            composeRule.onNodeWithText(EMPTY_TITLE).assertDoesNotExist()
            composeRule.onNodeWithText(FILTERED_EMPTY_TITLE).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-7ba08dd3-fee8-4da0-bdb5-511ea813b1e7 오늘 할 루틴이 없는 빈 상태에서 모든 루틴으로 바꾸면 목록이 나타나고 되돌리면 다시 빈 상태가 된다`() {
        setRoutineHome(
            summary(title = "가", startDate = THURSDAY),
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, WEDNESDAY, emptyList()),
                    Page(RoutineViewScope.ALL, WEDNESDAY, listOf(GA)),
                ),
        )
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))

        setViewScope(RoutineViewScope.ALL)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard("가", NOT_STARTED_TEXT))
        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(hasText(TODAY_EMPTY_TITLE))
        composeRule.onNode(hasTestTag(DIARY_EMPTY_BOX_TEST_TAG)).assertDoesNotExist()

        setViewScope(RoutineViewScope.TODAY)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))
        composeRule.onNode(routineHomeQuickRecordCard("가")).assertDoesNotExist()
    }

    @Test
    fun `TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 선택한 태그가 없으면 완료되지 않은 루틴이 있는지로 빈 상태 안내를 가른다`() {
        setRoutineHome(page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, emptyList())))

        listOf(
            Triple(RoutineViewScope.TODAY, emptyList(), EMPTY_TITLE),
            Triple(RoutineViewScope.ALL, emptyList(), EMPTY_TITLE),
            Triple(RoutineViewScope.TODAY, listOf(summary(title = "가", isFinished = true)), EMPTY_TITLE),
            Triple(RoutineViewScope.ALL, listOf(summary(title = "가", isFinished = true)), EMPTY_TITLE),
            Triple(RoutineViewScope.TODAY, listOf(summary(title = "가", startDate = THURSDAY)), TODAY_EMPTY_TITLE),
        ).forEach { (viewScope, summaryList, expectedTitle) ->
            composeRule.runOnIdle {
                environment.viewScope.value = viewScope
                environment.setSummaryList(summaryList)
            }
            composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(expectedTitle))
            composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
            composeRule.waitForIdle()

            displayedEmptyTitleList() shouldBe listOf(expectedTitle)
        }
    }

    @Test
    fun `TC-7bd552ae-99c3-4c5e-9d48-41a4130a65ab 태그를 선택했으면 보기 범위와 관계없이 조건에 맞는 루틴이 없음을 알린다`() {
        environment.filterTagList = listOf(Arb.tag().next().copy(isFinished = false, isDeleted = false))
        setRoutineHome()

        // 태그 A와 연결되지 않은 루틴, 태그 A와 연결되었지만 시작 전인 루틴은 좁힌 목록에 놓이지 않는다.
        listOf(RoutineViewScope.TODAY, RoutineViewScope.ALL).forEach { viewScope ->
            composeRule.runOnIdle { environment.viewScope.value = viewScope }
            composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FILTERED_EMPTY_TITLE))
            composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
            composeRule.waitForIdle()

            displayedEmptyTitleList() shouldBe listOf(FILTERED_EMPTY_TITLE)
        }
    }

    @Test
    fun `TC-68d686f2-29c1-4d2d-aefe-55fd3e84df8f 저장된 루틴이 바뀌어 완료되지 않은 루틴의 유무가 달라지면 빈 상태 안내도 바뀐다`() {
        val notStarted = summary(title = "가", startDate = THURSDAY)
        setRoutineHome(page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, emptyList())))
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))

        // 시작 전인 루틴이 추가되거나 내려받아진다.
        composeRule.runOnIdle { environment.addSummary(notStarted) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))

        // 상세 화면에서 그 루틴을 완료한다.
        composeRule.runOnIdle { environment.setFinished(id = notStarted.routine.id, isFinished = true) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))

        composeRule.runOnIdle { environment.setFinished(id = notStarted.routine.id, isFinished = false) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))

        // 상세 화면에서 그 루틴을 삭제한다.
        composeRule.runOnIdle { environment.removeSummary(id = notStarted.routine.id) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))
    }

    @Test
    fun `TC-d1a7a776-6f63-4832-8e3d-6e7cc0bf14f9 오늘 할 루틴이 아닌 루틴을 추가하면 목록에 나타나지 않고 모든 루틴으로 바꾸면 나타난다`() {
        val addedList =
            listOf(
                summary(title = "추가 가", cycle = RoutineCycle.DAILY, startDate = THURSDAY),
                summary(title = "추가 나", cycle = RoutineCycle.WEEKENDS, startDate = WEDNESDAY),
                summary(title = "추가 다", cycle = RoutineCycle.WEEKDAYS, startDate = LocalDate(2026, 7, 20)),
            )
        setRoutineHome(
            summary(title = WATER_TITLE),
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(WATER_TITLE)),
                    Page(RoutineViewScope.ALL, WEDNESDAY, listOf(WATER_TITLE) + addedList.map { added -> added.routine.detail.title }),
                ),
        )

        addedList.forEach { added ->
            composeRule.runOnIdle { environment.addSummary(added) }
            composeRule.waitForIdle()
        }
        composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
        composeRule.waitForIdle()
        cardTitleList() shouldBe listOf(WATER_TITLE)

        setViewScope(RoutineViewScope.ALL)

        addedList.forEach { added -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(added.routine.detail.title)) }
    }

    @Test
    fun `TC-0455ffe0-672a-44a8-9fc1-71ff198a876d 시작 날짜나 주기가 바뀌어 조회 결과가 달라지면 다시 고르지 않아도 목록에서 사라지거나 나타난다`() {
        val na = summary(title = "나", unit = "ml", goalAmount = 2_000, cycleAmount = 1_500L)
        val da = summary(title = "다", startDate = THURSDAY)
        val ra = summary(title = "라", cycle = RoutineCycle.WEEKENDS)
        setRoutineHome(summary(title = "가"), na, da, ra, page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(GA, NA))))
        cardTitleList() shouldBe listOf(GA, NA)

        // 저장이 바뀌면 열어 둔 조회가 바뀐 결과를 이어서 전달한다.
        updateDetail(na, pageTitleList = listOf(GA)) { detail -> detail.copy(cycle = RoutineCycle.WEEKENDS) }
        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard(NA))
        cardTitleList() shouldBe listOf(GA)

        updateDetail(na, pageTitleList = listOf(GA, NA)) { detail -> detail.copy(cycle = RoutineCycle.WEEKDAYS) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(NA, "오늘 1500 / 2000ml", "75%"))
        cardTitleList() shouldBe listOf(GA, NA)

        updateDetail(na, pageTitleList = listOf(GA)) { detail -> detail.copy(startDate = THURSDAY) }
        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard(NA))

        updateDetail(da, pageTitleList = listOf(GA, DA)) { detail -> detail.copy(startDate = WEDNESDAY) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(DA))

        updateDetail(ra, pageTitleList = listOf(GA, DA, "라")) { detail -> detail.copy(cycle = RoutineCycle.DAILY) }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard("라"))
        cardTitleList() shouldBe listOf(GA, DA, "라")
    }

    @Test
    fun `TC-f3044014-279c-4acc-881a-4ce519c30e33 다시 시작한 루틴은 조회 결과에 들 때만 목록에 나타난다`() {
        val weekends = summary(title = RESTARTED_WEEKENDS_TITLE, cycle = RoutineCycle.WEEKENDS, isFinished = true)
        val notStarted = summary(title = RESTARTED_NOT_STARTED_TITLE, startDate = THURSDAY, isFinished = true)
        val weekdays = summary(title = RESTARTED_WEEKDAYS_TITLE, cycle = RoutineCycle.WEEKDAYS, isFinished = true)
        setRoutineHome(
            summary(title = "가"),
            weekends,
            notStarted,
            weekdays,
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(GA, RESTARTED_WEEKDAYS_TITLE)),
                    Page(RoutineViewScope.ALL, WEDNESDAY, listOf(GA, RESTARTED_WEEKENDS_TITLE, RESTARTED_WEEKDAYS_TITLE, RESTARTED_NOT_STARTED_TITLE)),
                ),
        )
        cardTitleList() shouldBe listOf(GA)

        // 완료된 루틴 목록에서 다시 시작한 것이 저장된다.
        listOf(weekends, notStarted, weekdays).forEach { finished ->
            composeRule.runOnIdle { environment.setFinished(id = finished.routine.id, isFinished = false) }
            composeRule.waitForIdle()
        }

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(RESTARTED_WEEKDAYS_TITLE))
        cardTitleList() shouldBe listOf(GA, RESTARTED_WEEKDAYS_TITLE)

        setViewScope(RoutineViewScope.ALL)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(RESTARTED_NOT_STARTED_TITLE))
        cardTitleList() shouldBe listOf(GA, RESTARTED_WEEKENDS_TITLE, RESTARTED_WEEKDAYS_TITLE, RESTARTED_NOT_STARTED_TITLE)
    }

    @Test
    fun `TC-2d2a748a-d6d0-4eeb-bf4f-c58412b9395c 완료를 실행 취소해 되돌아온 루틴도 주기가 주말만으로 바뀌었으면 목록에 나타나지 않는다`() {
        assertUndoneRoutineHidden(swipe = { swipeRight() }) { detail -> detail.copy(cycle = RoutineCycle.WEEKENDS) }
    }

    @Test
    fun `TC-2d2a748a-d6d0-4eeb-bf4f-c58412b9395c 완료를 실행 취소해 되돌아온 루틴도 시작 날짜가 뒤로 바뀌었으면 목록에 나타나지 않는다`() {
        assertUndoneRoutineHidden(swipe = { swipeRight() }) { detail -> detail.copy(startDate = THURSDAY) }
    }

    @Test
    fun `TC-2d2a748a-d6d0-4eeb-bf4f-c58412b9395c 삭제를 실행 취소해 되돌아온 루틴도 주기가 주말만으로 바뀌었으면 목록에 나타나지 않는다`() {
        assertUndoneRoutineHidden(swipe = { swipeLeft() }) { detail -> detail.copy(cycle = RoutineCycle.WEEKENDS) }
    }

    @Test
    fun `TC-2d2a748a-d6d0-4eeb-bf4f-c58412b9395c 삭제를 실행 취소해 되돌아온 루틴도 시작 날짜가 뒤로 바뀌었으면 목록에 나타나지 않는다`() {
        assertUndoneRoutineHidden(swipe = { swipeLeft() }) { detail -> detail.copy(startDate = THURSDAY) }
    }

    private fun assertUndoneRoutineHidden(
        swipe: TouchInjectionScope.() -> Unit,
        change: (RoutineDetail) -> RoutineDetail,
    ) {
        val target = summary(title = "가")
        setRoutineHome(target, summary(title = "나"), page = listOf(Page(RoutineViewScope.TODAY, WEDNESDAY, listOf(GA, NA))))

        composeRule.onNode(routineHomeQuickRecordCard("가")).performTouchInput { swipe() }
        composeRule.waitForIdle()
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION))
        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard("가"))
        // 화면을 벗어나지 않은 채 다른 경로에서 그 루틴이 바뀌어 저장되어, 오늘 할 루틴 조회는 그 루틴을 돌려주지 않게 된다.
        updateDetail(target, pageTitleList = listOf(NA), transform = change)

        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(CROSSFADE_MILLIS)
        composeRule.waitForIdle()

        val restored = environment.storedSummaryList.single { stored -> stored.routine.id == target.routine.id }
        restored.routine.isFinished shouldBe false
        composeRule.onNode(routineHomeQuickRecordCard("가")).assertDoesNotExist()

        setViewScope(RoutineViewScope.ALL)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard("가"))
    }

    @Test
    fun `TC-ada941d6-d2f9-4eaa-b5a4-d313a4615474 금요일에서 토요일로 오늘을 다시 정하면 필터를 다시 고르지 않아도 목록이 바뀐다`() {
        assertListChangedByToday(firstToday = FRIDAY, firstList = listOf(READING_TITLE), returnedToday = SATURDAY, returnedList = listOf(HIKING_TITLE, YOGA_TITLE))
    }

    @Test
    fun `TC-ada941d6-d2f9-4eaa-b5a4-d313a4615474 일요일에서 월요일로 오늘을 다시 정하면 필터를 다시 고르지 않아도 목록이 바뀐다`() {
        assertListChangedByToday(firstToday = SUNDAY, firstList = listOf(HIKING_TITLE, YOGA_TITLE), returnedToday = MONDAY, returnedList = listOf(READING_TITLE, YOGA_TITLE))
    }

    private fun assertListChangedByToday(
        firstToday: LocalDate,
        firstList: List<String>,
        returnedToday: LocalDate,
        returnedList: List<String>,
    ) {
        setRoutineHomeQuickRecordToday(firstToday)
        setRoutineHome(
            summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS),
            summary(title = HIKING_TITLE, cycle = RoutineCycle.WEEKENDS),
            summary(title = YOGA_TITLE, cycle = RoutineCycle.DAILY, startDate = SATURDAY),
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, firstToday, firstList),
                    Page(RoutineViewScope.TODAY, returnedToday, returnedList),
                ),
        )
        cardTitleList() shouldBe firstList

        returnFromBackground(today = returnedToday)

        returnedList.forEach { title -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(title)) }
        (firstList - returnedList.toSet()).forEach { title -> composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard(title)) }
        cardTitleList() shouldBe returnedList
        environment.viewScope.value shouldBe RoutineViewScope.TODAY
        environment.pageTodayList.distinct() shouldBe listOf(firstToday, returnedToday)
    }

    @Test
    fun `TC-56b5d000-db96-4dd1-a053-d3c19305d889 화면을 보는 동안 자정이 지나 요일이 바뀌어도 목록에 놓인 루틴이 바뀌지 않는다`() {
        setRoutineHomeQuickRecordToday(FRIDAY)
        setRoutineHome(
            summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS),
            summary(title = HIKING_TITLE, cycle = RoutineCycle.WEEKENDS),
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, FRIDAY, listOf(READING_TITLE)),
                    Page(RoutineViewScope.TODAY, SATURDAY, listOf(HIKING_TITLE)),
                ),
        )
        cardTitleList() shouldBe listOf(READING_TITLE)

        composeRule.runOnIdle { setRoutineHomeQuickRecordToday(SATURDAY) }
        composeRule.mainClock.advanceTimeBy(ONE_MINUTE_MILLIS)
        composeRule.waitForIdle()

        cardTitleList() shouldBe listOf(READING_TITLE)
        environment.pageTodayList.distinct() shouldBe listOf(FRIDAY)
    }

    @Test
    fun `TC-2156c8fc-d92b-4722-9a33-ffd46f3520a0 양 입력 창을 연 뒤 요일이 바뀌어 목록에서 빠져도 확인하면 그 시점의 오늘로 기록한다`() {
        val reading = summary(title = READING_TITLE, cycle = RoutineCycle.WEEKDAYS, amountPerRecord = 5)
        setRoutineHomeQuickRecordToday(FRIDAY)
        setRoutineHome(
            reading,
            page =
                listOf(
                    Page(RoutineViewScope.TODAY, FRIDAY, listOf(READING_TITLE)),
                    Page(RoutineViewScope.TODAY, SATURDAY, emptyList()),
                ),
        )
        composeRule.longPressRoutineHomeQuickRecord(READING_TITLE)
        composeRule.routineAmountDialogAmount() shouldBe "5"

        returnFromBackground(today = SATURDAY)

        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(routineHomeQuickRecordCard(READING_TITLE))
        composeRule.routineAmountDialogAmount() shouldBe "5"

        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(isDialog()).fetchSemanticsNodes().size shouldBe 0
        environment.savedRecordList.map { record -> Triple(record.routineId, record.date, record.amount) } shouldBe listOf(Triple(reading.routine.id, SATURDAY, 5))
    }

    @Test
    fun `TC-0c810935-f3e9-406c-84c7-b3f039f54c93 보기 범위는 달력 보기를 좁히지 않는다`() {
        val notStarted = summary(title = "시작 전 루틴", startDate = LocalDate(2026, 7, 20), unit = "회")
        val weekends = summary(title = "주말만 루틴", cycle = RoutineCycle.WEEKENDS, unit = "회")
        val weekdays = summary(title = "평일만 루틴", cycle = RoutineCycle.WEEKDAYS, unit = "회")
        val daily = summary(title = "매일 루틴", unit = "회")
        val recordList =
            listOf(
                notStarted to 22,
                weekends to 11,
                weekends to 14,
                weekdays to 11,
                weekdays to 15,
                daily to 15,
            )
        environment.setCalendarRecordList(
            recordList.map { (summary, day) ->
                CalendarRoutineRecord(
                    routineId = summary.routine.id,
                    title = summary.routine.detail.title,
                    color = summary.routine.detail.color,
                    date = LocalDate(2026, 7, day),
                    amount = 1L,
                    unit = "회",
                )
            },
        )
        setRoutineHome(notStarted, weekends, weekdays, daily)
        switchViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)
        val expectedCountMap = recordList.groupingBy { (summary, _) -> summary.routine.detail.title }.eachCount()

        calendarRecordCountMap(expectedCountMap.keys) shouldBe expectedCountMap

        setViewScope(RoutineViewScope.ALL)

        composeRule.onNodeWithText(JULY_TITLE).assertExists()
        calendarRecordCountMap(expectedCountMap.keys) shouldBe expectedCountMap
    }

    // 필터에서 고른 보기 범위가 계정에 저장된 것과 같게 한다. 필터의 조작과 저장 요청은 필터 테스트와 RoutineHomeViewScopeFilterTest가 확인한다.
    private fun setViewScope(viewScope: RoutineViewScope) {
        composeRule.runOnIdle { environment.viewScope.value = viewScope }
        composeRule.waitForIdle()
    }

    // 다른 경로에서 루틴이 바뀌어 저장되고, 열어 둔 오늘 할 루틴 조회가 바뀐 결과를 돌려준다.
    private fun updateDetail(
        summary: RoutineSummary,
        pageTitleList: List<String>,
        transform: (RoutineDetail) -> RoutineDetail,
    ) {
        composeRule.runOnIdle {
            environment.updateDetail(id = summary.routine.id, transform = transform)
            environment.setPageTitleList(viewScope = RoutineViewScope.TODAY, today = WEDNESDAY, titleList = pageTitleList)
        }
        composeRule.waitForIdle()
    }

    // 앱이 백그라운드에 있는 사이 기기의 날짜가 바뀌고 같은 화면으로 돌아온다.
    private fun returnFromBackground(today: LocalDate) {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        setRoutineHomeQuickRecordToday(today)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private fun switchViewMode(description: String) {
        composeRule.onNodeWithContentDescription(description).performClick()
        composeRule.waitForIdle()
    }

    private fun displayedEmptyTitleList(): List<String> =
        listOf(EMPTY_TITLE, TODAY_EMPTY_TITLE, FILTERED_EMPTY_TITLE).filter { title ->
            composeRule.onAllNodes(hasText(title)).fetchSemanticsNodes().isNotEmpty()
        }

    // 달력에 놓인 루틴 기록 아이템을 루틴 제목마다 센다. 날짜마다 아이템이 하나씩 놓인다.
    private fun calendarRecordCountMap(titleSet: Set<String>): Map<String, Int> =
        titleSet.associateWith { title ->
            composeRule
                .onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription(title, substring = true))
                .fetchSemanticsNodes()
                .size
        }

    // 목록은 격자라 위에서 아래, 같은 줄에서는 시작에서 끝 순서로 읽은 카드의 제목이다.
    private fun cardTitleList(): List<String> =
        composeRule
            .onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .sortedWith(compareBy({ node -> node.boundsInRoot.top }, { node -> node.boundsInRoot.left }))
            .map { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .first()
                    .text
            }

    private fun setRoutineHome(
        vararg summary: RoutineSummary,
        page: List<Page> = emptyList(),
    ) {
        page.forEach { value -> environment.setPageTitleList(viewScope = value.viewScope, today = value.today, titleList = value.titleList) }
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
    }

    // 그 보기 범위와 오늘로 조회했을 때 돌려줄 루틴의 제목이다.
    private class Page(
        val viewScope: RoutineViewScope,
        val today: LocalDate,
        val titleList: List<String>,
    )

    // 따로 정하지 않으면 2026-07-01에 시작한 완료되지 않은 매일 루틴이다.
    private fun summary(
        title: String,
        cycle: RoutineCycle = RoutineCycle.DAILY,
        startDate: LocalDate = START_DATE,
        unit: String = "회",
        goalAmount: Int? = null,
        amountPerRecord: Int = 1,
        cycleAmount: Long = 0L,
        isFinished: Boolean = false,
    ): RoutineSummary =
        routineHomeQuickRecordSummary(
            title = title,
            startDate = startDate,
            cycle = cycle,
            unit = unit,
            goalAmount = goalAmount,
            amountPerRecord = amountPerRecord,
            cycleAmount = cycleAmount,
            isFinished = isFinished,
        )

    private companion object {
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        val FRIDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 10)
        val SATURDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 11)
        val SUNDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 12)
        val MONDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 13)
        val WEDNESDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val THURSDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        const val GA = "가"
        const val NA = "나"
        const val DA = "다"
        const val RESTARTED_WEEKENDS_TITLE = "나 주말만"
        const val RESTARTED_NOT_STARTED_TITLE = "나 시작 전"
        const val RESTARTED_WEEKDAYS_TITLE = "나 평일만"
        const val TODAY_0_OF_20_PAGE_TEXT = "오늘 0 / 20쪽"
        const val READING_TITLE = "독서"
        const val HIKING_TITLE = "등산"
        const val YOGA_TITLE = "요가"
        const val EXERCISE_TITLE = "운동"
        const val WATER_TITLE = "물 마시기"
        const val EXERCISE_RECORDED_TEXT = "운동 1 기록"
        const val NOT_STARTED_TEXT = "시작 전"
        const val EMPTY_TITLE = "아직 루틴이 없습니다"
        const val TODAY_EMPTY_TITLE = "오늘 할 루틴이 없습니다"
        const val TODAY_EMPTY_DESCRIPTION = "필터에서 모든 루틴을 볼 수 있습니다"
        const val FILTERED_EMPTY_TITLE = "조건에 맞는 루틴이 없습니다"
        const val JULY_TITLE = "2026년 7월"
        const val CROSSFADE_MILLIS = 1_000L
        const val ONE_MINUTE_MILLIS = 60_000L
    }
}
