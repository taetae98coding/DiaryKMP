package io.github.taetae98coding.diary.feature.dday.ui.detail

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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
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
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.ui.dDayEntry
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDateButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDescriptionInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayedColorHex
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEditableText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInputRepeatNumber
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayIsCountsBaseDayOn
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickDate
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeViewModel
import io.github.taetae98coding.diary.feature.dday.ui.home.homeDDayPagingDataOf
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
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
 * 제품 dDayEntry로 NavDisplay를 구성해 DDayDetail 화면의 진입과 떠남을 확인한다. 다른 기능 모듈의 화면은 대역 키로 대신한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayDetailNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dday: DDay = homeDDayOfDetail()
    private val tagA: Tag = Arb.tag(title = TAG_A_TITLE).next().copy(isDeleted = false)
    private val tagB: Tag = Arb.tag(title = TAG_B_TITLE).next().copy(isDeleted = false)
    private val backStack = NavBackStack<ScreenNavKey>()
    private val detailViewModelList = mutableListOf<DDayDetailViewModel>()
    private val homeViewModelList = mutableListOf<DDayHomeViewModel>()
    private val syncViewModelList = mutableListOf<SyncRefreshViewModel>()
    private val homeDDayList = MutableStateFlow(listOf(dday))
    private val homePagingData = MutableStateFlow(homeDDayPagingDataOf(listOf(dday)))

    // 디데이와 태그의 저장된 연결이다. 연결과 해제는 바로 저장되어, 새로 만든 태그 입력도 같은 연결을 보여 준다.
    private val storedTagUiState = MutableStateFlow(EntityTagInputUiState())

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 DDayHome 목록에서 진입해 삭제하면 DDayHome으로 돌아간다`() {
        assertDeleteReturns(route = DDayHomeNavKey)
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 캘린더 홈에서 진입해 삭제하면 캘린더 홈으로 돌아간다`() {
        assertDeleteReturns(route = CalendarHomeStandInNavKey)
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 시간표에서 진입해 삭제하면 시간표로 돌아간다`() {
        assertDeleteReturns(route = TimetableStandInNavKey)
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 TagDetail 디데이 탭에서 진입해 삭제하면 그 TagDetail로 돌아간다`() {
        enterFromTagDetailDDayTab()

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        detailViewModelList.forEach { viewModel -> verify(exactly = 1) { viewModel.delete() } }
        backStack.toList() shouldBe listOf(TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_DDAY_TAB").assertIsDisplayed()
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 SearchHome에서 진입해 삭제하면 SearchHome으로 돌아간다`() {
        assertDeleteReturns(route = SearchHomeStandInNavKey)
    }

    private fun assertDeleteReturns(route: ScreenNavKey) {
        backStack.addAll(listOf(route, DDayDetailNavKey(id = dday.id)))
        setNavDisplay()

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        detailViewModelList.forEach { viewModel -> verify(exactly = 1) { viewModel.delete() } }
        backStack.toList() shouldBe listOf(route)
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 DDayHome 목록에서 진입해 뒤로가면 DDayHome으로 돌아가고 수정하지 않는다`() {
        assertBackReturns(route = DDayHomeNavKey)
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 캘린더 홈에서 진입해 뒤로가면 캘린더 홈으로 돌아가고 수정하지 않는다`() {
        assertBackReturns(route = CalendarHomeStandInNavKey)
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 시간표에서 진입해 뒤로가면 시간표로 돌아가고 수정하지 않는다`() {
        assertBackReturns(route = TimetableStandInNavKey)
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 TagDetail 디데이 탭에서 진입해 뒤로가면 그 TagDetail로 돌아가고 수정하지 않는다`() {
        enterFromTagDetailDDayTab()
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_DDAY_TAB").assertIsDisplayed()
        detailViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.update(input = any()) } }
    }

    // TagDetail의 디데이 탭을 고른 뒤 그 탭의 디데이로 DDayDetail에 진입한다.
    private fun enterFromTagDetailDDayTab() {
        backStack.add(TagDetailNavKey(id = tagA.id))
        setNavDisplay()
        composeRule.onNodeWithText(TAG_DETAIL_DDAY_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(DDayDetailNavKey(id = dday.id)) }
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 SearchHome에서 진입해 뒤로가면 SearchHome으로 돌아가고 수정하지 않는다`() {
        assertBackReturns(route = SearchHomeStandInNavKey)
    }

    private fun assertBackReturns(route: ScreenNavKey) {
        backStack.addAll(listOf(route, DDayDetailNavKey(id = dday.id)))
        setNavDisplay()
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(route)
        detailViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.update(input = any()) } }
    }

    @Test
    fun `TC-8d4755e1-5f03-487e-be42-b09875fb668b TC-a61f7580-bf5b-4a67-9ad2-e481f9aa4d25 상세에서 삭제해 돌아온 DDayHome에는 삭제 안내와 실행 취소가 없다`() {
        backStack.add(DDayHomeNavKey)
        setNavDisplay()
        waitUntilCardDisplayed()

        composeRule.onNode(card()).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(DDayHomeNavKey, DDayDetailNavKey(id = dday.id))
        // 진입만으로는 서버와 맞추지 않는다.
        syncViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.refresh() } }
        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayHomeNavKey)
        composeRule.onNode(card()).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
        homeViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.delete(id = any()) } }
    }

    @Test
    fun `TC-ad6dd3b3-02c2-47ea-9008-dca0cc89a9ae 캘린더의 어느 날짜에서 들어와도 같은 디데이의 같은 내용을 보여 준다`() {
        backStack.add(CalendarHomeStandInNavKey)
        setNavDisplay()

        // 기준일 당일, 첫 기념일, 두 번째 기념일 중 어느 날짜를 골라도 이동 대상은 그 디데이 하나다.
        repeat(CALENDAR_DATE_COUNT) {
            composeRule.runOnIdle { backStack.add(DDayDetailNavKey(id = dday.id)) }
            composeRule.waitForIdle()

            assertStoredInput()

            composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
            composeRule.waitForIdle()
            backStack.toList() shouldBe listOf(CalendarHomeStandInNavKey)
        }
    }

    @Test
    fun `TC-758e9792-5a85-4288-ba68-3c2afdc171fd 뒤로가기로 떠난 뒤 다시 들어오면 저장된 내용으로 다시 시작한다`() {
        backStack.addAll(listOf(CalendarHomeStandInNavKey, DDayDetailNavKey(id = dday.id)))
        setNavDisplay()
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayPickDate(from = dday.detail.baseDate, to = LocalDate(2025, 2, 1))
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(DDayDetailNavKey(id = dday.id)) }
        composeRule.waitForIdle()

        assertStoredInput()
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-8bbf9397-4453-4c9d-9af2-16d69ad8fae2 뒤로가도 이미 반영된 태그 연결과 해제는 그대로 남는다`() {
        storedTagUiState.value = EntityTagInputUiState(tagList = listOf(tagB))
        backStack.addAll(listOf(CalendarHomeStandInNavKey, DDayDetailNavKey(id = dday.id)))
        setNavDisplay()
        composeRule.ddayDetailLinkTag(title = TAG_A_TITLE)
        composeRule.ddayDetailUnlinkTag(title = TAG_B_TITLE)
        pressBack()
        if (backStack.lastOrNull() is DDayDetailNavKey) pressBack()

        backStack.toList() shouldBe listOf(CalendarHomeStandInNavKey)
        composeRule.runOnIdle { backStack.add(DDayDetailNavKey(id = dday.id)) }
        composeRule.waitForIdle()

        composeRule.onNode(tagChip(TAG_A_TITLE)).assertExists()
        composeRule.onNode(tagChip(TAG_B_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-d7bd90da-4196-4810-aac3-1abc0adb9a65 연결한 태그를 누르면 TagDetail로 이동하고 뒤로가면 이 디데이 상세로 돌아온다`() {
        storedTagUiState.value = EntityTagInputUiState(tagList = listOf(tagA))
        backStack.addAll(listOf(CalendarHomeStandInNavKey, DDayDetailNavKey(id = dday.id)))
        setNavDisplay()

        composeRule.onNode(tagChip(TAG_A_TITLE)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(CalendarHomeStandInNavKey, DDayDetailNavKey(id = dday.id), TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText(TAG_DETAIL_CONTENT).assertIsDisplayed()

        pressBack()

        backStack.toList() shouldBe listOf(CalendarHomeStandInNavKey, DDayDetailNavKey(id = dday.id))
        composeRule.ddayTitleInput().ddayEditableText() shouldBe dday.detail.title
    }

    private fun assertStoredInput() {
        composeRule.ddayTitleInput().ddayEditableText() shouldBe dday.detail.title
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe dday.detail.description
        composeRule.ddayDisplayedColorHex() shouldBe dday.detail.color.colorHex()
        composeRule.ddayDateButton(dday.detail.baseDate).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe dday.detail.countsBaseDay
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    private fun waitUntilCardDisplayed() {
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(dday.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun card(): SemanticsMatcher = hasTestTag(DDAY_CARD_TEST_TAG) and hasText(dday.detail.title)

    private fun tagChip(title: String): SemanticsMatcher = hasText(title) and !hasAnyAncestor(isDialog())

    private fun setNavDisplay() {
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<CalendarHomeStandInNavKey> { Text(text = CALENDAR_CONTENT) }
                            entry<TimetableStandInNavKey> { Text(text = TIMETABLE_CONTENT) }
                            entry<SearchHomeStandInNavKey> { Text(text = SEARCH_CONTENT) }
                            entry<TagDetailNavKey> { TagDetailStandIn() }
                            dDayEntry(backStack = backStack)
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
            Text(text = TAG_DETAIL_CONTENT)
            Text(text = "$TAG_DETAIL_SELECTED_TAB_PREFIX$selectedTab")
            TextButton(onClick = { selectedTab = TAG_DETAIL_DDAY_TAB }) { Text(text = TAG_DETAIL_DDAY_TAB) }
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
                DDayFormTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory {
                mockk<DDayHomeViewModel>(relaxed = true)
                    .apply {
                        every { ddayPagingData } returns homePagingData
                        every { filterUiState } returns MutableStateFlow(DDayHomeScaffoldFilterUiState())
                        every { effect } returns Channel<DDayListEffect>().receiveAsFlow()
                    }.also(homeViewModelList::add)
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true)
                    .apply { every { uiState } returns MutableStateFlow(SyncRefreshUiState()) }
                    .also(syncViewModelList::add)
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val target = homeDDayList.value.first { value -> value.id == id }
                val effectChannel = Channel<DDayDetailEffect>(capacity = Channel.BUFFERED)
                mockk<DDayDetailViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(DDayDetailUiState.Content(id = id, detail = target.detail))
                        every { effect } returns effectChannel.receiveAsFlow()
                        // 삭제에 성공하면 기기에서 삭제 상태가 되어 DDayHome 목록에서 빠진다.
                        every { delete() } answers {
                            homeDDayList.update { list -> list.filterNot { value -> value.id == id } }
                            homePagingData.value = homeDDayPagingDataOf(homeDDayList.value)
                            effectChannel.trySend(DDayDetailEffect.DeleteSucceeded)
                        }
                    }.also(detailViewModelList::add)
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
        const val DEFAULT_DELETED_MESSAGE = "D-Day deleted."
        const val DEFAULT_UNDO = "Undo"
        const val CALENDAR_CONTENT = "CalendarHomeContent"
        const val TIMETABLE_CONTENT = "TimetableContent"
        const val SEARCH_CONTENT = "SearchHomeContent"
        const val TAG_DETAIL_CONTENT = "TagDetailContent"
        const val TAG_DETAIL_MEMO_TAB = "MemoTab"
        const val TAG_DETAIL_DDAY_TAB = "DDayTab"
        const val TAG_DETAIL_SELECTED_TAB_PREFIX = "Selected: "
        const val TAG_A_TITLE = "DDayDetailNavTagA"
        const val TAG_B_TITLE = "DDayDetailNavTagB"
        const val CALENDAR_DATE_COUNT = 3
        const val PAGING_TIMEOUT_MILLIS = 5_000L

        fun homeDDayOfDetail(): DDay =
            io.github.taetae98coding.diary.feature.dday.ui.home
                .homeDDayOf(title = DDAY_DETAIL_MARRIAGE) { detail -> ddayDetailOf(color = detail.color.let { ddayDetailColor() }) }
                .copy(id = Arb.kotlinUuid().next())
    }
}

// 다른 기능 모듈의 화면은 이 모듈의 의존이 아니므로 대역 키로 대신한다.
private data object CalendarHomeStandInNavKey : ScreenNavKey {
    override val screenName: String get() = "CalendarHome"
}

private data object TimetableStandInNavKey : ScreenNavKey {
    override val screenName: String get() = "Timetable"
}

private data object SearchHomeStandInNavKey : ScreenNavKey {
    override val screenName: String get() = "SearchHome"
}
