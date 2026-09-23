package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoPlaceSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountMemoPlaceSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        memoPlaceList: List<MemoPlaceLocalEntity>,
    ) {
        database.withWriteTransaction {
            memoPlaceList.forEach { memoPlace ->
                database.accountMemoPlaceSyncDao().clearPending(
                    accountId = accountId,
                    memoId = memoPlace.memoId,
                    placeId = memoPlace.placeId,
                    updatedAt = memoPlace.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        memoPlaceList: List<MemoPlaceLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localMemoPlaceMap =
                database
                    .memoPlaceDao()
                    .findByMemoIdList(memoPlaceList.map { memoPlace -> memoPlace.memoId }.distinct())
                    .associateBy { memoPlace -> memoPlace.memoId to memoPlace.placeId }

            database.memoPlaceDao().upsert(
                memoPlaceList.filter { memoPlace ->
                    val localUpdatedAt = localMemoPlaceMap[memoPlace.memoId to memoPlace.placeId]?.updatedAt
                    localUpdatedAt == null || memoPlace.updatedAt >= localUpdatedAt
                },
            )
            database.accountMemoPlaceSyncDao().insertIgnore(
                memoPlaceList.map { memoPlace ->
                    AccountMemoPlaceLocalEntity(
                        accountId = accountId,
                        memoId = memoPlace.memoId,
                        placeId = memoPlace.placeId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.MEMO_PLACE),
                    usn = cursor,
                ),
            )
        }
    }
}
