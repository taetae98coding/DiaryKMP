package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val DEFAULT_DETAIL_TAB_DESCRIPTION = "Contact detail"
internal const val DEFAULT_MEMO_TAB_DESCRIPTION = "Memo"
internal const val KOREAN_DETAIL_TAB_DESCRIPTION = "연락처 디테일"
internal const val KOREAN_MEMO_TAB_DESCRIPTION = "메모"
internal const val DEFAULT_MEMO_ADD_DESCRIPTION = "Add memo"
internal const val KOREAN_MEMO_ADD_DESCRIPTION = "메모 추가"
internal const val DEFAULT_UPDATE_DESCRIPTION = "Update contact"
internal val FIRST_CONTACT_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

private const val EFFECT_BUFFER_CAPACITY = 8

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// KoinApplication에 넘긴 모듈은 첫 테스트의 것이 이어서 쓰이므로, 메모 탭이 얻는 값은 모듈이 붙잡는 이 흐름들로 테스트마다 바꾼다.
internal val memoPagingDataFlow = MutableStateFlow(PagingData.empty<MemoListItem>())
internal val memoListUiStateFlow = MutableStateFlow(MemoListUiState())
internal val memoEffectFlow = MutableSharedFlow<MemoListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)

internal var memoViewModelRef: ContactDetailMemoViewModel? = null
    private set

internal var memoSyncViewModelRef: ContactDetailMemoSyncViewModel? = null
    private set

private val contactDetailTabViewModelModule =
    module {
        factory {
            mockk<ContactDetailMemoViewModel>(relaxed = true)
                .apply {
                    every { memoPagingData } returns memoPagingDataFlow
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { effect } returns memoEffectFlow
                }.also { memoViewModelRef = it }
        }
        factory {
            mockk<ContactDetailMemoSyncViewModel>(relaxed = true)
                .apply { every { uiState } returns memoListUiStateFlow }
                .also { memoSyncViewModelRef = it }
        }
    }

internal fun prepareContactDetailTabViewModels(
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
) {
    memoPagingDataFlow.value = memoPagingData
    memoListUiStateFlow.value = memoListUiState
    memoViewModelRef = null
    memoSyncViewModelRef = null
}

@Composable
internal fun ContactDetailScreenTestHost(content: @Composable () -> Unit) {
    // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로,
    // 테스트마다 새 소유자를 제공해 이전 테스트의 탭별 ViewModel이 재사용되지 않게 한다.
    val viewModelStoreOwner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        KoinApplication(configuration = koinConfiguration { modules(contactDetailTabViewModelModule) }) {
            DiaryTheme(content = content)
        }
    }
}

internal fun screenTestViewModel(
    uiState: StateFlow<ContactDetailUiState> = MutableStateFlow(ContactDetailUiState.Loading),
    effect: Flow<ContactDetailEffect> = emptyFlow(),
): ContactDetailViewModel {
    val viewModel = mockk<ContactDetailViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    justRun { viewModel.update(detail = any<ContactDetail>()) }
    justRun { viewModel.toggleFavorite() }
    justRun { viewModel.delete() }
    return viewModel
}

internal fun ComposeContentTestRule.setContactDetailScreen(
    viewModel: ContactDetailViewModel,
    id: Uuid = FIRST_CONTACT_ID,
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
    componentVisible: ContactDetailScaffoldComponentVisible = ContactDetailScaffoldComponentVisible(),
    navigateUp: () -> Unit = {},
    navigateToMemoAdd: () -> Unit = {},
    navigateToMemoDetail: (Uuid) -> Unit = {},
) {
    prepareContactDetailTabViewModels(memoPagingData = memoPagingData, memoListUiState = memoListUiState)

    setContent {
        ContactDetailScreenTestHost {
            ContactDetailScreen(
                navigateUp = navigateUp,
                navigateToMemoAdd = navigateToMemoAdd,
                navigateToMemoDetail = navigateToMemoDetail,
                id = id,
                componentVisibleProvider = { componentVisible },
                viewModel = viewModel,
            )
        }
    }
    waitForIdle()
}

internal fun ComposeContentTestRule.selectContactDetailTab(contentDescription: String) {
    onNodeWithContentDescription(contentDescription).performClick()
    waitForIdle()
}

/**
 * Scaffold는 떠 있는 버튼과 탭 내용을 슬롯으로 받으므로, Screen이 하는 조립을 그대로 두어 탭과 버튼의 표현을 함께 검증한다.
 */
@Composable
internal fun ContactDetailTestScaffold(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactDetailFormState(),
    tabState: ContactDetailTabState = rememberContactDetailTabState(),
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible = { ContactDetailScaffoldComponentVisible() },
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    onMemoAdd: () -> Unit = {},
) {
    val isUpdateEnabled by rememberIsUpdateEnabled(state = state, uiStateProvider = uiStateProvider)

    ContactDetailScaffold(
        onEvent = onEvent,
        modifier = modifier,
        state = state,
        tabState = tabState,
        uiStateProvider = uiStateProvider,
        componentVisibleProvider = componentVisibleProvider,
        tabFloatingActionButton = { tab ->
            ContactDetailTabFloatingActionButton(
                tab = tab,
                onUpdate = { onEvent(ContactDetailScaffoldEvent.ClickUpdate) },
                onMemoAdd = onMemoAdd,
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { (uiStateProvider() as? ContactDetailUiState.Content)?.isUpdateInProgress == true },
            )
        },
    ) { tab ->
        when (tab) {
            ContactDetailTab.DETAIL ->
                ContactDetailScaffoldContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    uiStateProvider = uiStateProvider,
                )

            ContactDetailTab.MEMO ->
                ContactDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = memoPagingItems,
                )
        }
    }
}

internal fun contactMemo(
    title: String,
    dateTime: MemoDateTime? = null,
): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(
            Memo::detail,
            fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime),
        ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .sample()

internal fun contactMemoPagingData(
    itemList: List<MemoListItem>,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
): PagingData<MemoListItem> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = refresh,
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = append,
            ),
    )

internal fun allDayMemoDateTime(date: LocalDate): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = date..date)
