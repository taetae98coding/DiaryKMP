package io.github.taetae98coding.diary.feature.core.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageEntityTagSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
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
public class EntityDetailTagViewModel(
    @InjectedParam private val kind: EntityTagKind,
    @InjectedParam private val entityId: Uuid,
    pageEntityTagSelectableTagUseCase: PageEntityTagSelectableTagUseCase,
    getEntityTagUseCase: GetEntityTagUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
) : ViewModel(),
    EntityTagInputController {
    private val pagingState =
        TagPickerPagingState(
            pageTag = { query ->
                pageEntityTagSelectableTagUseCase(parameter = PageEntityTagSelectableTagUseCase.Parameter(kind = kind, entityId = entityId, query = query))
            },
        )

    public val tagPagingData: Flow<PagingData<Tag>> = pagingState.tagPagingData.cachedIn(viewModelScope)
    public val selectableTagPagingData: Flow<PagingData<Tag>> = pagingState.selectableTagPagingData.cachedIn(viewModelScope)

    public val uiState: StateFlow<EntityTagInputUiState> =
        getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = kind, entityId = entityId))
            .map { result -> EntityTagInputUiState(tagList = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = EntityTagInputUiState(),
            )

    private val addingTagIdSet = mutableSetOf<Uuid>()
    private val removingTagIdSet = mutableSetOf<Uuid>()

    override fun updateQuery(query: String) {
        pagingState.updateQuery(query = query)
    }

    override fun add(tagId: Uuid) {
        if (!addingTagIdSet.add(tagId)) return

        viewModelScope.launch {
            try {
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))
            } finally {
                addingTagIdSet.remove(tagId)
            }
        }
    }

    override fun remove(tagId: Uuid) {
        if (!removingTagIdSet.add(tagId)) return

        viewModelScope.launch {
            try {
                removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))
            } finally {
                removingTagIdSet.remove(tagId)
            }
        }
    }
}
