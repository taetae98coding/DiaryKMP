package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
