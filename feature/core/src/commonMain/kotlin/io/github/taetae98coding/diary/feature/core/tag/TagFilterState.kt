package io.github.taetae98coding.diary.feature.core.tag

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

public class TagFilterState(
    pageTagUseCase: PageTagUseCase,
    getFilterUseCase: FlowUseCase<Unit, List<Tag>>,
    private val selectTagUseCase: UseCase<Uuid, *>,
    private val unselectTagUseCase: UseCase<Uuid, *>,
    private val unselectAllTagUseCase: UseCase<Unit, *>,
) {
    public val tagPagingData: Flow<PagingData<Tag>> =
        pageTagUseCase(parameter = "")
            .map { result -> result.getOrElse { PagingData.empty() } }

    public val selectedTagIdSet: Flow<Set<Uuid>> =
        getFilterUseCase()
            .map { result ->
                result
                    .getOrDefault(emptyList())
                    .map { tag -> tag.id }
                    .toSet()
            }

    private val inProgressSelectTagSet = mutableSetOf<Uuid>()
    private val inProgressUnselectTagSet = mutableSetOf<Uuid>()
    private var isUnselectAllInProgress = false

    public suspend fun select(id: Uuid) {
        if (!inProgressSelectTagSet.add(id)) return

        try {
            selectTagUseCase(parameter = id)
        } finally {
            inProgressSelectTagSet.remove(id)
        }
    }

    public suspend fun unselect(id: Uuid) {
        if (!inProgressUnselectTagSet.add(id)) return

        try {
            unselectTagUseCase(parameter = id)
        } finally {
            inProgressUnselectTagSet.remove(id)
        }
    }

    public suspend fun unselectAll() {
        if (isUnselectAllInProgress) return
        isUnselectAllInProgress = true

        try {
            unselectAllTagUseCase()
        } finally {
            isUnselectAllInProgress = false
        }
    }
}
