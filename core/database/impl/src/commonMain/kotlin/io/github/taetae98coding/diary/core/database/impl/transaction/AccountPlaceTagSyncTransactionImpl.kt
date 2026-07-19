package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTagSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountPlaceTagSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        placeTagList: List<PlaceTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            placeTagList.forEach { placeTag ->
                database.accountPlaceTagSyncDao().clearPending(
                    accountId = accountId,
                    placeId = placeTag.placeId,
                    tagId = placeTag.tagId,
                    updatedAt = placeTag.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        placeTagList: List<PlaceTagLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localPlaceTagMap =
                database
                    .placeTagDao()
                    .findByPlaceIdList(placeTagList.map { placeTag -> placeTag.placeId }.distinct())
                    .associateBy { placeTag -> placeTag.placeId to placeTag.tagId }

            database.placeTagDao().upsert(
                placeTagList.filter { placeTag ->
                    val localUpdatedAt = localPlaceTagMap[placeTag.placeId to placeTag.tagId]?.updatedAt
                    localUpdatedAt == null || placeTag.updatedAt >= localUpdatedAt
                },
            )
            database.accountPlaceTagSyncDao().insertIgnore(
                placeTagList.map { placeTag ->
                    AccountPlaceTagLocalEntity(
                        accountId = accountId,
                        placeId = placeTag.placeId,
                        tagId = placeTag.tagId,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.PLACE_TAG),
                    usn = cursor,
                ),
            )
        }
    }
}
