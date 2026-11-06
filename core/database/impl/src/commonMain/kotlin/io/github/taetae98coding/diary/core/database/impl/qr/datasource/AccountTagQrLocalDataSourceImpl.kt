package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountTagQrLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagQrLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagQrLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, QrLocalEntity> =
        database.accountTagQrDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
        )
}
