package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebTagLocalDataSource {
    override fun getTagList(
        accountId: Uuid,
        webId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        database.accountWebTagDao().getTagList(
            accountId = accountId,
            webId = webId,
        )

    override fun pageSelectableTag(
        accountId: Uuid,
        webId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountWebTagDao().pageSelectableTag(
            accountId = accountId,
            webId = webId,
            query = query,
        )
}
