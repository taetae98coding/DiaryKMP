package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.contact.contactPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagSelection
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.web.MemoWebInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.web.webPagingDataOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.uuid.Uuid

/**
 * MemoAdd 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun MemoAddScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

/**
 * MemoAdd 화면은 메모 추가와 태그 선택, 웹 선택, 연락처 선택, 장소 선택을 각각의 ViewModel에서 읽으므로, 화면을 배치하는 테스트는 다섯을 함께 넘긴다.
 */
internal data class MemoAddScreenViewModels(
    val viewModel: MemoAddViewModel,
    val tagViewModel: MemoAddTagViewModel,
    val webViewModel: MemoAddWebViewModel,
    val contactViewModel: MemoAddContactViewModel,
    val placeViewModel: MemoAddPlaceViewModel,
)

internal fun screenTestViewModel(
    effect: Flow<MemoAddEffect> = emptyFlow(),
    uiState: MemoAddUiState = MemoAddUiState(),
    tagList: List<Tag> = emptyList(),
    webList: List<Web> = emptyList(),
    contactList: List<Contact> = emptyList(),
    placeList: List<Place> = emptyList(),
    tagPagingData: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
    webPagingData: Flow<PagingData<Web>> = MutableStateFlow(webPagingDataOf(webList)),
    contactPagingData: Flow<PagingData<Contact>> = MutableStateFlow(contactPagingDataOf(contactList)),
    placePagingData: Flow<PagingData<Place>> = MutableStateFlow(placePagingDataOf(placeList)),
): MemoAddScreenViewModels {
    val viewModel = mockk<MemoAddViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect

    val tagViewModel = mockk<MemoAddTagViewModel>(relaxed = true)
    every { tagViewModel.uiState } returns MutableStateFlow(MemoTagInputUiState())
    every { tagViewModel.selection } returns MutableStateFlow(MemoTagSelection())
    every { tagViewModel.tagPagingData } returns tagPagingData

    val webViewModel = mockk<MemoAddWebViewModel>(relaxed = true)
    every { webViewModel.uiState } returns MutableStateFlow(MemoWebInputUiState())
    every { webViewModel.webIdSet } returns MutableStateFlow(emptySet())
    every { webViewModel.webPagingData } returns webPagingData

    val contactViewModel = mockk<MemoAddContactViewModel>(relaxed = true)
    every { contactViewModel.uiState } returns MutableStateFlow(MemoContactInputUiState())
    every { contactViewModel.contactIdSet } returns MutableStateFlow(emptySet())
    every { contactViewModel.contactPagingData } returns contactPagingData

    val placeViewModel = mockk<MemoAddPlaceViewModel>(relaxed = true)
    every { placeViewModel.uiState } returns MutableStateFlow(MemoPlaceInputUiState())
    every { placeViewModel.placeIdSet } returns MutableStateFlow(emptySet())
    every { placeViewModel.placePagingData } returns placePagingData

    return MemoAddScreenViewModels(
        viewModel = viewModel,
        tagViewModel = tagViewModel,
        webViewModel = webViewModel,
        contactViewModel = contactViewModel,
        placeViewModel = placeViewModel,
    )
}

/**
 * 태그와 장소 선택은 각 ViewModel이 단일 소스로 보관하므로, 선택 조작이 화면에 반영되는 흐름은
 * 실제 ViewModel에 조회 결과만 mock으로 전달해 검증한다.
 */
