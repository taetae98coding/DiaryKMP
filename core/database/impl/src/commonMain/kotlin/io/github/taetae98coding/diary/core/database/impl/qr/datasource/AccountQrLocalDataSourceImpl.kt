package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountQrLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountQrLocalDataSource {
    override fun page(accountId: Uuid): PagingSource<Int, QrLocalEntity> = database.accountQrDao().page(accountId = accountId)

    override fun find(
        accountId: Uuid,
        qrId: Uuid,
    ): Flow<QrLocalEntity?> =
        database.accountQrDao().find(
            accountId = accountId,
            qrId = qrId,
        )
}
