package io.github.taetae98coding.diary.core.database.api.qr.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import kotlin.uuid.Uuid

public interface AccountQrLocalDataSource {
    public fun page(accountId: Uuid): PagingSource<Int, QrLocalEntity>
}
