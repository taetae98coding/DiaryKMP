package io.github.taetae98coding.diary.core.database.impl.placetag.transaction

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(placeTagList) { placeTag ->
            database.accountPlaceTagSyncDao().clearPending(
                accountId = accountId,
                placeId = placeTag.placeId,
                tagId = placeTag.tagId,
                updatedAt = placeTag.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        placeTagList: List<PlaceTagLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.PLACE_TAG,
            cursor = cursor,
            pulledList = placeTagList,
            keyOf = { placeTag -> placeTag.placeId to placeTag.tagId },
            updatedAtOf = { placeTag -> placeTag.updatedAt },
            readLocalUpdatedAtMap = { pulledList ->
                database
                    .placeTagDao()
                    .findByPlaceIdList(pulledList.map { placeTag -> placeTag.placeId }.distinct())
                    .associate { placeTag -> (placeTag.placeId to placeTag.tagId) to placeTag.updatedAt }
            },
            upsert = { upsertList -> database.placeTagDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountPlaceTagSyncDao().insertIgnore(
                    pulledList.map { placeTag ->
                        AccountPlaceTagLocalEntity(
                            accountId = accountId,
                            placeId = placeTag.placeId,
                            tagId = placeTag.tagId,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
