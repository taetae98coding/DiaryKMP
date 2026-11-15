package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

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
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DDAY_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_ROUTINE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.routineEffectFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.routinePagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.detail.routineViewModelRef
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
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailRoutineScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // 현재 계정과 연결되지 않았거나 삭제된 태그는 조회되지 않으므로 대상 태그를 조회하지 못한 상태로 둔다.
    @Test
    fun `TC-584fb0be-955c-42e4-ab1b-c8418542b9e3 대상 태그의 상태와 무관하게 루틴 추가를 시작할 수 있다`() {
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow))
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)

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
    fun `TC-b3666bd3-adb9-4242-b690-6b2c6e9a615f 루틴 추가를 선택하면 추가 화면 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-b3666bd3-adb9-4242-b690-6b2c6e9a615f 한국어 환경 루틴 추가 버튼의 접근성 이름은 루틴 추가다`() {
        setScreenOnTab(tabDescription = KOREAN_ROUTINE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-6c149b3a-845d-401e-80b0-a9c34193ef1e 대상 태그를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnTab(uiState = TagDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-177eb170-a1ec-4b84-96a6-571b7f634b85 빈 상태에서도 루틴 추가를 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToAdd = { navigateCount += 1 })
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-2f48ca5c-9158-42c0-96ce-1baf93cbf3dc 루틴 추가는 루틴 탭에서만 제공된다`() {
        setScreen()

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_DDAY_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-01f8cb1f-40c9-497d-9d4b-044037487cdc TC-06bf4d99-c6ce-40e4-a313-91926ef72836 대상 태그를 조회하지 못해도 루틴 목록과 동작을 제공한다`() {
        setScreenOnTab(
            uiState = TagDetailUiState.Loading,
            pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = ITEM_TITLE))),
        )
        waitForItem()

        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-18af2db4-3af3-46cc-b4b8-62b88bf64a53 루틴을 선택하면 그 루틴의 상세 이동을 요청한다`() {
        val item = tagRoutine(title = ITEM_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            navigateToDetail = navigatedIdList::add,
        )
        waitForItem()

        composeRule.onNodeWithText(ITEM_TITLE).performClick()

        navigatedIdList shouldBe listOf(item.routine.id)
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 루틴 탭에서 목록을 당기면 계정 데이터 동기화를 한 번 요청하고 진행 중임을 표시한다`() {
        setScreenOnTab(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = ITEM_TITLE))))
        waitForItem()
        every { requireNotNull(syncViewModelRef).refresh() } answers { syncUiStateFlow.value = SyncRefreshUiState(isRefreshing = true) }

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 루틴 탭이 빈 상태여도 목록을 당기면 계정 데이터 동기화를 한 번 요청하고 진행 중임을 표시한다`() {
        setScreenOnTab()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        every { requireNotNull(syncViewModelRef).refresh() } answers { syncUiStateFlow.value = SyncRefreshUiState(isRefreshing = true) }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(syncViewModelRef).refresh() }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-78c2f045-7fd9-423f-ba6d-5de1e37027d2 TC-5c5b0403-8a85-454d-b486-04ff8a8c2c23 루틴 카드를 삭제 방향으로 밀면 탭은 바뀌지 않고 그 루틴 삭제를 요청하며 기본 안내와 실행 취소를 표시한다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.routine.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_ROUTINE_TAB_DESCRIPTION).assertIsSelected()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-78c2f045-7fd9-423f-ba6d-5de1e37027d2 루틴 카드를 삭제 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val item = swipeItem(tabDescription = KOREAN_ROUTINE_TAB_DESCRIPTION)

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-648019bb-ee48-40e7-9ad8-62a28ae546d8 TC-0ad9ffe8-1686-4386-9d4d-397acbd57d8c 루틴 카드를 완료 방향으로 밀면 탭은 바뀌지 않고 그 루틴 완료만 요청하며 기본 안내와 실행 취소를 표시한다`() {
        val item = swipeItem()

        swipeCardRight()

        verify(exactly = 1) { viewModel().finish(id = item.routine.id) }
        verify(exactly = 0) { viewModel().delete(id = any()) }
        composeRule.onNodeWithContentDescription(DEFAULT_ROUTINE_TAB_DESCRIPTION).assertIsSelected()
        emitEffect(RoutineListEffect.Finished(id = item.routine.id))

        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-648019bb-ee48-40e7-9ad8-62a28ae546d8 루틴 카드를 완료 방향으로 밀면 한국어 안내와 실행 취소를 표시한다`() {
        val item = swipeItem(tabDescription = KOREAN_ROUTINE_TAB_DESCRIPTION)

        swipeCardRight()
        emitEffect(RoutineListEffect.Finished(id = item.routine.id))

        composeRule.onNodeWithText(KOREAN_FINISHED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-c3202b93-f1d8-45fc-8c20-7760d467065a 완료를 실행 취소하면 그 루틴의 다시 시작을 한 번 요청한다`() {
        val item = swipeItem()

        swipeCardRight()
        emitEffect(RoutineListEffect.Finished(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restart(id = item.routine.id) }
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `TC-17f1723e-ce7c-46fa-ac4d-5693c8b622f2 완료가 저장되지 못해 안내가 오지 않으면 루틴이 남고 다시 완료할 수 있다`() {
        val item = swipeItem()

        swipeCardRight()

        verify(exactly = 1) { viewModel().finish(id = item.routine.id) }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(SWIPE_RESET_MILLIS)
        swipeCardRight()

        verify(exactly = 2) { viewModel().finish(id = item.routine.id) }
    }

    @Test
    fun `TC-bc592962-fcae-45cb-b7c6-f61d2f3295b8 TC-4b884c00-d08d-4a8d-92e5-72f906e08423 완료된 루틴 확인을 선택하면 완료된 루틴 목록 이동을 한 번 요청한다`() {
        var navigateCount = 0
        setScreenOnTab(navigateToFinishedList = { navigateCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-7bb0cca0-dfc0-4de4-9e4d-0a5fb45ac8c9 연결된 루틴이 표시되어 있어도 완료된 루틴 확인을 실행할 수 있다`() {
        var navigateCount = 0
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = ITEM_TITLE))),
            navigateToFinishedList = { navigateCount += 1 },
        )
        waitForItem()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    // 현재 계정과 연결되지 않았거나 삭제된 태그는 조회되지 않으므로 대상 태그를 조회하지 못한 상태로 둔다.
    @Test
    fun `TC-7bb0cca0-dfc0-4de4-9e4d-0a5fb45ac8c9 TC-f6dd456d-f61f-495f-8b6c-782bf8800ece 대상 태그의 상태와 무관하게 완료된 루틴 확인을 실행할 수 있다`() {
        var navigateCount = 0
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(tagDetailUiState(detail = tagDetail(TAG_TITLE)))
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(uiStateFlow), navigateToRoutineFinishedList = { navigateCount += 1 })
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)

        val uiStateList =
            listOf(
                TagDetailUiState.Loading,
                tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
            )
        uiStateList.forEach { uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        }

        navigateCount shouldBe uiStateList.size
    }

    @Test
    fun `TC-7bb0cca0-dfc0-4de4-9e4d-0a5fb45ac8c9 표시 범위를 모든 하위까지로 넓혀도 완료된 루틴 확인을 실행할 수 있다`() {
        var navigateCount = 0
        setScreen(navigateToFinishedList = { navigateCount += 1 })
        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_DESCENDANT_SCOPE_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        verify { viewModel().select(scope = TagScope.DESCENDANT) }
        navigateCount shouldBe 1
    }

    @Test
    fun `TC-0d8ff25d-6ba0-46de-89a2-f08e4e6c4542 완료된 루틴 확인은 루틴 탭에서만 제공된다`() {
        var navigateCount = 0
        setScreen(navigateToFinishedList = { navigateCount += 1 })

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_DDAY_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            composeRule.selectTagDetailTab(tabDescription)

            composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
        }

        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-cec32aea-92d1-4049-b1f1-95d6c0ddb624 삭제를 실행 취소하면 그 루틴의 삭제 되돌리기를 요청한다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = item.routine.id) }
    }

    @Test
    fun `TC-587c148b-1d6a-4fd5-9636-a72536722eb1 삭제가 저장되지 못해 안내가 오지 않으면 안내와 실행 취소를 표시하지 않는다`() {
        val item = swipeItem()

        swipeCardLeft()

        verify(exactly = 1) { viewModel().delete(id = item.routine.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-bba04000-d3ea-4a13-9e1d-3e9a3443c4fe 안내가 보이는 동안 다른 루틴을 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val item = swipeItem()
        val otherId = Arb.kotlinUuid().next()

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        emitEffect(RoutineListEffect.Deleted(id = otherId))

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().restore(id = otherId) }
        verify(exactly = 0) { viewModel().restore(id = item.routine.id) }
    }

    @Test
    fun `TC-f86aef6f-a3d2-4b36-ac75-89ffe9b0da07 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    @Test
    fun `TC-ea6a1ec1-9ad1-47b3-8582-d176ee28aa18 루틴 탭의 삭제와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val item = tagRoutine(title = ITEM_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(pagingData = tagEntityPagingData(itemList = listOf(item)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)
        waitForItem()

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { viewModel().delete(id = item.routine.id) }
        verify(exactly = 1) { viewModel().restore(id = item.routine.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-ea6a1ec1-9ad1-47b3-8582-d176ee28aa18 루틴 탭의 완료와 실행 취소는 대상 태그와 수정 중인 입력을 바꾸지 않는다`() {
        val item = tagRoutine(title = ITEM_TITLE)
        // 대상 태그의 수정이나 삭제를 요청하면 엄격한 mock이 실패하므로, 요청하지 않았음을 함께 확인한다.
        setScreen(pagingData = tagEntityPagingData(itemList = listOf(item)))
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)
        waitForItem()

        swipeCardRight()
        emitEffect(RoutineListEffect.Finished(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        verify(exactly = 1) { viewModel().finish(id = item.routine.id) }
        verify(exactly = 1) { viewModel().restart(id = item.routine.id) }
        composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-48c421da-50ce-416e-9615-f1eae07a1205 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val item = swipeItem()

        swipeCardLeft()
        emitEffect(RoutineListEffect.Deleted(id = item.routine.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.selectTagDetailTab(DEFAULT_ROUTINE_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel().restore(id = any()) }
    }

    private fun swipeItem(tabDescription: String = DEFAULT_ROUTINE_TAB_DESCRIPTION): RoutineSummary {
        val item = tagRoutine(title = ITEM_TITLE)
        setScreenOnTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            tabDescription = tabDescription,
        )
        waitForItem()

        return item
    }

    private fun swipeCardLeft() {
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun swipeCardRight() {
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
    }

    private fun waitForItem() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitEffect(effect: RoutineListEffect) {
        routinePagingDataFlow.value = tagEntityPagingData(itemList = emptyList())
        routineEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun viewModel(): TagDetailRoutineViewModel = requireNotNull(routineViewModelRef)

    private fun setScreenOnTab(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<RoutineSummary> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        navigateToFinishedList: () -> Unit = {},
        tabDescription: String = DEFAULT_ROUTINE_TAB_DESCRIPTION,
    ) {
        setScreen(
            uiState = uiState,
            pagingData = pagingData,
            navigateToAdd = navigateToAdd,
            navigateToDetail = navigateToDetail,
            navigateToFinishedList = navigateToFinishedList,
        )
        composeRule.selectTagDetailTab(tabDescription)
    }

    private fun setScreen(
        uiState: TagDetailUiState = tagDetailUiState(detail = tagDetail(TAG_TITLE)),
        pagingData: PagingData<RoutineSummary> = tagEntityPagingData(itemList = emptyList()),
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        navigateToFinishedList: () -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            routinePagingData = pagingData,
            navigateToRoutineAdd = navigateToAdd,
            navigateToRoutineDetail = navigateToDetail,
            navigateToRoutineFinishedList = navigateToFinishedList,
        )
    }

    private companion object {
        const val ITEM_TITLE = "TagDetailScreenRoutine"
        const val KOREAN_ROUTINE_TAB_DESCRIPTION = "루틴"
        const val DEFAULT_EMPTY_TITLE = "No routines linked to this tag"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
        const val KOREAN_ADD_BUTTON_DESCRIPTION = "루틴 추가"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val SWIPE_RESET_MILLIS = 2_000L
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished routines"
        const val DEFAULT_SCOPE_BUTTON_DESCRIPTION = "Display scope"
        const val DEFAULT_DESCENDANT_SCOPE_LABEL = "All descendants"
        const val DEFAULT_FINISHED_MESSAGE = "Routine finished."
        const val KOREAN_FINISHED_MESSAGE = "루틴이 완료되었습니다."
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val KOREAN_DELETED_MESSAGE = "루틴이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
    }
}
