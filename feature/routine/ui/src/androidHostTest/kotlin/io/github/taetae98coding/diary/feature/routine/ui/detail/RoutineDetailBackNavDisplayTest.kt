package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

/**
 * 제품 routineEntry로 NavDisplay를 구성해 RoutineDetail 화면을 뒤로가기로 떠나고 다시 들어오는 결과를 확인한다. 다른 기능 모듈의 화면은 대역 키로 대신한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailBackNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val routineId: Uuid = Arb.kotlinUuid().next()
    private val tagA: Tag = tagOf(title = TAG_A_TITLE)
    private val tagB: Tag = tagOf(title = TAG_B_TITLE)
    private val backStack = NavBackStack<ScreenNavKey>()
    private val detailViewModelList = mutableListOf<RoutineDetailViewModel>()
    private val recordViewModelList = mutableListOf<RoutineDetailRecordViewModel>()

    // 기기에 저장된 루틴, 기록과 태그 연결이다. 기록 넣기와 빼기, 태그 연결과 해제는 바로 저장되어 새로 만든 화면도 같은 내용을 보여 준다.
    private val storedDetail: RoutineDetail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, amountPerRecord = 250)
    private val storedRecordList = MutableStateFlow<List<RoutineRecord>>(emptyList())
    private val storedRecordPagingData = MutableStateFlow(routineRecordLoadedPagingDataOf(emptyList()))
    private val storedTagUiState = MutableStateFlow(EntityTagInputUiState())

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        routineRecordSetToday(TODAY)
    }

    @After
    fun tearDown() {
        routineRecordReleaseToday()
        stopKoin()
    }

    @Test
    fun `TC-7d5c9e5d-b0e0-4f07-842f-1951b847dbe2 RoutineHome 목록에서 진입해 뒤로가면 RoutineHome으로 돌아가고 저장하지 않는다`() {
        backStack.addAll(listOf(RoutineHomeNavKey, RoutineDetailNavKey(id = routineId)))
        setNavDisplay()
        editInput()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE).assertIsDisplayed()
        assertNothingSaved()
    }

    @Test
    fun `TC-7d5c9e5d-b0e0-4f07-842f-1951b847dbe2 RoutineHome 달력 보기에서 진입해 뒤로가면 RoutineHome으로 돌아가고 저장하지 않는다`() {
        backStack.add(RoutineHomeNavKey)
        setNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE).performClick()
        composeRule.waitForIdle()
        // 달력 보기의 루틴 기록 아이템을 누르면 그 루틴의 RoutineDetail로 이동한다.
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routineId)) }
        composeRule.waitForIdle()
        editInput()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE).assertIsDisplayed()
        assertNothingSaved()
    }

    @Test
    fun `TC-7d5c9e5d-b0e0-4f07-842f-1951b847dbe2 TagDetail 루틴 탭에서 진입해 뒤로가면 그 TagDetail로 돌아가고 저장하지 않는다`() {
        backStack.add(TagDetailNavKey(id = tagA.id))
        setNavDisplay()
        composeRule.onNodeWithText(TAG_DETAIL_ROUTINE_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routineId)) }
        composeRule.waitForIdle()
        editInput()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_ROUTINE_TAB").assertIsDisplayed()
        assertNothingSaved()
    }

    @Test
    fun `TC-7d5c9e5d-b0e0-4f07-842f-1951b847dbe2 SearchHome에서 진입해 뒤로가면 SearchHome으로 돌아가고 저장하지 않는다`() {
        backStack.addAll(listOf(RoutineDetailBackSearchHomeStandInNavKey, RoutineDetailNavKey(id = routineId)))
        setNavDisplay()
        editInput()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineDetailBackSearchHomeStandInNavKey)
        composeRule.onNodeWithText(SEARCH_CONTENT).assertIsDisplayed()
        assertNothingSaved()
    }

    @Test
    fun `TC-453831c8-63fb-4ad7-b592-98d3a1bfd20c 뒤로가도 이미 넣거나 뺀 기록과 반영된 태그 연결과 해제는 그대로 남는다`() {
        val recordY = routineRecordOf(date = LocalDate(2026, 3, 5), amount = 100, routineId = routineId)
        storeRecordList(listOf(recordY))
        storedTagUiState.value = EntityTagInputUiState(tagList = listOf(tagB))
        backStack.addAll(listOf(RoutineDetailBackSearchHomeStandInNavKey, RoutineDetailNavKey(id = routineId)))
        setNavDisplay()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")
        composeRule.routineRecordClickAdd()
        composeRule.routineRecordClickRemove(date = recordY.date, amountText = "100ml")
        linkTag(title = TAG_A_TITLE)
        unlinkTag(title = TAG_B_TITLE)
        pressBack()
        if (backStack.lastOrNull() is RoutineDetailNavKey) pressBack()

        backStack.toList() shouldBe listOf(RoutineDetailBackSearchHomeStandInNavKey)
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routineId)) }
        composeRule.waitForIdle()

        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_10, amountText = "500ml"))
        composeRule.onNode(tagChip(TAG_A_TITLE)).assertExists()
        composeRule.onNode(tagChip(TAG_B_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-018c8b0c-91e9-49b4-b6f8-c6c52e0def8d 뒤로가기로 떠난 뒤 다시 들어오면 저장된 내용과 기록 넣기의 처음 값으로 다시 시작한다`() {
        backStack.addAll(listOf(RoutineDetailBackSearchHomeStandInNavKey, RoutineDetailNavKey(id = routineId)))
        setNavDisplay()
        editInput()
        composeRule.onNode(routineRecordUpdateButton()).assertExists()

        clickNavigateUp()
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routineId)) }
        composeRule.waitForIdle()

        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe storedDetail.title
        composeRule.routineRecordDescriptionInput().routineRecordEditableText() shouldBe storedDetail.description
        composeRule.routineRecordDisplayedColorHex() shouldBe storedDetail.color.routineRecordColorHex()
        composeRule.routineRecordFormDateButton(storedDetail.startDate).assertExists()
        composeRule.routineRecordGoalInput().routineRecordEditableText() shouldBe "2000"
        composeRule.routineRecordAmountPerRecordInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-54e64fd8-a6a7-4b69-a220-e2d2df1e6944 화면을 떠났다가 다시 진입하면 오늘을 다시 정한다`() {
        backStack.addAll(listOf(RoutineDetailBackSearchHomeStandInNavKey, RoutineDetailNavKey(id = routineId)))
        setNavDisplay()
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()

        routineRecordSetToday(TOMORROW)
        clickNavigateUp()
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routineId)) }
        composeRule.waitForIdle()

        composeRule.onNode(routineRecordDateButton(TOMORROW)).assertExists()
    }

    // 제목과 목표량을 고치고, 기록할 날짜를 고르고 기록의 양을 입력한다.
    private fun editInput() {
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.routineRecordGoalInput().performTextReplacement("3000")
        composeRule.waitForIdle()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")
    }

    private fun assertNothingSaved() {
        detailViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.update(input = any()) } }
        recordViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(date = any(), amount = any()) } }
    }

    private fun clickNavigateUp() {
        composeRule.onNodeWithContentDescription(ROUTINE_RECORD_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun linkTag(title: String) {
        composeRule.routineRecordOpenTagPicker()
        composeRule.routineRecordAwaitText(text = title)
        composeRule.onNodeWithText(title).performClick()
        composeRule.waitForIdle()
    }

    // 연결된 태그는 태그 입력과 선택 목록에 모두 나타나므로, 선택 목록의 마지막 노드를 눌러 해제한다.
    private fun unlinkTag(title: String) {
        composeRule.routineRecordOpenTagPicker()
        composeRule.routineRecordAwaitText(text = title, count = 2)
        composeRule.onAllNodesWithText(title).onLast().performClick()
        composeRule.waitForIdle()
    }

    private fun tagChip(title: String): SemanticsMatcher = hasText(title) and !hasAnyAncestor(isDialog())

    private fun storeRecordList(recordList: List<RoutineRecord>) {
        storedRecordList.value = recordList.sortedByDescending { record -> record.date }
        storedRecordPagingData.value = routineRecordLoadedPagingDataOf(storedRecordList.value)
    }

    private fun setNavDisplay() {
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<RoutineDetailBackSearchHomeStandInNavKey> { Text(text = SEARCH_CONTENT) }
                            entry<TagDetailNavKey> { TagDetailStandIn() }
                            routineEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    // TagDetail 화면을 대신한다. 탭 선택은 그 화면이 저장하는 상태이므로 같은 방식으로 저장해 돌아왔을 때의 탭을 드러낸다.
    @Composable
    private fun TagDetailStandIn() {
        var selectedTab by rememberSaveable { mutableStateOf(TAG_DETAIL_MEMO_TAB) }

        Column {
            Text(text = "$TAG_DETAIL_SELECTED_TAB_PREFIX$selectedTab")
            TextButton(onClick = { selectedTab = TAG_DETAIL_ROUTINE_TAB }) { Text(text = TAG_DETAIL_ROUTINE_TAB) }
        }
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                RoutineTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory { routineHomeViewModel() }
            factory { routineHomeCalendarViewModel() }
            factory { routineHomeHolidayViewModel() }
            factory { routineSyncRefreshViewModel() }
            factory {
                mockk<RoutineDetailViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(RoutineDetailUiState.Content(id = routineId, detail = storedDetail, isFinished = false))
                        every { effect } returns Channel<RoutineDetailEffect>().receiveAsFlow()
                    }.also(detailViewModelList::add)
            }
            factory {
                val effectChannel = Channel<RoutineDetailRecordEffect>(capacity = Channel.BUFFERED)
                mockk<RoutineDetailRecordViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(RoutineDetailRecordUiState())
                        every { recordPagingData } returns storedRecordPagingData
                        every { effect } returns effectChannel.receiveAsFlow()
                        every { add(date = any(), amount = any()) } answers {
                            storeRecordList(storedRecordList.value + routineRecordOf(date = firstArg(), amount = secondArg(), routineId = routineId))
                            effectChannel.trySend(RoutineDetailRecordEffect.AddSucceeded)
                        }
                        every { delete(recordId = any()) } answers {
                            val recordId = firstArg<Uuid>()

                            storeRecordList(storedRecordList.value.filterNot { record -> record.id == recordId })
                        }
                    }.also(recordViewModelList::add)
            }
            factory {
                mockk<EntityDetailTagViewModel>(relaxed = true).apply {
                    val allTagList = listOf(tagA, tagB)
                    every { uiState } returns storedTagUiState
                    every { tagPagingData } returns MutableStateFlow(PagingData.from(allTagList))
                    every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(allTagList))
                    every { add(tagId = any()) } answers {
                        val tagId = firstArg<Uuid>()
                        storedTagUiState.update { state -> state.copy(tagList = state.tagList + allTagList.filter { tag -> tag.id == tagId }) }
                    }
                    every { remove(tagId = any()) } answers {
                        val tagId = firstArg<Uuid>()
                        storedTagUiState.update { state -> state.copy(tagList = state.tagList.filterNot { tag -> tag.id == tagId }) }
                    }
                }
            }
        }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val TOMORROW: LocalDate = LocalDate(2026, 3, 21)
        val MARCH_10: LocalDate = LocalDate(2026, 3, 10)
        const val DEFAULT_CALENDAR_VIEW_MODE = "Show calendar"
        const val DEFAULT_LIST_VIEW_MODE = "Show list"
        const val SEARCH_CONTENT = "SearchHomeContent"
        const val TAG_DETAIL_MEMO_TAB = "MemoTab"
        const val TAG_DETAIL_ROUTINE_TAB = "RoutineTab"
        const val TAG_DETAIL_SELECTED_TAB_PREFIX = "Selected: "
        const val TAG_A_TITLE = "RoutineDetailBackTagA"
        const val TAG_B_TITLE = "RoutineDetailBackTagB"

        fun tagOf(title: String): Tag =
            Arb.tag().next().let { tag ->
                tag.copy(detail = tag.detail.copy(emoji = "", title = title), isFinished = false, isDeleted = false)
            }
    }
}

// 다른 기능 모듈의 화면은 이 모듈의 의존이 아니므로 대역 키로 대신한다.
private data object RoutineDetailBackSearchHomeStandInNavKey : ScreenNavKey {
    override val screenName: String get() = "SearchHome"
}
