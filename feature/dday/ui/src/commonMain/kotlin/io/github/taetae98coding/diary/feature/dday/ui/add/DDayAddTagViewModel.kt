package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagInputController
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionState
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.TagPickerPagingState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class DDayAddTagViewModel(
    @InjectedParam initialPrimaryTagId: Uuid?,
    pageTagUseCase: PageTagUseCase,
    getSelectedTagUseCase: GetSelectedTagUseCase,
) : ViewModel(),
    PrimaryTagInputController {
    private val selectionState =
        PrimaryTagSelectionState(
            initialPrimaryTagId = initialPrimaryTagId,
            getSelectedTag = { tagIdSet -> getSelectedTagUseCase(parameter = tagIdSet) },
        )

    private val pagingState = TagPickerPagingState(pageTag = { query -> pageTagUseCase(parameter = query) })

    val selectionUiState: StateFlow<PrimaryTagSelectionUiState> = selectionState.selectionUiState

    val tagPagingData: Flow<PagingData<Tag>> = pagingState.tagPagingData.cachedIn(viewModelScope)

    val selectableTagPagingData: Flow<PagingData<Tag>> = pagingState.selectableTagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<PrimaryTagInputUiState> =
        selectionState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = PrimaryTagInputUiState(),
        )

    override fun updateQuery(query: String) {
        pagingState.updateQuery(query = query)
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
