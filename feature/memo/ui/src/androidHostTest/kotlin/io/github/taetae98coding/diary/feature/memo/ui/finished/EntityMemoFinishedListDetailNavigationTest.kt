@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListDetailPlaceholder
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListScreen
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListViewModel
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_COPY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
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
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
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

// 항목 완료 메모 목록의 목록 화면은 각 항목 기능 모듈의 internal entry라 이 모듈에서 쓸 수 없으므로,
// 그 entry와 같은 공용 화면과 pane 구성을 대역 entry로 두고 메모 상세는 제품 memoEntry로 띄운다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class EntityMemoFinishedListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val finishedPagingData = MutableStateFlow(memoPagingDataOf(itemList = emptyList()))

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
    fun `TC-170510aa-f75c-496d-a57a-c3732a48e734 ContactMemoFinishedList 상세 영역에서 메모를 삭제하면 목록은 유지되고 선택 전 상태가 된다`() {
        val contactId = fixtureMonkey.giveMeOne<Uuid>()

        assertDeleteReturnsToPlaceholder(listOf(ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId)))
    }

    @Test
    fun `TC-170510aa-f75c-496d-a57a-c3732a48e734 PlaceMemoFinishedList 상세 영역에서 메모를 삭제하면 목록은 유지되고 선택 전 상태가 된다`() {
        val placeId = fixtureMonkey.giveMeOne<Uuid>()

        assertDeleteReturnsToPlaceholder(listOf(PlaceDetailNavKey(id = placeId), PlaceMemoFinishedListNavKey(placeId = placeId)))
    }

    @Test
    fun `TC-170510aa-f75c-496d-a57a-c3732a48e734 WebMemoFinishedList 상세 영역에서 메모를 삭제하면 목록은 유지되고 선택 전 상태가 된다`() {
        val webId = fixtureMonkey.giveMeOne<Uuid>()

        assertDeleteReturnsToPlaceholder(listOf(WebDetailNavKey(id = webId), WebMemoFinishedListNavKey(webId = webId)))
    }

    @Test
    fun `TC-170510aa-f75c-496d-a57a-c3732a48e734 목록에서 다른 메모를 고른 뒤 삭제하면 목록은 유지되고 선택 전 상태가 된다`() {
        val contactId = fixtureMonkey.giveMeOne<Uuid>()
        val listBackStack = listOf(ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId))
        val memoList = memoList()
        val backStack = setNavDisplay(listBackStack = listBackStack, memoList = memoList, copiedId = fixtureMonkey.giveMeOne<Uuid>())
        selectMemo(memoList[0])
        selectMemo(memoList[1])

        deleteOnDetail()

        assertPlaceholderWithList(backStack = backStack, listBackStack = listBackStack)
    }

    @Test
    fun `TC-170510aa-f75c-496d-a57a-c3732a48e734 복사한 메모 상세에서 삭제하면 목록은 유지되고 선택 전 상태가 된다`() {
        val contactId = fixtureMonkey.giveMeOne<Uuid>()
        val listBackStack = listOf(ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId))
        val memoList = memoList()
        val copiedId = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = setNavDisplay(listBackStack = listBackStack, memoList = memoList, copiedId = copiedId)
        selectMemo(memoList[0])
        composeRule.onNode(actionButton(DEFAULT_COPY_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe MemoDetailNavKey(id = copiedId)

        deleteOnDetail()

        assertPlaceholderWithList(backStack = backStack, listBackStack = listBackStack)
    }

    private fun assertDeleteReturnsToPlaceholder(listBackStack: List<ScreenNavKey>) {
        val memoList = memoList()
        val backStack = setNavDisplay(listBackStack = listBackStack, memoList = memoList, copiedId = fixtureMonkey.giveMeOne<Uuid>())
        selectMemo(memoList[0])

        deleteOnDetail()

        assertPlaceholderWithList(backStack = backStack, listBackStack = listBackStack)
    }

    private fun assertPlaceholderWithList(
        backStack: NavBackStack<ScreenNavKey>,
        listBackStack: List<ScreenNavKey>,
    ) {
        backStack.toList() shouldBe listOf(EntityFinishedListTestTopLevelNavKey) + listBackStack
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

    private fun setNavDisplay(
        listBackStack: List<ScreenNavKey>,
        memoList: List<Memo>,
        copiedId: Uuid,
    ): NavBackStack<ScreenNavKey> {
        val backStack = NavBackStack<ScreenNavKey>(EntityFinishedListTestTopLevelNavKey, *listBackStack.toTypedArray())
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
                                        entry<EntityFinishedListTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        entry<ContactDetailNavKey> { Text(text = ENTITY_DETAIL_CONTENT) }
                                        entry<PlaceDetailNavKey> { Text(text = ENTITY_DETAIL_CONTENT) }
                                        entry<WebDetailNavKey> { Text(text = ENTITY_DETAIL_CONTENT) }
                                        entityMemoFinishedListEntry<ContactMemoFinishedListNavKey>(backStack = backStack)
                                        entityMemoFinishedListEntry<PlaceMemoFinishedListNavKey>(backStack = backStack)
                                        entityMemoFinishedListEntry<WebMemoFinishedListNavKey>(backStack = backStack)
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

        return backStack
    }

    private inline fun <reified T : ScreenNavKey> EntryProviderScope<ScreenNavKey>.entityMemoFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
        entry<T>(
            metadata = { key ->
                ListDetailSceneStrategy.listPane(
                    sceneKey = key,
                    detailPlaceholder = { EntityMemoFinishedListDetailPlaceholder() },
                ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
            },
        ) {
            EntityMemoFinishedListScreen(
                navigateUp = {},
                navigateToMemoDetail = backStack::navigateToMemoDetail,
                emptyTitle = EMPTY_TITLE,
                memoViewModel = entityMemoFinishedListViewModel(),
                syncViewModel = screenTestSyncViewModel(),
            )
        }
    }

    private fun entityMemoFinishedListViewModel(): EntityMemoFinishedListViewModel =
        mockk<EntityMemoFinishedListViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(EntityMemoFinishedListUiState())
            every { sortUiState } returns MutableStateFlow(ListSortUiState())
            every { memoPagingData } returns finishedPagingData
            every { effect } returns emptyFlow()
        }

    private fun viewModelModule(copiedId: Uuid): Module =
        module {
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

    private fun detailViewModel(
        id: Uuid,
        copiedId: Uuid,
    ): MemoDetailViewModel {
        val uiState: MutableStateFlow<MemoDetailUiState> =
            MutableStateFlow(memoDetailUiState(id = id, detail = memoDetail(title = detailTitle(id = id)), isFinished = true))
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<MemoDetailViewModel>(relaxed = true) {
            every { this@mockk.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { delete() } answers { effectChannel.trySend(MemoDetailEffect.DeleteSucceeded).getOrThrow() }
            every { copy() } answers { effectChannel.trySend(MemoDetailEffect.CopySucceeded(id = copiedId)).getOrThrow() }
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val MEMO_COUNT = 3
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val ENTITY_DETAIL_CONTENT = "EntityDetailContent"
        const val EMPTY_TITLE = "EntityMemoFinishedListEmpty"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a memo"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun listTitle(index: Int): String = "EntityMemoFinishedListDetailTitle$index"

        fun detailTitle(id: Uuid): String = "EntityMemoFinishedListDetailScreen$id"

        fun actionButton(contentDescription: String): SemanticsMatcher = hasContentDescription(contentDescription) and hasClickAction()

        fun memoList(): List<Memo> =
            List(MEMO_COUNT) { index ->
                fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null).let { memo ->
                    memo.copy(detail = memo.detail.copy(title = listTitle(index = index), dateTime = null))
                }
            }
    }
}

// 항목 상세로 들어오기 전의 주요 목적지를 대신한다.
private data object EntityFinishedListTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "EntityFinishedListTestTopLevel"
}
