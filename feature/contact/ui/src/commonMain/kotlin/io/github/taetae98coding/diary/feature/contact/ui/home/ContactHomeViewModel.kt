@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.contact.ContactListEffect
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class ContactHomeViewModel(
    pageContactUseCase: PageContactUseCase,
    deleteContactUseCase: DeleteContactUseCase,
    restoreContactUseCase: RestoreContactUseCase,
) : ViewModel() {
    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.NAME))

    val filterUiState: StateFlow<ContactHomeFilterUiState>
        field = MutableStateFlow(ContactHomeFilterUiState())

    val contactPagingData: Flow<PagingData<ContactListItem>> =
        combine(sortUiState, filterUiState) { sortUiState, filterUiState ->
            PageContactUseCase.Parameter(sort = sortUiState.sort, filter = filterUiState.filter)
        }.flatMapLatest { parameter -> pageContactUseCase(parameter = parameter) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val contactActions =
        ListItemDeleteActions<ContactListEffect>(
            deleteUseCase = deleteContactUseCase,
            restoreUseCase = restoreContactUseCase,
            deletedEffect = ContactListEffect::Deleted,
        )

    val effect: Flow<ContactListEffect> = contactActions.effect

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }

    fun setBirthdayExistence(existence: ContactFilterExistence) {
        filterUiState.update { uiState -> uiState.copy(filter = uiState.filter.copy(birthday = existence)) }
    }

    fun setBirthYearExistence(existence: ContactFilterExistence) {
        filterUiState.update { uiState -> uiState.copy(filter = uiState.filter.copy(birthYear = existence)) }
    }

    fun setPhoneNumberExistence(existence: ContactFilterExistence) {
        filterUiState.update { uiState -> uiState.copy(filter = uiState.filter.copy(phoneNumber = existence)) }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { contactActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { contactActions.restore(id = id) }
    }
}
