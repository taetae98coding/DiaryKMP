package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DDAY_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_QR_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.ddayEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.ddayPagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.ddayViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.syncUiStateFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.syncViewModelRef
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagDDay
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailDDayScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    // 현재 계정과 연결되지 않았거나 삭제된 태그는 조회되지 않으므로 대상 태그를 조회하지 못한 상태로 둔다.
    @Test
    fun `TC-07ac6b85-0960-403d-a243-964c88bb4347 대상 태그의 상태와 무관하게 디데이 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)

        listOf(
            tagDetailUiState(detail = tagDetail(TAG_TITLE)),
            TagDetailUiState.Loading,
            tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
        ).forEach { uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-ee216b2f-cca4-4240-ad87-599d9c553e07 디데이 추가를 선택하면 추가 화면 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ee216b2f-cca4-4240-ad87-599d9c553e07 한국어 환경 디데이 추가 버튼의 접근성 이름은 디데이 추가다`() {
        setScreenOnTab(tabDescription = KOREAN_DDAY_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-749e274c-5873-40d8-bdf8-fa9feadf428a 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-57cf935d-7107-45ac-9c3a-1648c601bb3c 빈 상태에서도 디데이 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-b15482d3-692d-40df-9931-7007aeaefecf 디데이 추가는 디데이 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_QR_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-5fc1e198-f6c0-4aef-86be-eb12e34474cd TC-670f23ef-9288-44df-9bb8-7981c75e1fd3 대상 태그를 조회하지 못해도 디데이 목록과 동작을 제공한다`() {
        setScreenOnTab(
            uiState = TagDetailUiState.Loading,
            pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = ITEM_TITLE))),
        )
        waitForItem()

        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-8a780f18-6ca9-43cd-b372-c55d623c510f 디데이를 선택하면 그 디데이의 상세 이동을 요청한다`() {
        val item = tagDDay(title = ITEM_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            navigateToDetail = navigatedIdList::add,
        )
        waitForItem()

        composeRule.onNodeWithText(ITEM_TITLE).performClick()

        navigatedIdList shouldBe listOf(item.id)
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 디데이 탭에서 목록을 당기면 계정 데이터 동기화를 한 번 요청하고 진행 중임을 표시한다`() {
        setScreenOnTab(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = ITEM_TITLE))))
        waitForItem()
        every { requireNotNull(syncViewModelRef).refresh() } answers { syncUiStateFlow.value = SyncRefreshUiState(isRefreshing = true) }

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 디데이 탭이 빈 상태여도 목록을 당기면 계정 데이터 동기화를 한 번 요청하고 진행 중임을 표시한다`() {
        setScreenOnTab()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        every { requireNotNull(syncViewModelRef).refresh() } answers { syncUiStateFlow.value = SyncRefreshUiState(isRefreshing = true) }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-4382250e-5614-4d93-b8e0-3a543a092105 TC-5c5b0403-8a85-454d-b486-04ff8a8c2c23 디데이 카드를 삭제 방향으로 밀면 탭은 바뀌지 않고 그 디데이 삭제를 요청하며 기본 안내와 실행 취소를 표시한다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_DDAY_TAB_DESCRIPTION).assertIsSelected()
        emitEffect(DDayListEffect.Deleted(id = item.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-4382250e-5614-4d93-b8e0-3a543a092105 디데이 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val item = swipeItem(tabDescription = KOREAN_DDAY_TAB_DESCRIPTION)

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-e4c34ecb-550c-4880-a2e1-39a34b9ed31a TC-0ad9ffe8-1686-4386-9d4d-397acbd57d8c 디데이 카드를 오른쪽으로 밀면 탭은 바뀌지 않고 아무 상태 변경도 요청하지 않는다`() {
        swipeItem()

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel().delete(id = any()) }
        verify(exactly = 0) { viewModel().restore(id = any()) }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_DDAY_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-cec6fa57-4f18-4ca8-9343-2cfb0249e203 삭제를 실행 취소하면 그 디데이의 삭제 되돌리기를 요청한다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = item.id) }
    }

    @Test
    fun `TC-4e316c58-2cf0-4839-bf4a-c0a401062ea7 삭제가 저장되지 못해 안내가 오지 않으면 안내와 실행 취소를 표시하지 않는다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-b6cecd45-869d-478d-a4d8-f643258b7888 안내가 보이는 동안 다른 디데이를 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val item = swipeItem()
        val otherId = Arb.kotlinUuid().next()

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitEffect(DDayListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = otherId) }
        verify(exactly = 0) { viewModel().restore(id = item.id) }
    }

    @Test
    fun `TC-ac55a565-7623-4c27-89b6-524645c32fdb 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `TC-8afedc19-db03-4132-ba3a-ca4d91ffe844 디데이 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val item = tagDDay(title = ITEM_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(pagingData = tagEntityPagingData(itemList = listOf(item)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)
        waitForItem()

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { viewModel().delete(id = item.id) }
        verify(exactly = 1) { viewModel().restore(id = item.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-bbe924d2-9354-4be5-bb06-ddd1a93a6256 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(DDayListEffect.Deleted(id = item.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    private fun swipeItem(tabDescription: String = DEFAULT_DDAY_TAB_DESCRIPTION): DDay {
        val item = tagDDay(title = ITEM_TITLE)
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            tabDescription = tabDescription,
        )
        waitForItem()

        return item
    }

    private fun swipeCardLeft() {
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun waitForItem() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitEffect(effect: DDayListEffect) {
        ddayPagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        ddayEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun viewModel(): TagDetailDDayViewModel = requireNotNull(ddayViewModelRef)

    private fun setScreenOnTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<DDay> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        tabDescription: String = DEFAULT_DDAY_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            pagingData = pagingData,
            navigateToAdd = navigateToAdd,
            navigateToDetail = navigateToDetail,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<DDay> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            ddayPagingData = pagingData,
            navigateToDDayAdd = navigateToAdd,
            navigateToDDayDetail = navigateToDetail,
        )
    }

    private companion object {
        const val ITEM_TITLE = "TagDetailScreenDDay"
        const val KOREAN_DDAY_TAB_DESCRIPTION = "디데이"
        const val DEFAULT_EMPTY_TITLE = "No D-Days linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add D-Day"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "디데이 추가"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_DELETED_MESSAGE = "D-Day deleted."
        const val KOREAN_DELETED_MESSAGE = "디데이가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
    }
}