internal fun screenTestRealViewModel(
    initialPrimaryTagId: Uuid? = null,
    initialContactId: Uuid? = null,
    initialPlaceId: Uuid? = null,
    initialWebId: Uuid? = null,
    tagList: List<Tag> = emptyList(),
    webList: List<Web> = emptyList(),
    contactList: List<Contact> = emptyList(),
    placeList: List<Place> = emptyList(),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
    webPagingDataFlow: Flow<PagingData<Web>> = MutableStateFlow(webPagingDataOf(webList)),
    contactPagingDataFlow: Flow<PagingData<Contact>> = MutableStateFlow(contactPagingDataOf(contactList)),
    placePagingDataFlow: Flow<PagingData<Place>> = MutableStateFlow(placePagingDataOf(placeList)),
    addMemoUseCase: AddMemoUseCase = mockk(),
): MemoAddScreenViewModels {
    val pageTagUseCase = mockk<PageTagUseCase>()
    every { pageTagUseCase(parameter = any()) } returns MutableStateFlow(Result.success(tagPagingDataOf(tagList)))

    val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
    every { getSelectedTagUseCase(parameter = any()) } answers { selectedFlowOf(list = tagList, idOf = Tag::id, idSet = firstArg()) }

    val pageMemoSelectableWebUseCase = mockk<PageMemoSelectableWebUseCase>()
    every { pageMemoSelectableWebUseCase(parameter = any()) } returns MutableStateFlow(Result.success(webPagingDataOf(webList)))

    val getSelectedWebUseCase = mockk<GetSelectedWebUseCase>()
    every { getSelectedWebUseCase(parameter = any()) } answers { selectedFlowOf(list = webList, idOf = Web::id, idSet = firstArg()) }

    val pageMemoSelectableContactUseCase = mockk<PageMemoSelectableContactUseCase>()
    every { pageMemoSelectableContactUseCase(parameter = any()) } returns MutableStateFlow(Result.success(contactPagingDataOf(contactList)))

    val getSelectedContactUseCase = mockk<GetSelectedContactUseCase>()
    every { getSelectedContactUseCase(parameter = any()) } answers { selectedFlowOf(list = contactList, idOf = Contact::id, idSet = firstArg()) }

    val pagePlaceUseCase = mockk<PagePlaceUseCase>()
    every { pagePlaceUseCase(parameter = any()) } returns MutableStateFlow(Result.success(placePagingDataOf(placeList)))

    val getSelectedPlaceUseCase = mockk<GetSelectedPlaceUseCase>()
    every { getSelectedPlaceUseCase(parameter = any()) } answers { selectedFlowOf(list = placeList, idOf = Place::id, idSet = firstArg()) }

    val tagViewModel =
        MemoAddTagViewModel(
            initialPrimaryTagId = initialPrimaryTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    val webViewModel =
        MemoAddWebViewModel(
            initialWebId = initialWebId,
            pageMemoSelectableWebUseCase = pageMemoSelectableWebUseCase,
            getSelectedWebUseCase = getSelectedWebUseCase,
        )
    val contactViewModel =
        MemoAddContactViewModel(
            initialContactId = initialContactId,
            pageMemoSelectableContactUseCase = pageMemoSelectableContactUseCase,
            getSelectedContactUseCase = getSelectedContactUseCase,
        )
    val placeViewModel =
        MemoAddPlaceViewModel(
            initialPlaceId = initialPlaceId,
            pagePlaceUseCase = pagePlaceUseCase,
            getSelectedPlaceUseCase = getSelectedPlaceUseCase,
        )

    return MemoAddScreenViewModels(
        viewModel = MemoAddViewModel(addMemoUseCase = addMemoUseCase),
        // 선택 목록의 페이지 조회 자체는 이 화면 검증의 대상이 아니므로 준비된 목록으로 고정한다.
        tagViewModel = spyk(tagViewModel) { every { tagPagingData } returns tagPagingDataFlow },
        webViewModel = spyk(webViewModel) { every { webPagingData } returns webPagingDataFlow },
        contactViewModel = spyk(contactViewModel) { every { contactPagingData } returns contactPagingDataFlow },
        placeViewModel = spyk(placeViewModel) { every { placePagingData } returns placePagingDataFlow },
    )
}

// 선택한 식별자로 저장소를 조회하는 동작을 준비된 목록에서 골라내는 방식으로 대신한다.
private fun <T> selectedFlowOf(
    list: List<T>,
    idOf: (T) -> Uuid,
    idSet: Set<Uuid>,
): Flow<Result<List<T>>> = MutableStateFlow(Result.success(list.filter { item -> idOf(item) in idSet }))
