package io.github.taetae98coding.diary.core.database.api.dday.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagDDayLocalDataSource {
    public fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, DDayLocalEntity>
}
