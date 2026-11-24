@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputController
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
import kotlin.uuid.Uuid

@AssistedInject
internal class MemoAddContactViewModel(
    @Assisted initialContactId: Uuid?,
    pageMemoSelectableContactUseCase: PageMemoSelectableContactUseCase,
    getSelectedContactUseCase: GetSelectedContactUseCase,
) : ViewModel(),
    MemoContactInputController {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(initialContactId: Uuid?): MemoAddContactViewModel
    }

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
        MemoSelectablePaging { query -> pageMemoSelectableContactUseCase(parameter = query) }

    val contactPagingData: Flow<PagingData<ContactListItem>> = selectablePaging.pagingData.cachedIn(viewModelScope)

    val selectableContactPagingData: Flow<PagingData<ContactListItem>> = selectablePaging.selectablePagingData.cachedIn(viewModelScope)

    override fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    override fun selectContact(contactId: Uuid) {
        selectionUiState.update { value -> value.copy(contactIdSet = value.contactIdSet + contactId) }
    }

    override fun unselectContact(contactId: Uuid) {
        selectionUiState.update { value -> value.copy(contactIdSet = value.contactIdSet - contactId) }
    }
}
