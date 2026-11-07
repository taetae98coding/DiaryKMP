package io.github.taetae98coding.diary.core.database.api.qr.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagQrLocalDataSource {
    public fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, QrLocalEntity>
}
