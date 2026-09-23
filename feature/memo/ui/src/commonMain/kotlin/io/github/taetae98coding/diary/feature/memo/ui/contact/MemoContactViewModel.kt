@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    private val query = MutableStateFlow("")

    val contactPagingData: Flow<PagingData<Contact>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> pageMemoSelectableContactUseCase(parameter = value) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<MemoContactInputUiState> =
        getMemoContactUseCase(parameter = id)
            .map { result -> MemoContactInputUiState(selectedContactList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = MemoContactInputUiState(),
            )

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectContact(contactId: Uuid) {
        viewModelScope.launch {
            addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = id, contactId = contactId))
        }
    }

    fun unselectContact(contactId: Uuid) {
        viewModelScope.launch {
            removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = id, contactId = contactId))
        }
    }
}
