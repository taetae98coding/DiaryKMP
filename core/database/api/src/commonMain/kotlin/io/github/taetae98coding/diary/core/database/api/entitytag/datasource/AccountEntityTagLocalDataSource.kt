package io.github.taetae98coding.diary.core.database.api.entitytag.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountEntityTagLocalDataSource {
    public fun getTagList(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
    ): Flow<List<TagLocalEntity>>

    public fun pageSelectableTag(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>
}
