package io.github.taetae98coding.diary.core.database.api.music.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagMusicLocalDataSource {
    public fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
        sort: ListSortLocalEntity,
    ): PagingSource<Int, MusicLocalEntity>
}
