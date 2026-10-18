@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ContactHomeViewModel(
    pageContactUseCase: PageContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val restoreContactUseCase: RestoreContactUseCase,
) : ViewModel() {
    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.NAME))

    val contactPagingData: Flow<PagingData<Contact>> =
        sortUiState
            .flatMapLatest { (sort) -> pageContactUseCase(parameter = sort) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<ContactHomeEffect>(Channel.BUFFERED)
    val effect: Flow<ContactHomeEffect> = _effect.receiveAsFlow()

    private val deletingIdSet = mutableSetOf<Uuid>()
    private val restoringIdSet = mutableSetOf<Uuid>()

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }

    fun delete(id: Uuid) {
        if (!deletingIdSet.add(id)) return

        viewModelScope.launch {
            try {
                deleteContactUseCase(parameter = id)
                    .onSuccess { _effect.send(ContactHomeEffect.Deleted(id = id)) }
            } finally {
                deletingIdSet.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!restoringIdSet.add(id)) return

        viewModelScope.launch {
            try {
                restoreContactUseCase(parameter = id)
            } finally {
                restoringIdSet.remove(id)
            }
        }
    }
}
