@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.colorHexText
import io.github.taetae98coding.diary.feature.memo.ui.add.screenTestRealViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_COPY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_RESTART_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_UPDATE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailEffect
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailUiState
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.memoDetail
import io.github.taetae98coding.diary.feature.memo.ui.detail.memoDetailUiState
import io.github.taetae98coding.diary.feature.memo.ui.detail.screenTestTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.finished.MemoFinishedListViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeViewModel
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.home.screenTestSyncViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 제품 memoEntry로 메모 화면을 띄우고, 메모 상세와 메모 추가의 동작이 전환 이력과 화면에 남기는 결과를 본다.
// 다른 기능 모듈의 진입 화면은 이 모듈의 의존이 아니므로 그 화면의 키에 대역 화면을 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoEntryNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey)
    private val homeMemo: Memo = fixtureMonkey.memo(title = HOME_MEMO_TITLE)
    private val storedHomeMemoList = MutableStateFlow(listOf(homeMemo))
    private var finishedMemoList: List<Memo> = emptyList()
    private val finishedPagingData = MutableStateFlow(memoPagingDataOf(itemList = emptyList()))
    private val detailContentMap = mutableMapOf<Uuid, DetailContent>()
    private var copiedId: Uuid = fixtureMonkey.giveMeOne<Uuid>()

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트 앞뒤로 전역 Koin을 정리한다.
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
    fun `TC-527627f5-3cbc-4326-b0d3-62be6c057fea 복사에 성공하면 상세 대상이 복사본 메모의 저장된 내용으로 바뀐다`() {
        val originalId = fixtureMonkey.giveMeOne<Uuid>()
        val original = detailContent(prefix = ORIGINAL_PREFIX, color = ORIGINAL_COLOR, dateTime = null)
        val copied = detailContent(prefix = COPIED_PREFIX, color = COPIED_COLOR, dateTime = allDayDateTime())
        detailContentMap[originalId] = original
        detailContentMap[copiedId] = copied
        backStack.replaceAll(listOf(MemoHomeNavKey, MemoDetailNavKey(id = originalId)))
        setMemoNavDisplay()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(original.title))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNode(actionButton(DEFAULT_COPY_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoDetailNavKey(id = copiedId))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(copied.title))
        composeRule.onAllNodesWithText(copied.title).assertCountEquals(2)
        composeRule.onAllNodesWithText(original.title + EDIT_SUFFIX).assertCountEquals(0)
        composeRule.onAllNodesWithText(original.title).assertCountEquals(0)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(copied.description).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onAllNodesWithText(original.description).assertCountEquals(0)
        composeRule.colorHexText() shouldBe COPIED_COLOR_HEX
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().assertIsOn()
        composeRule.onNodeWithText(ALL_DAY_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
        copied.selectedTitleList.forEach { title -> composeRule.onAllNodesWithText(title, substring = true).onFirst().assertExists() }
        original.selectedTitleList.forEach { title -> composeRule.onAllNodesWithText(title, substring = true).assertCountEquals(0) }
    }

    @Test
    fun `TC-ac9ee7e5-8624-4ec6-a6ec-17dfb844fa00 뒤로가기를 사용하면 MemoDetail 화면을 벗어나 진입 화면으로 돌아간다`() {
        setMemoNavDisplay()

        entryKeyList().forEach { entryKey ->
            listOf(::clickNavigateUp, ::pressBack).forEach { navigateBack ->
                val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
                showDetail(entryKey = entryKey, detailKey = detailKey)

                navigateBack()

                assertEntryShown(entryKey = entryKey, leftDetailId = detailKey.id)
            }
        }
    }

    @Test
    fun `TC-231b6a8e-83cc-4cf4-8c2d-558f5715280a 메모를 삭제하면 MemoDetail 화면이 닫히고 진입하기 전 화면이 표시된다`() {
        setMemoNavDisplay()

        entryKeyList().forEach { entryKey ->
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            showDetail(entryKey = entryKey, detailKey = detailKey)

            composeRule.onNode(actionButton(DEFAULT_DELETE_BUTTON_DESCRIPTION)).performClick()
            composeRule.waitForIdle()

            assertEntryShown(entryKey = entryKey, leftDetailId = detailKey.id)
        }
    }

    @Test
    fun `TC-a6baf1a0-fcf7-4d4c-beb0-649aeca097ca 뒤로가기를 사용하면 MemoAdd 화면이 닫히고 진입하기 전 화면이 표시된다`() {
        setMemoNavDisplay()

        listOf(MemoHomeNavKey, TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())).forEach { entryKey ->
            listOf(::clickNavigateUp, ::pressBack).forEach { navigateBack ->
                composeRule.runOnIdle { backStack.replaceAll(listOf(entryKey, MemoAddNavKey())) }
                composeRule.waitForIdle()
                composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsDisplayed()

                navigateBack()

                backStack.toList() shouldBe listOf(entryKey)
                composeRule.onNodeWithText(entryContent(entryKey)).assertIsDisplayed()
                composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
            }
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-e183d38a-ea59-4d68-a14a-cfa4320aef19 상세 영역에서 메모를 삭제하면 목록은 유지되고 그 전에 놓였던 메모 추가가 상세 영역에 놓인다`() {
        backStack.replaceAll(listOf(MemoHomeNavKey, MemoAddNavKey()))
        setMemoNavDisplay()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(ADD_INPUT_TITLE)
        val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        composeRule.runOnIdle { backStack.add(detailKey) }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(detailContentOf(detailKey.id).title).onFirst().assertExists()

        composeRule.onNode(actionButton(DEFAULT_DELETE_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoAddNavKey())
        composeRule.onNodeWithText(HOME_MEMO_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithText(detailContentOf(detailKey.id).title).assertCountEquals(0)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(ADD_INPUT_TITLE))
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-ac9ee7e5-8624-4ec6-a6ec-17dfb844fa00 TC-231b6a8e-83cc-4cf4-8c2d-558f5715280a 목록과 함께 표시된 상세 영역에서 뒤로가거나 삭제하면 목록을 유지하고 선택한 메모가 없을 때의 상태로 돌아간다`() {
        setMemoNavDisplay()

        listOf(::pressBack, ::deleteOnDetail).forEach { leaveDetail ->
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            composeRule.runOnIdle { backStack.replaceAll(listOf(MemoHomeNavKey, detailKey)) }
            composeRule.waitForIdle()
            composeRule.onAllNodesWithText(detailContentOf(detailKey.id).title).onFirst().assertExists()

            leaveDetail()

            backStack.toList() shouldBe listOf(MemoHomeNavKey)
            composeRule.onNodeWithText(HOME_MEMO_TITLE).assertIsDisplayed()
            composeRule.onAllNodesWithText(detailContentOf(detailKey.id).title).assertCountEquals(0)
            composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsDisplayed()
            composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        }
    }

    @Test
    @Config(qualifiers = "w1280dp-h1600dp")
    fun `TC-a81448da-d577-46bf-aeaa-178e5da430c4 목록과 함께 보는 상세 영역에서 메모 제목을 고치면 목록에 바로 반영된다`() {
        detailContentMap[homeMemo.id] = detailContent(prefix = HOME_MEMO_TITLE, color = ORIGINAL_COLOR, dateTime = null).copy(title = HOME_MEMO_TITLE)
        backStack.replaceAll(listOf(MemoHomeNavKey, MemoDetailNavKey(id = homeMemo.id)))
        setMemoNavDisplay()
        // 목록의 카드, 상세 영역의 상단 바와 제목 입력 칸에 같은 제목이 보인다.
        composeRule.onAllNodesWithText(HOME_MEMO_TITLE).assertCountEquals(3)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onNode(actionButton(DEFAULT_UPDATE_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(HOME_MEMO_TITLE).fetchSemanticsNodes().isEmpty()
        }

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoDetailNavKey(id = homeMemo.id))
        composeRule.onAllNodesWithText(HOME_MEMO_TITLE + EDIT_SUFFIX).assertCountEquals(3)
    }

    @Test
    fun `TC-88767c4e-e929-48a6-9ef7-195dce9b2e0b 상세 화면에서 다시 시작하거나 삭제한 뒤 돌아오면 그 메모가 완료된 메모 목록에서 사라진다`() {
        finishedMemoList = List(FINISHED_MEMO_COUNT) { index -> fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null).withTitle(finishedTitle(index)) }
        finishedPagingData.value = finishedMemoList.toPagingData()
        backStack.replaceAll(listOf(MemoHomeNavKey, MemoFinishedListNavKey))
        setMemoNavDisplay()
        val restartedMemo = finishedMemoList[0]
        val deletedMemo = finishedMemoList[1]
        val remainedMemo = finishedMemoList[2]

        listOf(restartedMemo to ::restartAndNavigateUp, deletedMemo to ::deleteOnDetail).forEach { (memo, leaveAfterAction) ->
            composeRule.onNodeWithText(memo.detail.title).performClick()
            composeRule.waitForIdle()
            backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoFinishedListNavKey, MemoDetailNavKey(id = memo.id))
            composeRule.onAllNodesWithText(memo.detail.title).assertCountEquals(0)

            leaveAfterAction()

            backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoFinishedListNavKey)
            composeRule.onAllNodesWithText(memo.detail.title).assertCountEquals(0)
            composeRule.onNodeWithText(remainedMemo.detail.title).assertIsDisplayed()
        }
    }

    private fun showDetail(
        entryKey: ScreenNavKey,
        detailKey: MemoDetailNavKey,
    ) {
        composeRule.runOnIdle { backStack.replaceAll(listOf(entryKey, detailKey)) }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(detailContentOf(detailKey.id).title).onFirst().assertIsDisplayed()
    }

    private fun assertEntryShown(
        entryKey: ScreenNavKey,
        leftDetailId: Uuid,
    ) {
        backStack.toList() shouldBe listOf(entryKey)
        composeRule.onNodeWithText(entryContent(entryKey)).assertIsDisplayed()
        composeRule.onAllNodesWithText(detailContentOf(leftDetailId).title).assertCountEquals(0)
    }

    private fun clickNavigateUp() {
        composeRule.onNode(actionButton(DEFAULT_NAVIGATE_UP_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun deleteOnDetail() {
        composeRule.onNode(actionButton(DEFAULT_DELETE_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
    }

    private fun restartAndNavigateUp() {
        composeRule.onNode(actionButton(DEFAULT_RESTART_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
        clickNavigateUp()
    }

    // 다시 시작이나 삭제가 저장되면 완료된 메모 목록이 다시 조회되는 것과 같게 목록 조회 결과에서 그 메모를 뺀다.
    private fun removeFromFinishedList(id: Uuid) {
        finishedMemoList = finishedMemoList.filterNot { memo -> memo.id == id }
        finishedPagingData.value = finishedMemoList.toPagingData()
    }

    // 수정이 저장되면 메모 목록이 다시 조회되는 것과 같게 목록 조회 결과의 그 메모를 바꾼다.
    private fun updateStoredHomeMemo(
        id: Uuid,
        detail: MemoDetail,
    ) {
        storedHomeMemoList.value = storedHomeMemoList.value.map { memo -> if (memo.id == id) memo.copy(detail = memo.detail.copy(title = detail.title)) else memo }
    }

    private fun NavBackStack<ScreenNavKey>.replaceAll(keyList: List<ScreenNavKey>) {
        clear()
        addAll(keyList)
    }

    private fun detailContentOf(id: Uuid): DetailContent = detailContentMap.getOrPut(id) { detailContent(prefix = "Detail${detailContentMap.size}", color = ORIGINAL_COLOR, dateTime = null) }

    private fun viewModelModule(): Module =
        module {
            factory<MemoHomeViewModel> { memoHomeViewModel() }
            factory<MemoFinishedListViewModel> {
                mockk<MemoFinishedListViewModel>(relaxed = true) {
                    every { sortUiState } returns MutableStateFlow(ListSortUiState())
                    every { memoPagingData } returns finishedPagingData
                    every { effect } returns emptyFlow()
                }
            }
            factory<SyncRefreshViewModel> { screenTestSyncViewModel() }
            factory<MemoDetailViewModel> { parameters -> detailViewModel(id = parameters.get<Uuid>()) }
            factory<MemoTagViewModel> { parameters ->
                screenTestTagViewModel(uiState = MutableStateFlow(MemoTagInputUiState(selectedTagList = listOf(detailContentOf(parameters.get<Uuid>()).tag))))
            }
            factory<MemoWebViewModel> { parameters ->
                screenTestWebViewModel(uiState = MutableStateFlow(MemoWebInputUiState(selectedWebList = listOf(detailContentOf(parameters.get<Uuid>()).web))))
            }
            factory<MemoContactViewModel> { parameters ->
                screenTestContactViewModel(uiState = MutableStateFlow(MemoContactInputUiState(selectedContactList = listOf(detailContentOf(parameters.get<Uuid>()).contact))))
            }
            factory<MemoPlaceViewModel> { parameters ->
                screenTestPlaceViewModel(
                    uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(detailContentOf(parameters.get<Uuid>()).place))),
                )
            }
            factory<MemoAddViewModel> { screenTestRealViewModel().viewModel }
            factory<MemoAddTagViewModel> { screenTestRealViewModel().tagViewModel }
            factory<MemoAddWebViewModel> { screenTestRealViewModel().webViewModel }
            factory<MemoAddContactViewModel> { screenTestRealViewModel().contactViewModel }
            factory<MemoAddPlaceViewModel> { screenTestRealViewModel().placeViewModel }
            // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 mock을 쓴다.
            factory<MemoPlaceMapViewModel> {
                mockk<MemoPlaceMapViewModel>(relaxed = true) { every { uiState } returns MutableStateFlow(MemoPlaceMapUiState.Loading) }
            }
            factory<MemoPlacePickerMapViewModel> {
                mockk<MemoPlacePickerMapViewModel>(relaxed = true) { every { uiState } returns MutableStateFlow(MemoPlacePickerMapUiState()) }
            }
            factory<MemoGeminiViewModel> {
                mockk<MemoGeminiViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(MemoGeminiUiState())
                    every { effect } returns emptyFlow()
                }
            }
        }

    private fun setMemoNavDisplay() {
        val viewModelModule = viewModelModule()

        composeRule.setContent {
            // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }
            val resultEventBus = remember { ResultEventBus() }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalResultEventBus provides resultEventBus,
            ) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    DiaryTheme {
                        val holder = rememberListDetailPlaceholderStateHolder()

                        CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                            NavDisplay(
                                backStack = backStack,
                                sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                                entryDecorators =
                                    listOf(
                                        rememberSaveableStateHolderNavEntryDecorator(),
                                        rememberViewModelStoreNavEntryDecorator(),
                                        rememberListDetailPlaceholderNavEntryDecorator(holder),
                                    ),
                                entryProvider =
                                    entryProvider {
                                        entry<TagDetailNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<TagMemoFinishedListNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<ContactDetailNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<ContactMemoFinishedListNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<PlaceDetailNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<PlaceMemoFinishedListNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<WebDetailNavKey> { key -> Text(text = entryContent(key)) }
                                        entry<WebMemoFinishedListNavKey> { key -> Text(text = entryContent(key)) }
                                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun memoHomeViewModel(): MemoHomeViewModel {
        val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
        every { getMemoFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val getMemoFilterTagIdUseCase = mockk<GetMemoFilterTagIdUseCase>()
        every { getMemoFilterTagIdUseCase(parameter = Unit) } returns flowOf(Result.success(emptySet()))
        val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
        every { getMemoExistenceFilterUseCase(parameter = Unit) } returns flowOf(Result.success(MemoExistenceFilter()))
        val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
        every { pageMemoHomeUseCase(parameter = any()) } returns storedHomeMemoList.map { memoList -> Result.success(PagingData.from(memoList)) }

        return MemoHomeViewModel(
            getMemoFilterUseCase = getMemoFilterUseCase,
            getMemoFilterTagIdUseCase = getMemoFilterTagIdUseCase,
            getMemoExistenceFilterUseCase = getMemoExistenceFilterUseCase,
            pageMemoHomeUseCase = pageMemoHomeUseCase,
            finishMemoUseCase = mockk(),
            restartMemoUseCase = mockk(),
            deleteMemoUseCase = mockk(),
            restoreMemoUseCase = mockk(),
        )
    }

    private fun detailViewModel(id: Uuid): MemoDetailViewModel {
        val content = detailContentOf(id)
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val uiState: MutableStateFlow<MemoDetailUiState> =
            MutableStateFlow(
                memoDetailUiState(
                    id = id,
                    detail = memoDetail(title = content.title).copy(description = content.description, color = content.color, dateTime = content.dateTime),
                    isFinished = finishedMemoList.any { memo -> memo.id == id },
                ),
            )

        return mockk<MemoDetailViewModel>(relaxed = true) {
            every { this@mockk.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { delete() } answers {
                removeFromFinishedList(id)
                effectChannel.trySend(MemoDetailEffect.DeleteSucceeded).getOrThrow()
            }
            every { restart() } answers {
                removeFromFinishedList(id)
                uiState.value = (uiState.value as MemoDetailUiState.Content).copy(isFinished = false)
            }
            every { update(any()) } answers {
                val detail = firstArg<MemoDetail>()
                updateStoredHomeMemo(id = id, detail = detail)
                uiState.value = (uiState.value as MemoDetailUiState.Content).copy(detail = detail)
            }
            every { copy() } answers { effectChannel.trySend(MemoDetailEffect.CopySucceeded(id = copiedId)).getOrThrow() }
        }
    }

    private data class DetailContent(
        val title: String,
        val description: String,
        val color: Long,
        val dateTime: MemoDateTime?,
        val tag: Tag,
        val web: Web,
        val contact: Contact,
        val place: Place,
    ) {
        val selectedTitleList: List<String>
            get() = listOf(tag.detail.title, web.detail.title, contact.detail.name, place.detail.title)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val FINISHED_MEMO_COUNT = 3
        const val HOME_MEMO_TITLE = "MemoEntryHomeMemo"
        const val ADD_INPUT_TITLE = "MemoEntryAddInput"
        const val ORIGINAL_PREFIX = "Original"
        const val COPIED_PREFIX = "Copied"
        const val EDIT_SUFFIX = "Edited"
        const val ORIGINAL_COLOR = 0xFFE57373
        const val COPIED_COLOR = 0xFF3A7BD5
        const val COPIED_COLOR_HEX = "#3A7BD5"
        const val ALL_DAY_START_DATE_TEXT = "Jul 19, 2026"
        const val ALL_DAY_END_DATE_TEXT = "Jul 28, 2026"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun entryKeyList(): List<ScreenNavKey> {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()

            return listOf(
                MemoHomeNavKey,
                TagDetailNavKey(id = tagId),
                TagMemoFinishedListNavKey(tagId = tagId),
                ContactDetailNavKey(id = contactId),
                PlaceDetailNavKey(id = placeId),
                WebDetailNavKey(id = webId),
                ContactMemoFinishedListNavKey(contactId = contactId),
                PlaceMemoFinishedListNavKey(placeId = placeId),
                WebMemoFinishedListNavKey(webId = webId),
            )
        }

        fun finishedTitle(index: Int): String = "MemoEntryFinished$index"

        fun Memo.withTitle(title: String): Memo = copy(detail = detail.copy(title = title, dateTime = null))

        fun List<Memo>.toPagingData(): PagingData<MemoListItem> = memoPagingDataOf(itemList = map { memo -> MemoListItem.Content(memo = memo) })

        fun entryContent(key: ScreenNavKey): String = if (key == MemoHomeNavKey) HOME_MEMO_TITLE else "EntryContent-$key"

        fun detailContent(
            prefix: String,
            color: Long,
            dateTime: MemoDateTime?,
        ): DetailContent =
            DetailContent(
                title = "$prefix-title",
                description = "$prefix-description",
                color = color,
                dateTime = dateTime,
                tag = testTag(title = "$prefix-tag"),
                web = testWeb(title = "$prefix-web"),
                contact = testContact(name = "$prefix-contact"),
                place = testPlace(title = "$prefix-place"),
            )

        fun allDayDateTime(): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 28))

        fun actionButton(contentDescription: String): SemanticsMatcher = hasContentDescription(contentDescription) and hasClickAction()

        fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}
