package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagViewModel
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.uuid.Uuid

// KoinApplication에 넘긴 모듈은 첫 테스트의 것이 이어서 쓰이므로, 조회 결과는 모듈이 붙잡는 이 흐름의 값으로 테스트마다 바꾼다.
private val memoPagingDataFlow = MutableStateFlow(pagingDataOf<Memo>(emptyList()))
private val tagPagingDataFlow = MutableStateFlow(pagingDataOf<Tag>(emptyList()))
private val placePagingDataFlow = MutableStateFlow(pagingDataOf<Place>(emptyList()))
private val webPagingDataFlow = MutableStateFlow(pagingDataOf<Web>(emptyList()))

// 검색어 반영 시점은 ViewModel 테스트가 검증하므로, 화면 테스트에서는 입력한 질의가 곧바로 반영된 것으로 다룬다.
// 반영을 기다리는 상태를 확인하는 테스트만 반영을 멈춘다.
private const val SEARCH_HOME_ENTRY_KEY = "SearchHome"

private val appliedQueryFlow = MutableStateFlow("")
private var isQueryApplied = true

private val searchHomeViewModelModule =
    module {
        factory {
            mockk<SearchHomeMemoViewModel>(relaxed = true).apply {
                every { pagingData } returns memoPagingDataFlow
                every { appliedQuery } returns appliedQueryFlow
                every { sort } returns MutableStateFlow(ListSort.TITLE)
                every { updateQuery(any()) } answers { if (isQueryApplied) appliedQueryFlow.value = firstArg() }
            }
        }
        factory {
            mockk<SearchHomeTagViewModel>(relaxed = true).apply {
                every { pagingData } returns tagPagingDataFlow
                every { appliedQuery } returns appliedQueryFlow
                every { sort } returns MutableStateFlow(ListSort.TITLE)
                every { updateQuery(any()) } answers { if (isQueryApplied) appliedQueryFlow.value = firstArg() }
            }
        }
        factory {
            mockk<SearchHomePlaceViewModel>(relaxed = true).apply {
                every { pagingData } returns placePagingDataFlow
                every { appliedQuery } returns appliedQueryFlow
                every { sort } returns MutableStateFlow(ListSort.TITLE)
                every { updateQuery(any()) } answers { if (isQueryApplied) appliedQueryFlow.value = firstArg() }
            }
        }
        factory {
            mockk<SearchHomeWebViewModel>(relaxed = true).apply {
                every { pagingData } returns webPagingDataFlow
                every { appliedQuery } returns appliedQueryFlow
                every { sort } returns MutableStateFlow(ListSort.TITLE)
                every { updateQuery(any()) } answers { if (isQueryApplied) appliedQueryFlow.value = firstArg() }
            }
        }
    }

// 유형별 ViewModel은 각 Content가 Koin으로 얻으므로, 조회 결과는 ViewModel을 대신 넣어 정한다.
internal fun ComposeContentTestRule.setSearchHomeScreen(
    isQueryAppliedImmediately: Boolean = true,
    memoList: List<Memo> = emptyList(),
    tagList: List<Tag> = emptyList(),
    placeList: List<Place> = emptyList(),
    webList: List<Web> = emptyList(),
    initialType: SearchHomeType = SearchHomeType.MEMO,
    navigateUp: () -> Unit = {},
    navigateToMemoDetail: (Uuid) -> Unit = {},
    navigateToTagDetail: (Uuid) -> Unit = {},
    navigateToPlaceDetail: (Uuid) -> Unit = {},
    navigateToWebDetail: (Uuid) -> Unit = {},
    isShownProvider: () -> Boolean = { true },
) {
    appliedQueryFlow.value = ""
    isQueryApplied = isQueryAppliedImmediately
    memoPagingDataFlow.value = pagingDataOf(memoList)
    tagPagingDataFlow.value = pagingDataOf(tagList)
    placePagingDataFlow.value = pagingDataOf(placeList)
    webPagingDataFlow.value = pagingDataOf(webList)

    setContent {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로,
        // 테스트마다 새 소유자를 제공해 이전 테스트의 유형별 ViewModel이 재사용되지 않게 한다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(searchHomeViewModelModule) }) {
                DiaryTheme {
                    // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
                    val saveableStateHolder = rememberSaveableStateHolder()

                    if (isShownProvider()) {
                        saveableStateHolder.SaveableStateProvider(key = SEARCH_HOME_ENTRY_KEY) {
                            SearchHomeScreen(
                                navigateUp = navigateUp,
                                navigateToMemoDetail = navigateToMemoDetail,
                                navigateToTagDetail = navigateToTagDetail,
                                navigateToPlaceDetail = navigateToPlaceDetail,
                                navigateToWebDetail = navigateToWebDetail,
                                initialType = initialType,
                            )
                        }
                    }
                }
            }
        }
    }
    waitForIdle()
}

// 사용자가 입력을 이어 가는 동안처럼 입력한 질의가 아직 결과에 반영되지 않은 상태를 만든다.
internal fun setSearchQueryApplied(isApplied: Boolean) {
    isQueryApplied = isApplied
}

internal fun ComposeContentTestRule.selectSearchHomeTab(label: String) {
    onNodeWithText(label).performClick()
    waitForIdle()
}
