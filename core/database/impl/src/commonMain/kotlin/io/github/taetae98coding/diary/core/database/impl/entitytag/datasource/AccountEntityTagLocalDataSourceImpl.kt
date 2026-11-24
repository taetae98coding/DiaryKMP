package io.github.taetae98coding.diary.core.database.impl.entitytag.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.entitytag.datasource.AccountEntityTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountEntityTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountEntityTagLocalDataSource {
    override fun getTagList(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        database.entityTagTable(kind = kind).getTagList(
            accountId = accountId,
            entityId = entityId,
        )

    override fun pageSelectableTag(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        database.entityTagTable(kind = kind).pageSelectableTag(
            accountId = accountId,
            entityId = entityId,
            query = query,
        )
}
