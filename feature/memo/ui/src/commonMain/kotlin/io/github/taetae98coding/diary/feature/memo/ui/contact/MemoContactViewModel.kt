@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MemoContactViewModel(
    @InjectedParam private val id: Uuid,
    pageMemoSelectableContactUseCase: PageMemoSelectableContactUseCase,
    getMemoContactUseCase: GetMemoContactUseCase,
    private val addMemoContactUseCase: AddMemoContactUseCase,
    private val removeMemoContactUseCase: RemoveMemoContactUseCase,
) : ViewModel() {
    private val selectablePaging =
        MemoSelectablePaging(scope = viewModelScope) { query -> pageMemoSelectableContactUseCase(parameter = query) }

    val contactPagingData: Flow<PagingData<Contact>> = selectablePaging.pagingData

    val selectableContactPagingData: Flow<PagingData<Contact>> = selectablePaging.selectablePagingData

    val uiState: StateFlow<MemoContactInputUiState> =
        getMemoContactUseCase(parameter = id)
            .map { result -> MemoContactInputUiState(selectedContactList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoContactInputUiState(),
            )

    private val inProgressSelectContactSet = mutableSetOf<Uuid>()
    private val inProgressUnselectContactSet = mutableSetOf<Uuid>()

    fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    fun selectContact(contactId: Uuid) {
        if (!inProgressSelectContactSet.add(contactId)) return

        viewModelScope.launch {
            try {
                addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = id, contactId = contactId))
            } finally {
                inProgressSelectContactSet.remove(contactId)
            }
        }
    }

    fun unselectContact(contactId: Uuid) {
        if (!inProgressUnselectContactSet.add(contactId)) return

        viewModelScope.launch {
            try {
                removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = id, contactId = contactId))
            } finally {
                inProgressUnselectContactSet.remove(contactId)
            }
        }
    }
}
