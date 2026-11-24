package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountTagLinkLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagLinkLocalDataSource {
    override fun getTagList(
        accountId: Uuid,
        fromTagId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        database.accountTagLinkDao().getTagList(
            accountId = accountId,
            fromTagId = fromTagId,
        )

    override fun pageSelectableTag(
        accountId: Uuid,
        fromTagId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountTagLinkDao().pageSelectableTag(
            accountId = accountId,
            fromTagId = fromTagId,
            query = query,
        )
}
