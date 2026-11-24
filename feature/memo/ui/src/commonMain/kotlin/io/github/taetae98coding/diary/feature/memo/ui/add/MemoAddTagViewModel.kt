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
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagInputController
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionState
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionUiState
import io.github.taetae98coding.diary.feature.memo.ui.picker.MemoSelectablePaging
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

@AssistedInject
internal class MemoAddTagViewModel(
    @Assisted initialPrimaryTagId: Uuid?,
    pageTagUseCase: PageTagUseCase,
    getSelectedTagUseCase: GetSelectedTagUseCase,
) : ViewModel(),
    PrimaryTagInputController {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(initialPrimaryTagId: Uuid?): MemoAddTagViewModel
    }

    private val selectionState =
        PrimaryTagSelectionState(
            initialPrimaryTagId = initialPrimaryTagId,
            getSelectedTag = { tagIdSet -> getSelectedTagUseCase(parameter = tagIdSet) },
        )

    private val selectablePaging =
        MemoSelectablePaging { query -> pageTagUseCase(parameter = query) }

    val selectionUiState: StateFlow<PrimaryTagSelectionUiState> = selectionState.selectionUiState

    val tagPagingData: Flow<PagingData<Tag>> = selectablePaging.pagingData.cachedIn(viewModelScope)

    val selectableTagPagingData: Flow<PagingData<Tag>> = selectablePaging.selectablePagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<PrimaryTagInputUiState> =
        selectionState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = PrimaryTagInputUiState(),
        )

    override fun updateQuery(query: String) {
        selectablePaging.updateQuery(query)
    }

    override fun selectTag(tagId: Uuid) {
        selectionState.selectTag(tagId = tagId)
    }

    override fun unselectTag(tagId: Uuid) {
        selectionState.unselectTag(tagId = tagId)
    }

    override fun selectPrimaryTag(tagId: Uuid) {
        selectionState.selectPrimaryTag(tagId = tagId)
    }

    override fun unselectPrimaryTag() {
        selectionState.unselectPrimaryTag()
    }
}
