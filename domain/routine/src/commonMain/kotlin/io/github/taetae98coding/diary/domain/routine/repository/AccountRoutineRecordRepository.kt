package io.github.taetae98coding.diary.domain.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountRoutineRecordRepository {
    public fun page(
        account: Account,
        routineId: Uuid,
    ): Flow<PagingData<RoutineRecord>>

    public fun find(
        account: Account,
        recordId: Uuid,
    ): Flow<RoutineRecord?>

    public suspend fun upsert(
        account: Account,
        record: RoutineRecord,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        recordId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
