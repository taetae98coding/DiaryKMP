package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(memoPlaceList) { memoPlace ->
            database.accountMemoPlaceSyncDao().clearPending(
                accountId = accountId,
                memoId = memoPlace.memoId,
                placeId = memoPlace.placeId,
                updatedAt = memoPlace.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        memoPlaceList: List<MemoPlaceLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_PLACE,
            cursor = cursor,
            pulledList = memoPlaceList,
            keyOf = { memoPlace -> memoPlace.memoId to memoPlace.placeId },
            updatedAtOf = { memoPlace -> memoPlace.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .memoPlaceDao()
                    .findByMemoIdList(pulledList.map { memoPlace -> memoPlace.memoId }.distinct())
                    .associate { memoPlace -> (memoPlace.memoId to memoPlace.placeId) to memoPlace.updatedAt }
            },
            upsert = { upsertList -> database.memoPlaceDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountMemoPlaceSyncDao().insertIgnore(
                    pulledList.map { memoPlace ->
                        AccountMemoPlaceLocalEntity(
                            accountId = accountId,
                            memoId = memoPlace.memoId,
                            placeId = memoPlace.placeId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
