package io.github.taetae98coding.diary.core.database.api.webtag.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountWebTagLocalDataSource {
    public fun getTagList(
        accountId: Uuid,
        webId: Uuid,
    ): Flow<List<TagLocalEntity>>

    public fun pageSelectableTag(
        accountId: Uuid,
        webId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>
}
