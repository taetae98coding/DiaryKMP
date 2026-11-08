package io.github.taetae98coding.diary.core.database.api.dday.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountDDayLocalDataSource {
    public fun page(accountId: Uuid): PagingSource<Int, DDayLocalEntity>

    public fun find(
        accountId: Uuid,
        ddayId: Uuid,
    ): Flow<DDayLocalEntity?>
}
