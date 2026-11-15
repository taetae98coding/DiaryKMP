@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.app.shared.di.DiaryKoinApplication
import io.github.taetae98coding.diary.app.shared.navigation.appEntryProvider
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.progress.range
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageEntityTagSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid

// 태그별 완료된 루틴 목록과 루틴 상세는 서로 다른 기능 모듈에 있으므로, 두 기능이 앱에 등록한 화면과 배치 정보, ViewModel을 그대로 띄워 함께 확인한다.
// 기기에 저장된 루틴과 기록은 UseCase 대역이 흉내 내며, 대역은 목록을 정렬하지 않으므로 목록의 순서는 저장소 테스트가 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagRoutineFinishedListDetailPaneTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val tagId: Uuid = Arb.kotlinUuid().next()
    private val listBackStack: List<ScreenNavKey> = listOf(RoutineHomeNavKey, TagDetailNavKey(id = tagId), TagRoutineFinishedListNavKey(tagId = tagId))
    private val backStack: NavBackStack<ScreenNavKey> = NavBackStack(*listBackStack.toTypedArray())
    private val store = RoutineStore(tagId = tagId)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        mockkObject(Clock.System)
        every { Clock.System.now() } returns LocalDateTime(TODAY, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())
    }

    @After
    fun tearDown() {
        unmockkObject(Clock.System)
    }

    @Test
    fun `TC-2488bcc7-9c4d-46de-b9a5-cbaea1e2f4d9 상세 영역에서 루틴을 삭제하면 선택 전 상태로 돌아간다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)

        clickDetailAction(DELETE_DESCRIPTION)

        assertDeleted(deleted = routineList[0], remainingTitleList = listOf(PLANT_TITLE))
    }

    @Test
    fun `TC-2488bcc7-9c4d-46de-b9a5-cbaea1e2f4d9 이어 고른 다른 루틴을 상세 영역에서 삭제해도 선택 전 상태로 돌아간다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)
        selectRoutine(PLANT_TITLE)
        assertDetail(title = PLANT_TITLE)

        clickDetailAction(DELETE_DESCRIPTION)

        assertDeleted(deleted = routineList[1], remainingTitleList = listOf(WATER_TITLE))
    }

    @Test
    fun `TC-58b393e8-9d06-4edd-af50-73b28fefc875 상세 영역에서 루틴을 다시 시작하면 목록에서 사라지고 상세는 그대로 남는다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)

        clickDetailAction(RESTART_DESCRIPTION)

        waitUntilCardTitles(PLANT_TITLE)
        store.stored(routineList[0]).isFinished shouldBe false
        backStack.toList() shouldBe listBackStack + RoutineDetailNavKey(id = routineList[0].id)
        assertDetail(title = WATER_TITLE)
        detailActionCount(FINISH_DESCRIPTION) shouldBe 1
        detailActionCount(RESTART_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-53b1e84d-e7b5-4b71-a486-0abc17333e5b 상세 영역에서 다시 시작한 루틴을 다시 완료하면 목록에 다시 나타난다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)
        clickDetailAction(RESTART_DESCRIPTION)
        waitUntilCardTitles(PLANT_TITLE)

        clickDetailAction(FINISH_DESCRIPTION)

        waitUntilCardTitles(WATER_TITLE, PLANT_TITLE)
        store.stored(routineList[0]).isFinished shouldBe true
        assertDetail(title = WATER_TITLE)
        detailActionCount(RESTART_DESCRIPTION) shouldBe 1
        detailActionCount(FINISH_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-9179153b-7a39-4d3f-a2fd-4e0844c1278a 상세 영역에서 기록과 목표량을 바꿔도 카드 문구는 완료 그대로이고 제목을 고치면 카드가 바뀐 제목을 보여 준다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)

        clickDetailAction(RECORD_ADD_DESCRIPTION)
        waitUntilExists(hasContentDescription(RECORD_REMOVE_DESCRIPTION))
        store.storedRecordList.map { record -> record.date to record.amount } shouldBe listOf(TODAY to 250)
        assertFinishedCards(WATER_TITLE, PLANT_TITLE)

        composeRule.onNode(hasContentDescription(RECORD_REMOVE_DESCRIPTION) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
        waitUntilGone(hasContentDescription(RECORD_REMOVE_DESCRIPTION))
        store.storedRecordList.shouldBeEmpty()
        assertFinishedCards(WATER_TITLE, PLANT_TITLE)

        goalAmountInput().performTextReplacement("3000")
        clickDetailAction(UPDATE_DESCRIPTION)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { store.stored(routineList[0]).detail.goalAmount == 3000 }
        assertFinishedCards(WATER_TITLE, PLANT_TITLE)

        titleInput().performTextReplacement(RENAMED_TITLE)
        clickDetailAction(UPDATE_DESCRIPTION)
        assertFinishedCards(RENAMED_TITLE, PLANT_TITLE)
    }

    @Test
    fun `TC-9179153b-7a39-4d3f-a2fd-4e0844c1278a 상세 영역에서 시작 날짜를 고쳐 시작 전이 되어도 카드 문구는 완료 그대로다`() {
        val routineList = routineList()
        setNavDisplay(routineList = routineList)
        selectRoutine(WATER_TITLE)
        assertDetail(title = WATER_TITLE)

        pickDate(currentText = START_DATE_TEXT, date = LocalDate(2026, 3, 21))
        clickDetailAction(UPDATE_DESCRIPTION)

        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { store.stored(routineList[0]).detail.startDate == LocalDate(2026, 3, 21) }
        assertFinishedCards(WATER_TITLE, PLANT_TITLE)
    }

    @Test
    fun `TC-2d07d2df-ff06-40e5-b488-bdd92abca85c TagRoutineFinishedList에서 진입한 루틴 상세는 루틴 목록과 함께 표시하지 않는다`() {
        val unfinished = routine(title = UNFINISHED_TITLE).copy(isFinished = false)
        val finished = routine(title = WATER_TITLE)
        setNavDisplay(routineList = listOf(unfinished, finished))

        selectRoutine(WATER_TITLE)

        backStack.toList() shouldBe listBackStack + RoutineDetailNavKey(id = finished.id)
        assertDetail(title = WATER_TITLE)
        cardTextList() shouldBe listOf(listOf(WATER_TITLE, FINISHED_TEXT))
        composeRule.onNode(card(UNFINISHED_TITLE)).assertDoesNotExist()
        composeRule.onAllNodes(hasContentDescription(SHOW_CALENDAR_DESCRIPTION)).assertCountEquals(0)
        composeRule.onAllNodes(hasContentDescription(SHOW_LIST_DESCRIPTION)).assertCountEquals(0)
    }

    private fun setNavDisplay(routineList: List<Routine>) {
        store.setRoutineList(routineList)
        val koinApplication = store.koinApplication()
        composeRule.setContent {
            KoinIsolatedContext(context = koinApplication) {
                CompositionLocalProvider(LocalViewModelStoreOwner provides remember { TestViewModelStoreOwner() }) {
                    DiaryTheme {
                        AppNavDisplay()
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun AppNavDisplay() {
        val resultEventBus = rememberResultEventBus()
        val placeholderStateHolder = rememberListDetailPlaceholderStateHolder()
        val entryProvider = remember { appEntryProvider(backStack = backStack) }

        CompositionLocalProvider(
            LocalResultEventBus provides resultEventBus,
            LocalListDetailPlaceholderStateHolder provides placeholderStateHolder,
        ) {
            NavDisplay(
                backStack = backStack,
                sceneStrategies =
                    listOf(
                        remember { BottomSheetSceneStrategy<ScreenNavKey>() },
                        rememberDiaryListDetailSceneStrategy(),
                    ),
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                        rememberResultEventBusNavEntryDecorator(resultEventBus),
                        rememberListDetailPlaceholderNavEntryDecorator(placeholderStateHolder),
                    ),
                entryProvider = entryProvider,
            )
        }
    }

    private fun selectRoutine(title: String) {
        waitUntilExists(card(title))
        composeRule.onNode(card(title)).performClick()
        composeRule.waitForIdle()
    }

    private fun assertDetail(title: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule
                .onAllNodes(hasSetTextAction())
                .fetchSemanticsNodes()
                .firstOrNull()
                ?.config
                ?.getOrNull(SemanticsProperties.EditableText)
                ?.text == title
        }
        composeRule.onNodeWithText(PLACEHOLDER_MESSAGE).assertDoesNotExist()
    }

    private fun assertDeleted(
        deleted: Routine,
        remainingTitleList: List<String>,
    ) {
        waitUntilExists(hasText(PLACEHOLDER_MESSAGE))
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        backStack.toList() shouldBe listBackStack
        store.stored(deleted).isDeleted shouldBe true
        waitUntilCardTitles(*remainingTitleList.toTypedArray())
    }

    private fun assertFinishedCards(vararg title: String) {
        waitUntilCardTitles(*title)
        cardTextList() shouldContainExactlyInAnyOrder title.map { value -> listOf(value, FINISHED_TEXT) }
    }

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[TITLE_INDEX]

    private fun goalAmountInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[GOAL_AMOUNT_INDEX]

    // 목록 카드의 스와이프 표시도 같은 이름을 쓰므로 상세 영역에 있는 버튼을 누른다.
    private fun clickDetailAction(description: String) {
        val interactions = composeRule.onAllNodes(hasContentDescription(description) and hasClickAction())
        val detailLeft = titleInput().fetchSemanticsNode().boundsInRoot.left
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailLeft }

        interactions[index].performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
    }

    private fun detailActionCount(description: String): Int {
        val detailLeft = titleInput().fetchSemanticsNode().boundsInRoot.left

        return composeRule
            .onAllNodes(hasContentDescription(description) and hasClickAction())
            .fetchSemanticsNodes()
            .count { node -> node.boundsInRoot.left >= detailLeft }
    }

    // 날짜 선택 창을 글자 입력으로 바꿔 날짜를 적고 확인한다.
    private fun pickDate(
        currentText: String,
        date: LocalDate,
    ) {
        composeRule.onNodeWithText(currentText).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(SWITCH_TO_INPUT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextClearance()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextInput(date.toInputText())
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONFIRM).performClick()
        composeRule.waitForIdle()
    }

    private fun card(title: String): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title)

    private fun cardTextList(): List<List<String>> =
        composeRule
            .onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .map { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .map { text -> text.text }
                    .filter { text -> text.isNotEmpty() }
            }

    private fun waitUntilCardTitles(vararg title: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { cardTextList().mapNotNull { textList -> textList.firstOrNull() }.toSet() == title.toSet() }
        cardTextList().size shouldBe title.size
    }

    private fun waitUntilExists(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitUntilGone(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        const val TIMEOUT_MILLIS = 5_000L
        const val WATER_TITLE = "물 마시기"
        const val PLANT_TITLE = "물 주기"
        const val RENAMED_TITLE = "물 한 잔"
        const val UNFINISHED_TITLE = "산책"
        const val START_DATE_TEXT = "Mar 1, 2026"
        const val FINISHED_TEXT = "Finished"
        const val PLACEHOLDER_MESSAGE = "Choose a routine"
        const val UPDATE_DESCRIPTION = "Update routine"
        const val DELETE_DESCRIPTION = "Delete routine"
        const val FINISH_DESCRIPTION = "Finish routine"
        const val RESTART_DESCRIPTION = "Restart routine"
        const val RECORD_ADD_DESCRIPTION = "Add record"
        const val RECORD_REMOVE_DESCRIPTION = "Remove record, Mar 20, 2026 250ml"
        const val SHOW_CALENDAR_DESCRIPTION = "Show calendar"
        const val SHOW_LIST_DESCRIPTION = "Show list"
        const val SWITCH_TO_INPUT_DESCRIPTION = "Switch to text input mode"
        const val CONFIRM = "OK"
        const val TITLE_INDEX = 0
        const val GOAL_AMOUNT_INDEX = 3

        fun routineList(): List<Routine> = listOf(WATER_TITLE, PLANT_TITLE).map { title -> routine(title = title) }

        fun routine(title: String): Routine =
            Arb
                .routine(
                    isDeleted = false,
                    detail =
                        Arb.routineDetail().next().copy(
                            title = title,
                            description = "",
                            startDate = LocalDate(2026, 3, 1),
                            cycle = RoutineCycle.DAILY,
                            unit = "ml",
                            goalAmount = 2000,
                            amountPerRecord = 250,
                        ),
                    isFinished = true,
                ).next()
    }
}

// 기기에 저장된 루틴과 기록을 흉내 낸다. UseCase 대역이 이 저장 내용을 읽고 바꾸며, 조회는 저장한 순서 그대로 답한다.
private class RoutineStore(
    private val tagId: Uuid,
) {
    private val routineFlow = MutableStateFlow<List<Routine>>(emptyList())
    private val recordFlow = MutableStateFlow<List<RoutineRecord>>(emptyList())

    val storedRecordList: List<RoutineRecord>
        get() = recordFlow.value

    fun setRoutineList(routineList: List<Routine>) {
        routineFlow.value = routineList
    }

    fun stored(routine: Routine): Routine = routineFlow.value.first { value -> value.id == routine.id }

    fun koinApplication(): KoinApplication =
        koinApplication<DiaryKoinApplication> {
            allowOverride(true)
            modules(routineModule(), recordModule(), tagModule())
        }

    private fun routineModule(): Module {
        val pageFinishedTagRoutineUseCase = mockk<PageFinishedTagRoutineUseCase>()
        val findRoutineUseCase = mockk<FindRoutineUseCase>()
        val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
        val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
        val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
        val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
        val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()

        every { pageFinishedTagRoutineUseCase(parameter = any()) } answers {
            val parameter = firstArg<PageFinishedTagRoutineUseCase.Parameter>()

            combine(routineFlow, recordFlow) { routineList, recordList ->
                Result.success(loadedPagingDataOf(if (parameter.tagId == tagId) routineList.toFinishedSummaryList(recordList = recordList, today = parameter.today) else emptyList()))
            }
        }
        every { findRoutineUseCase(parameter = any()) } answers {
            val id = firstArg<Uuid>()

            routineFlow.map { list -> Result.success(list.firstOrNull { routine -> routine.id == id }) }
        }
        coEvery { updateRoutineUseCase(parameter = any()) } coAnswers {
            val parameter = firstArg<UpdateRoutineUseCase.Parameter>()

            change(id = parameter.id) { routine -> routine.copy(detail = parameter.input.toRoutineDetail()) }
        }
        coEvery { finishRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isFinished = true) } }
        coEvery { restartRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isFinished = false) } }
        coEvery { deleteRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isDeleted = true) } }
        coEvery { restoreRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isDeleted = false) } }

        return module {
            factory { pageFinishedTagRoutineUseCase }
            factory { findRoutineUseCase }
            factory { updateRoutineUseCase }
            factory { finishRoutineUseCase }
            factory { restartRoutineUseCase }
            factory { deleteRoutineUseCase }
            factory { restoreRoutineUseCase }
        }
    }

    private fun recordModule(): Module {
        val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
        val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
        val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()

        every { pageRoutineRecordUseCase(parameter = any()) } answers {
            val routineId = firstArg<Uuid>()

            recordFlow.map { list -> Result.success(loadedPagingDataOf(list.filter { record -> record.routineId == routineId })) }
        }
        coEvery { addRoutineRecordUseCase(parameter = any()) } coAnswers {
            val parameter = firstArg<AddRoutineRecordUseCase.Parameter>()
            val record = Arb.routineRecord(routineId = parameter.routineId).next().copy(date = parameter.date, amount = parameter.amount)
            recordFlow.update { list -> list + record }

            Result.success(record.id)
        }
        coEvery { deleteRoutineRecordUseCase(parameter = any()) } coAnswers {
            val recordId = firstArg<Uuid>()
            recordFlow.update { list -> list.filterNot { record -> record.id == recordId } }

            Result.success(1)
        }

        return module {
            factory { pageRoutineRecordUseCase }
            factory { addRoutineRecordUseCase }
            factory { deleteRoutineRecordUseCase }
        }
    }

    private fun tagModule(): Module {
        val findTagUseCase = mockk<FindTagUseCase>()
        val getProgressReportedUseCase = mockk<GetProgressReportedUseCase>()
        val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
        val pageEntityTagSelectableTagUseCase = mockk<PageEntityTagSelectableTagUseCase>()

        every { findTagUseCase(parameter = any()) } returns flowOf(Result.success(Arb.tag().next().copy(id = tagId, isDeleted = false)))
        every { getProgressReportedUseCase() } returns flowOf(Result.success(false))
        every { getEntityTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        every { pageEntityTagSelectableTagUseCase(parameter = any()) } returns flowOf(Result.success(loadedPagingDataOf(emptyList())))

        return module {
            factory { findTagUseCase }
            factory { getProgressReportedUseCase }
            factory { getEntityTagUseCase }
            factory { pageEntityTagSelectableTagUseCase }
            factory { mockk<RequestSyncUseCase>() }
            factory { mockk<AddEntityTagUseCase>() }
            factory { mockk<RemoveEntityTagUseCase>() }
        }
    }

    private fun change(
        id: Uuid,
        transform: (Routine) -> Routine,
    ): Result<Int> {
        routineFlow.update { list -> list.map { routine -> if (routine.id == id) transform(routine) else routine } }

        return Result.success(1)
    }

    private fun List<Routine>.toFinishedSummaryList(
        recordList: List<RoutineRecord>,
        today: LocalDate,
    ): List<RoutineSummary> {
        val currentCycle = routineCurrentCycle(today = today)

        return filter { routine -> routine.isFinished && !routine.isDeleted }
            .map { routine ->
                val range = currentCycle.range(cycle = routine.detail.cycle)

                RoutineSummary(
                    routine = routine,
                    cycleAmount = recordList.filter { record -> record.routineId == routine.id && record.date in range }.sumOf { record -> record.amount.toLong() },
                )
            }
    }
}

private class TestViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

private fun <T : Any> loadedPagingDataOf(list: List<T>): PagingData<T> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

private fun RoutineInput.toRoutineDetail(): RoutineDetail =
    RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

private fun LocalDate.toInputText(): String = "${month.ordinal.plus(1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}$year"
