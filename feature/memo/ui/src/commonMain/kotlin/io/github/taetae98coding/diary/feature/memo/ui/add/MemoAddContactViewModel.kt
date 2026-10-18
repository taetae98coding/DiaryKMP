@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoAddContactViewModel(
    @InjectedParam initialContactId: Uuid?,
    pageMemoSelectableContactUseCase: PageMemoSelectableContactUseCase,
    getSelectedContactUseCase: GetSelectedContactUseCase,
) : ViewModel() {
    val selectionUiState: StateFlow<MemoAddContactSelectionUiState>
        field = MutableStateFlow(MemoAddContactSelectionUiState(contactIdSet = setOfNotNull(initialContactId)))

    val uiState: StateFlow<MemoContactInputUiState> =
        selectionUiState
            .flatMapLatest { value -> getSelectedContactUseCase(parameter = value.contactIdSet) }
            .map { result -> MemoContactInputUiState(selectedContactList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoContactInputUiState(),
            )

    private val selectablePaging =
        MemoSelectablePaging(scope = viewModelScope) { query -> pageMemoSelectableContactUseCase(parameter = query) }

    val contactPagingData: Flow<PagingData<Contact>> = selectablePaging.pagingData

    val selectableContactPagingData: Flow<PagingData<Contact>> = selectablePaging.selectablePagingData

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectContact(id: Uuid) {
        selectionUiState.update { value -> value.copy(contactIdSet = value.contactIdSet + id) }
    }

    fun unselectContact(id: Uuid) {
        selectionUiState.update { value -> value.copy(contactIdSet = value.contactIdSet - id) }
    }
}
