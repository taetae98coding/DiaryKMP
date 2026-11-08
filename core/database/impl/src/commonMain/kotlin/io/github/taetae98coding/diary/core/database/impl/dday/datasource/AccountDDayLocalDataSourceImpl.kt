package io.github.taetae98coding.diary.core.database.impl.dday.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountDDayLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDDayLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountDDayLocalDataSource {
    override fun page(accountId: Uuid): PagingSource<Int, DDayLocalEntity> = database.accountDDayDao().page(accountId = accountId)

    override fun find(
        accountId: Uuid,
        ddayId: Uuid,
    ): Flow<DDayLocalEntity?> =
        database.accountDDayDao().find(
            accountId = accountId,
            ddayId = ddayId,
        )
}
