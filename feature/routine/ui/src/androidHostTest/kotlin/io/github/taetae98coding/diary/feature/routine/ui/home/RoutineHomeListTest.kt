package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
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

// 목록 보기의 카드는 제품 RoutineHomeScreen과 RoutineHomeViewModel로 표시하고, 저장된 루틴과 주기 합계는 UseCase mock이 돌려준다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeListTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(TODAY)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `TC-2929c198-238d-48ae-b5ea-00368b8c2691 계정에 저장된 루틴을 제목과 카드 문구로 표시한다`() {
        setRoutineHome(
            routineHomeQuickRecordSummary(title = WATER_ROUTINE_TITLE, startDate = START_DATE, cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = 2_000, cycleAmount = 1_500L),
            routineHomeQuickRecordSummary(title = "운동", startDate = START_DATE, cycle = RoutineCycle.WEEKLY, unit = "회", goalAmount = 3, cycleAmount = 2L),
            routineHomeQuickRecordSummary(title = "달리기", startDate = START_DATE, cycle = RoutineCycle.MONTHLY, unit = "m", cycleAmount = 12_000L),
            routineHomeQuickRecordSummary(title = "독서", startDate = START_DATE, cycle = RoutineCycle.DAILY, unit = "회"),
        )

        composeRule.routineHomeQuickRecordCardTextList(WATER_ROUTINE_TITLE) shouldBe listOf(WATER_ROUTINE_TITLE, "오늘 1500 / 2000ml", "75%")
        composeRule.routineHomeQuickRecordCardTextList("운동") shouldBe listOf("운동", "이번 주 2 / 3회", "66%")
        composeRule.routineHomeQuickRecordCardTextList("달리기") shouldBe listOf("달리기", "이번 달 12000m")
        composeRule.routineHomeQuickRecordCardTextList("독서") shouldBe listOf("독서", "오늘 0회")
    }

    @Test
    fun `TC-7b58f05e-7964-4d3f-86ce-9ce1f7555211 루틴 카드에는 제목과 카드 문구, 빠른 기록 외의 내용을 표시하지 않는다`() {
        setRoutineHome(
            routineHomeQuickRecordSummary(
                title = WATER_TITLE,
                startDate = START_DATE,
                unit = "ml",
                amountPerRecord = 250,
            ) { detail -> detail.copy(description = "하루 여덟 잔") },
        )

        composeRule.routineHomeQuickRecordCardTextList(WATER_TITLE) shouldBe listOf(WATER_TITLE, "오늘 0ml")
        composeRule.onNode(routineHomeQuickRecordButton(WATER_TITLE)).assertExists()
    }

    @Test
    fun `TC-9383140e-a812-4260-96fd-b553d7b3bf60 진행 중인 루틴의 카드에만 빠른 기록을 제공한다`() {
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(
            routineHomeQuickRecordSummary(title = IN_PROGRESS_TITLE, startDate = START_DATE),
            routineHomeQuickRecordSummary(title = TODAY_START_TITLE, startDate = TODAY),
            routineHomeQuickRecordSummary(title = NOT_STARTED_TITLE, startDate = TOMORROW),
        )

        composeRule.onNode(routineHomeQuickRecordButton(IN_PROGRESS_TITLE)).assertExists()
        composeRule.onNode(routineHomeQuickRecordButton(TODAY_START_TITLE)).assertExists()
        composeRule.onNode(routineHomeQuickRecordButton(NOT_STARTED_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-2357de08-5442-4b44-8b3d-3cade4e361a2 보기 범위가 모든 루틴이면 시작 전인 루틴도 목록에 카드 문구와 함께 남는다`() {
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(
            routineHomeQuickRecordSummary(title = NOT_STARTED_TITLE, startDate = TOMORROW, goalAmount = 2_000),
        )

        composeRule.routineHomeQuickRecordCardTextList(NOT_STARTED_TITLE) shouldBe listOf(NOT_STARTED_TITLE, "시작 전")
    }

    @Test
    fun `TC-bf4a3569-a8c2-4aeb-95d1-c2b958a9b7c4 루틴을 추가하면 목록에 나타난다`() {
        environment.titleOrderByToday = mapOf(TODAY to listOf(FIRST_TITLE, SECOND_TITLE, THIRD_TITLE))
        setRoutineHome(
            routineHomeQuickRecordSummary(title = FIRST_TITLE, startDate = START_DATE, unit = "회"),
            routineHomeQuickRecordSummary(title = THIRD_TITLE, startDate = START_DATE, unit = "회"),
        )

        composeRule.runOnIdle {
            environment.addSummary(routineHomeQuickRecordSummary(title = SECOND_TITLE, startDate = START_DATE, unit = "회", cycleAmount = 2L))
        }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(SECOND_TITLE, "오늘 2회"))

        cardTitleList() shouldBe listOf(FIRST_TITLE, SECOND_TITLE, THIRD_TITLE)
    }

    @Test
    fun `TC-917f0e47-5ab0-4af8-b720-1238223135e9 목록의 루틴을 선택하면 그 루틴의 RoutineDetail 화면으로 이동한다`() {
        val target = routineHomeQuickRecordSummary(title = SECOND_TITLE, startDate = START_DATE)
        setRoutineHome(routineHomeQuickRecordSummary(title = FIRST_TITLE, startDate = START_DATE), target)

        composeRule.onNode(routineHomeQuickRecordCard(SECOND_TITLE)).performClick()
        composeRule.waitForIdle()

        environment.detailNavigationList shouldBe listOf(target.routine.id)
    }

    @Test
    fun `TC-a8d83f9c-fb51-4ce1-8cfa-8e6448ca1961 빠른 기록을 눌러도 상세로 이동하지 않는다`() {
        setRoutineHome(routineHomeQuickRecordSummary(title = WATER_TITLE, startDate = START_DATE))

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        environment.detailNavigationList.shouldBeEmpty()
    }

    @Test
    fun `TC-a8d83f9c-fb51-4ce1-8cfa-8e6448ca1961 빠른 기록을 길게 눌러도 상세로 이동하지 않는다`() {
        setRoutineHome(routineHomeQuickRecordSummary(title = WATER_TITLE, startDate = START_DATE))

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.onNode(routineAmountDialogField()).assertExists()
        environment.detailNavigationList.shouldBeEmpty()
    }

    // 아직 준비되지 않은 자리는 목록이 루틴 없이 놓는 카드이므로, 목록이 쓰는 카드를 루틴 없이 놓아 선택한다.
    @Test
    fun `TC-e0b883bd-d3aa-453f-ba8f-f2524cc2f3d4 아직 준비되지 않은 자리는 선택해도 이동하지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        composeRule.setContent {
            DiaryTheme {
                SwipeRoutineCard(
                    onEvent = { event -> eventList += event },
                    summary = null,
                )
            }
        }

        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG)).performClick()
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

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

    private fun setRoutineHome(vararg summary: RoutineSummary) {
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        summary.forEach { value -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(value.routine.detail.title)) }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val TOMORROW: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val WATER_TITLE = WATER_ROUTINE_TITLE
        const val IN_PROGRESS_TITLE = "진행 중 루틴"
        const val TODAY_START_TITLE = "오늘 시작 루틴"
        const val NOT_STARTED_TITLE = "시작 전 루틴"
        const val FIRST_TITLE = "가 루틴"
        const val SECOND_TITLE = "나 루틴"
        const val THIRD_TITLE = "다 루틴"
    }
}

private const val WATER_ROUTINE_TITLE = "물 마시기"
