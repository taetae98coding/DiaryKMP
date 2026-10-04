package io.github.taetae98coding.diary.core.database.impl.sync.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal suspend fun <T> DiaryDatabase.clearPendingEach(
    pushedList: List<T>,
    clearPending: suspend (T) -> Unit,
) {
    withWriteTransaction {
        pushedList.forEach { pushed -> clearPending(pushed) }
    }
}

// 세 쓰기와 커서 전진을 한 트랜잭션으로 묶어 일부만 반영된 채 커서가 앞서지 않게 한다.
@Suppress("LongParameterList")
internal suspend fun <T, K> DiaryDatabase.upsertPulled(
    accountId: Uuid,
    kind: SyncKindLocalEntity,
    cursor: Long,
    pulledList: List<T>,
    keyOf: (T) -> K,
    updatedAtOf: (T) -> Instant,
    readLocalUpdatedAtMap: suspend (List<T>) -> Map<K, Instant>,
    upsert: suspend (List<T>) -> Unit,
    insertIgnoreAccount: suspend (List<T>) -> Unit,
) {
    withWriteTransaction {
        val localUpdatedAtMap = readLocalUpdatedAtMap(pulledList)

        upsert(
            pulledList.filter { pulled ->
                val localUpdatedAt = localUpdatedAtMap[keyOf(pulled)]
                localUpdatedAt == null || updatedAtOf(pulled) >= localUpdatedAt
            },
        )
        insertIgnoreAccount(pulledList)
        syncCursorDao().upsert(
            SyncCursorLocalEntity(
                accountId = accountId,
                kind = kind.persistentValue,
                usn = cursor,
            ),
        )
    }
}
