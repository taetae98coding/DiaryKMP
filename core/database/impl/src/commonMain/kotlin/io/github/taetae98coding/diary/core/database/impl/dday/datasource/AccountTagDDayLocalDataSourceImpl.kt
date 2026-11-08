package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountTagDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagDDayLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagDDayLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, DDayLocalEntity> =
        database.accountTagDDayDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
        )
}
