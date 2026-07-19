package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceTagLocalDataSource {
    override fun getTagList(
        accountId: Uuid,
        placeId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        database.accountPlaceTagDao().getTagList(
            accountId = accountId,
            placeId = placeId,
        )

    override fun pageSelectableTag(
        accountId: Uuid,
        placeId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountPlaceTagDao().pageSelectableTag(
            accountId = accountId,
            placeId = placeId,
            query = query,
        )
}
