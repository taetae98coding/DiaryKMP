@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
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
    val contactIdSet: StateFlow<Set<Uuid>>
        field = MutableStateFlow(setOfNotNull(initialContactId))

    val uiState: StateFlow<MemoContactInputUiState> =
        contactIdSet
            .flatMapLatest { contactIdSet -> getSelectedContactUseCase(parameter = contactIdSet) }
            .map { result -> MemoContactInputUiState(selectedContactList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoContactInputUiState(),
            )

    private val query = MutableStateFlow("")

    val contactPagingData: Flow<PagingData<Contact>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> pageMemoSelectableContactUseCase(parameter = value) }
            .mapNotNull { result -> result.getOrNull() }
            .cachedIn(viewModelScope)

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun selectContact(id: Uuid) {
        contactIdSet.update { value -> value + id }
    }

    fun unselectContact(id: Uuid) {
        contactIdSet.update { value -> value - id }
    }
}
