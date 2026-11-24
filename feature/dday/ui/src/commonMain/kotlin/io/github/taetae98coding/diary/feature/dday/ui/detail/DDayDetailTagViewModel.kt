package io.github.taetae98coding.diary.feature.dday.ui.detail

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
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.SetDDayPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UnsetDDayPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageEntityTagSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.PrimaryTagInputController
import io.github.taetae98coding.diary.feature.core.tag.TagPickerPagingState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
internal class DDayDetailTagViewModel(
    @Assisted private val id: Uuid,
    pageEntityTagSelectableTagUseCase: PageEntityTagSelectableTagUseCase,
    getEntityTagUseCase: GetEntityTagUseCase,
    findDDayUseCase: FindDDayUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
    private val setDDayPrimaryTagUseCase: SetDDayPrimaryTagUseCase,
    private val unsetDDayPrimaryTagUseCase: UnsetDDayPrimaryTagUseCase,
) : ViewModel(),
    PrimaryTagInputController {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(id: Uuid): DDayDetailTagViewModel
    }

    private val pagingState =
        TagPickerPagingState(
            pageTag = { query ->
                pageEntityTagSelectableTagUseCase(parameter = PageEntityTagSelectableTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, query = query))
            },
        )

    val tagPagingData: Flow<PagingData<Tag>> = pagingState.tagPagingData.cachedIn(viewModelScope)

    val selectableTagPagingData: Flow<PagingData<Tag>> = pagingState.selectableTagPagingData.cachedIn(viewModelScope)

    val uiState: StateFlow<PrimaryTagInputUiState> =
        combine(
            getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id)),
            findDDayUseCase(parameter = id).map { result -> result.getOrNull()?.primaryTagId },
        ) { tagResult, primaryTagId ->
            val selectedTagList = tagResult.getOrNull().orEmpty()

            PrimaryTagInputUiState(
                selectedTagList = selectedTagList,
                primaryTagId = primaryTagId?.takeIf { tagId -> selectedTagList.any { tag -> tag.id == tagId } },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = PrimaryTagInputUiState(),
        )

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private val inProgressSelectPrimaryTagSet = mutableSetOf<Uuid>()
    private var isUnselectPrimaryTagInProgress = false

    override fun updateQuery(query: String) {
        pagingState.updateQuery(query = query)
    }

    override fun selectTag(tagId: Uuid) {
        if (!inProgressSelectTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = tagId))
            } finally {
                inProgressSelectTagSet.remove(tagId)
            }
        }
    }

    override fun unselectTag(tagId: Uuid) {
        if (!inProgressUnselectTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id, tagId = tagId))
            } finally {
                inProgressUnselectTagSet.remove(tagId)
            }
        }
    }

    override fun selectPrimaryTag(tagId: Uuid) {
        if (!inProgressSelectPrimaryTagSet.add(tagId)) return

        viewModelScope.launch {
            try {
                setDDayPrimaryTagUseCase(parameter = SetDDayPrimaryTagUseCase.Parameter(ddayId = id, tagId = tagId))
            } finally {
                inProgressSelectPrimaryTagSet.remove(tagId)
            }
        }
    }

    override fun unselectPrimaryTag() {
        if (isUnselectPrimaryTagInProgress) return
        isUnselectPrimaryTagInProgress = true

        viewModelScope.launch {
            try {
                unsetDDayPrimaryTagUseCase(parameter = id)
            } finally {
                isUnselectPrimaryTagInProgress = false
            }
        }
    }
}
