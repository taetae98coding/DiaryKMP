package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 오늘은 기기의 날짜로 정해지므로 기기 시계만 테스트가 바꾸고, 화면과 RoutineHomeViewModel은 제품 코드를 쓴다.
// 목록 순서는 저장소가 오늘을 받아 정하므로, 오늘마다 조회되는 순서를 테스트 데이터로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeTodayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(BEFORE_MIDNIGHT)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `TC-115d314d-eb03-4b9a-85ed-643f736e6751 루틴 상세에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.onNode(routineHomeQuickRecordCard(IN_PROGRESS_TITLE)).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT).assertExists()
            setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT)
            pressBack()
        }
    }

    @Test
    fun `TC-115d314d-eb03-4b9a-85ed-643f736e6751 루틴 추가에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_ADD_CONTENT).assertExists()
            setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT)
            pressBack()
        }
    }

    @Test
    fun `TC-115d314d-eb03-4b9a-85ed-643f736e6751 앱이 백그라운드에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
            composeRule.waitForIdle()
            setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT)
            composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-115d314d-eb03-4b9a-85ed-643f736e6751 다른 주요 목적지에 다녀오면 오늘을 다시 정한다`() {
        assertTodayRefreshed {
            composeRule.runOnIdle { environment.backStack.remove(RoutineHomeNavKey) }
            composeRule.waitForIdle()
            composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_OTHER_CONTENT).assertExists()
            setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT)
            composeRule.runOnIdle { environment.backStack.add(RoutineHomeNavKey) }
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-21eae803-7dfa-42d6-adde-57de3d4294c7 화면을 보는 동안 자정이 지나도 카드 문구와 목록 순서는 바뀌지 않는다`() {
        setBoundaryRoutineHome()

        composeRule.runOnIdle { setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT) }
        composeRule.mainClock.advanceTimeBy(ONE_MINUTE_MILLIS)
        composeRule.waitForIdle()

        assertBeforeMidnightList()
        environment.pageTodayList.distinct() shouldBe listOf(BEFORE_MIDNIGHT)
    }

    @Test
    fun `TC-1bbf226d-16ea-44ee-923e-9112a9e8423d 보기를 바꾸는 것으로는 오늘을 다시 정하지 않는다`() {
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(routineHomeQuickRecordSummary(title = STARTING_TITLE, startDate = AFTER_MIDNIGHT, unit = "회"))
        composeRule.routineHomeQuickRecordCardTextList(STARTING_TITLE) shouldBe listOf(STARTING_TITLE, NOT_STARTED_TEXT)

        composeRule.runOnIdle { setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT) }
        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        primaryDateCount(day = BEFORE_MIDNIGHT.day) shouldBe 1
        primaryDateCount(day = AFTER_MIDNIGHT.day) shouldBe 0

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(STARTING_TITLE))
        composeRule.routineHomeQuickRecordCardTextList(STARTING_TITLE) shouldBe listOf(STARTING_TITLE, NOT_STARTED_TEXT)
        composeRule.onNode(routineHomeQuickRecordButton(STARTING_TITLE)).assertDoesNotExist()
        environment.pageTodayList.distinct() shouldBe listOf(BEFORE_MIDNIGHT)
    }

    @Test
    fun `TC-746d0a93-a6f3-43c7-b5f3-372fe38b0429 다른 화면에서 돌아와 오늘을 다시 정하면 달력의 오늘 강조도 바뀐다`() {
        setRoutineHome(inProgressSummary())
        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)
        composeRule.onNodeWithText(JULY_TITLE).assertExists()
        primaryDateCount(day = BEFORE_MIDNIGHT.day) shouldBe 1

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_ADD_CONTENT).assertExists()
        setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT)
        pressBack()

        composeRule.onNodeWithText(JULY_TITLE).assertExists()
        primaryDateCount(day = AFTER_MIDNIGHT.day) shouldBe 1
        primaryDateCount(day = BEFORE_MIDNIGHT.day) shouldBe 0
    }

    @Test
    fun `TC-285be36d-12e3-4d28-a6a1-36666b8716bd 화면을 보는 동안 자정이 지나도 빠른 기록의 날짜는 화면이 정해 둔 오늘이다`() {
        assertRecordedOnScreenToday {
            composeRule.clickRoutineHomeQuickRecord(ENDLESS_TITLE)
        }
    }

    @Test
    fun `TC-285be36d-12e3-4d28-a6a1-36666b8716bd 화면을 보는 동안 자정이 지나도 양을 입력한 기록의 날짜는 화면이 정해 둔 오늘이다`() {
        assertRecordedOnScreenToday {
            composeRule.longPressRoutineHomeQuickRecord(ENDLESS_TITLE)
            composeRule.routineAmountDialogAmount() shouldBe "1"
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
            composeRule.waitForIdle()
        }
    }

    private fun assertRecordedOnScreenToday(record: () -> Unit) {
        setRoutineHome(routineHomeQuickRecordSummary(title = ENDLESS_TITLE, startDate = START_DATE, unit = "회", amountPerRecord = 1))
        composeRule.routineHomeQuickRecordCardTextList(ENDLESS_TITLE) shouldBe listOf(ENDLESS_TITLE, TODAY_ZERO_TEXT)
        composeRule.runOnIdle { setRoutineHomeQuickRecordToday(AFTER_MIDNIGHT) }
        composeRule.mainClock.advanceTimeBy(ONE_MINUTE_MILLIS)
        composeRule.waitForIdle()

        record()

        environment.savedRecordList.map { saved -> saved.date } shouldBe listOf(BEFORE_MIDNIGHT)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ENDLESS_TITLE, "오늘 1회"))
    }

    private fun assertTodayRefreshed(leaveAndReturn: () -> Unit) {
        setBoundaryRoutineHome()

        leaveAndReturn()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(STARTING_TITLE, TODAY_ZERO_TEXT))
        cardTitleList() shouldBe listOf(STARTING_TITLE, IN_PROGRESS_TITLE)
        composeRule.routineHomeQuickRecordCardTextList(STARTING_TITLE) shouldBe listOf(STARTING_TITLE, TODAY_ZERO_TEXT)
        composeRule.onNode(routineHomeQuickRecordButton(STARTING_TITLE)).assertExists()
        composeRule.routineHomeQuickRecordCardTextList(IN_PROGRESS_TITLE) shouldBe listOf(IN_PROGRESS_TITLE, TODAY_ZERO_TEXT)
        composeRule.onNode(routineHomeQuickRecordButton(IN_PROGRESS_TITLE)).assertExists()
    }

    // 시작 전인 루틴을 목록에 올려 두므로 보기 범위는 `모든 루틴`이다.
    private fun setBoundaryRoutineHome() {
        environment.viewScope.value = RoutineViewScope.ALL
        environment.titleOrderByToday =
            mapOf(
                BEFORE_MIDNIGHT to listOf(IN_PROGRESS_TITLE, STARTING_TITLE),
                AFTER_MIDNIGHT to listOf(STARTING_TITLE, IN_PROGRESS_TITLE),
            )
        // 7월 15일에 넣은 기록은 날짜가 바뀌면 매일 루틴의 이번 주기에 들지 않는다.
        environment.cycleAmountByToday = mapOf(AFTER_MIDNIGHT to mapOf(IN_PROGRESS_TITLE to 0L))
        setRoutineHome(
            inProgressSummary(cycleAmount = 1L),
            routineHomeQuickRecordSummary(title = STARTING_TITLE, startDate = AFTER_MIDNIGHT, unit = "회"),
        )
        assertBeforeMidnightList()
    }

    private fun assertBeforeMidnightList() {
        cardTitleList() shouldBe listOf(IN_PROGRESS_TITLE, STARTING_TITLE)
        composeRule.routineHomeQuickRecordCardTextList(IN_PROGRESS_TITLE) shouldBe listOf(IN_PROGRESS_TITLE, TODAY_ONE_TEXT)
        composeRule.onNode(routineHomeQuickRecordButton(IN_PROGRESS_TITLE)).assertExists()
        composeRule.routineHomeQuickRecordCardTextList(STARTING_TITLE) shouldBe listOf(STARTING_TITLE, NOT_STARTED_TEXT)
        composeRule.onNode(routineHomeQuickRecordButton(STARTING_TITLE)).assertDoesNotExist()
    }

    private fun primaryDateCount(day: Int): Int = composeRule.routineHomeQuickRecordPrimaryDateCount(day = day, primaryColor = environment.onPrimaryColor)

    // 목록은 격자라 위에서 아래, 같은 줄에서는 시작에서 끝 순서로 읽은 카드의 제목이다.
    private fun cardTitleList(): List<String> =
        composeRule
            .onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .sortedWith(compareBy({ node -> node.boundsInRoot.top }, { node -> node.boundsInRoot.left }))
            .map { node ->
                node.config[SemanticsProperties.Text]
                    .first()
                    .text
            }

    private fun toggleViewMode(description: String) {
        composeRule.onNodeWithContentDescription(description).performClick()
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setRoutineHome(vararg summary: RoutineSummary) {
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        summary.forEach { value -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(value.routine.detail.title)) }
    }

    private fun inProgressSummary(cycleAmount: Long = 0L): RoutineSummary = routineHomeQuickRecordSummary(title = IN_PROGRESS_TITLE, startDate = START_DATE, unit = "회", cycleAmount = cycleAmount)

    private companion object {
        val BEFORE_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val AFTER_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val IN_PROGRESS_TITLE = "나"
        const val STARTING_TITLE = "가"
        const val ENDLESS_TITLE = "매일 루틴"
        const val TODAY_ZERO_TEXT = "오늘 0회"
        const val NOT_STARTED_TEXT = "시작 전"
        const val TODAY_ONE_TEXT = "오늘 1회"
        const val JULY_TITLE = "2026년 7월"
        const val ADD_DESCRIPTION = "루틴 추가"
        const val ONE_MINUTE_MILLIS = 60_000L
    }
}
