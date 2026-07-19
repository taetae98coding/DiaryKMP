package io.github.taetae98coding.diary.core.database.api.placetag.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountPlaceTagLocalDataSource {
    public fun getTagList(
        accountId: Uuid,
        placeId: Uuid,
    ): Flow<List<TagLocalEntity>>

    public fun pageSelectableTag(
        accountId: Uuid,
        placeId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>
}
