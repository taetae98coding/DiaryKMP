@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_COPY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_FINISH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_RESTART_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailEffect
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailUiState
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.memoDetail
import io.github.taetae98coding.diary.feature.memo.ui.detail.memoDetailUiState
import io.github.taetae98coding.diary.feature.memo.ui.detail.screenTestTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

// 두 영역을 함께 표시하는 너비에서 제품 memoEntry로 완료된 메모 목록과 메모 상세를 띄우고, 상세 화면의 동작이 배치에 남기는 결과를 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class MemoFinishedListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(FinishedListTestTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey)
    private var finishedMemoList: List<Memo> = emptyList()
    private val finishedPagingData = MutableStateFlow(memoPagingDataOf(itemList = emptyList()))
    private val detailUiStateMap = mutableMapOf<Uuid, MutableStateFlow<MemoDetailUiState>>()

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
    fun `TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-015 목록에서 고른 메모 상세에서 삭제하면 선택 전 상태로 돌아간다`() {
        val memoList = memoList()
        setMemoNavDisplay(memoList = memoList)
        selectMemo(memoList[0])

        deleteOnDetail()

        assertPlaceholderWithList()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-015 목록에서 다른 메모를 고른 뒤 삭제하면 선택 전 상태로 돌아간다`() {
        val memoList = memoList()
        setMemoNavDisplay(memoList = memoList)
        selectMemo(memoList[0])
        selectMemo(memoList[1])

        deleteOnDetail()

        assertPlaceholderWithList()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-015 복사한 메모 상세에서 삭제하면 선택 전 상태로 돌아간다`() {
        val memoList = memoList()
        val copiedId = fixtureMonkey.giveMeOne<Uuid>()
        setMemoNavDisplay(memoList = memoList, copiedId = copiedId)
        selectMemo(memoList[0])
        composeRule.onNode(actionButton(DEFAULT_COPY_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe MemoDetailNavKey(id = copiedId)

        deleteOnDetail()

        assertPlaceholderWithList()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-016 상세 영역에서 메모를 다시 시작하면 목록에서 사라지고 상세는 그대로 남는다`() {
        val memoList = memoList()
        setMemoNavDisplay(memoList = memoList)
        selectMemo(memoList[0])

        composeRule.onNode(actionButton(DEFAULT_RESTART_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe MemoDetailNavKey(id = memoList[0].id)
        composeRule.onAllNodesWithText(listTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true
        composeRule.onNodeWithText(listTitle(index = 1)).assertExists()
        composeRule.onAllNodesWithText(detailTitle(id = memoList[0].id)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onNode(actionButton(DEFAULT_FINISH_BUTTON_DESCRIPTION)).assertExists()
    }

    private fun assertPlaceholderWithList() {
        backStack.toList() shouldBe listOf(FinishedListTestTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey)
        composeRule.onNodeWithText(listTitle(index = 0)).assertExists()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertExists()
    }

    private fun selectMemo(memo: Memo) {
        composeRule.onNodeWithText(memo.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(detailTitle(id = memo.id)).fetchSemanticsNodes().isNotEmpty() shouldBe true
    }

    private fun deleteOnDetail() {
        composeRule.onNode(actionButton(DEFAULT_DELETE_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
    }

    private fun setMemoNavDisplay(
        memoList: List<Memo>,
        copiedId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
    ) {
        finishedMemoList = memoList
        finishedPagingData.value = memoPagingDataOf(itemList = memoList.map { memo -> MemoListItem.Content(memo = memo) })
        val viewModelModule = viewModelModule(copiedId = copiedId)

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
                                        entry<FinishedListTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(listTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun viewModelModule(copiedId: Uuid): Module =
        module {
            factory<MemoFinishedListViewModel> {
                mockk<MemoFinishedListViewModel>(relaxed = true) {
                    every { sortUiState } returns MutableStateFlow(ListSortUiState())
                    every { memoPagingData } returns finishedPagingData
                    every { effect } returns emptyFlow()
                }
            }
            factory<SyncRefreshViewModel> { screenTestSyncViewModel() }
            factory<MemoDetailViewModel> { parameters -> detailViewModel(id = parameters.get<Uuid>(), copiedId = copiedId) }
            factory<MemoTagViewModel> { screenTestTagViewModel() }
            factory<MemoWebViewModel> { screenTestWebViewModel() }
            factory<MemoContactViewModel> { screenTestContactViewModel() }
            factory<MemoPlaceViewModel> { screenTestPlaceViewModel() }
            // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 mock을 쓴다.
            factory<MemoPlaceMapViewModel> {
                mockk<MemoPlaceMapViewModel>(relaxed = true) { every { uiState } returns MutableStateFlow(MemoPlaceMapUiState.Loading) }
            }
            factory<MemoGeminiViewModel> {
                mockk<MemoGeminiViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(MemoGeminiUiState())
                    every { effect } returns emptyFlow()
                }
            }
        }

    private fun detailViewModel(
        id: Uuid,
        copiedId: Uuid,
    ): MemoDetailViewModel {
        val uiState =
            detailUiStateMap.getOrPut(id) {
                MutableStateFlow(memoDetailUiState(id = id, detail = memoDetail(title = detailTitle(id = id)), isFinished = true))
            }
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<MemoDetailViewModel>(relaxed = true) {
            every { this@mockk.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { delete() } answers { effectChannel.trySend(MemoDetailEffect.DeleteSucceeded).getOrThrow() }
            every { copy() } answers { effectChannel.trySend(MemoDetailEffect.CopySucceeded(id = copiedId)).getOrThrow() }
            // 다시 시작이 저장되면 완료된 메모 목록이 다시 조회되는 것과 같게 목록 조회 결과에서 그 메모를 뺀다.
            every { restart() } answers {
                uiState.value = memoDetailUiState(id = id, detail = memoDetail(title = detailTitle(id = id)), isFinished = false)
                finishedMemoList = finishedMemoList.filterNot { memo -> memo.id == id }
                finishedPagingData.value = memoPagingDataOf(itemList = finishedMemoList.map { memo -> MemoListItem.Content(memo = memo) })
            }
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val MEMO_COUNT = 3
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a memo"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun listTitle(index: Int): String = "MemoFinishedListDetailTitle$index"

        fun detailTitle(id: Uuid): String = "MemoFinishedListDetailScreen$id"

        fun actionButton(contentDescription: String): SemanticsMatcher = hasContentDescription(contentDescription) and hasClickAction()

        fun memoList(): List<Memo> =
            List(MEMO_COUNT) { index ->
                fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null).let { memo ->
                    memo.copy(detail = memo.detail.copy(title = listTitle(index = index), dateTime = null))
                }
            }
    }
}

// 캘린더 홈처럼 메모가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object FinishedListTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "FinishedListTestTopLevel"
}
