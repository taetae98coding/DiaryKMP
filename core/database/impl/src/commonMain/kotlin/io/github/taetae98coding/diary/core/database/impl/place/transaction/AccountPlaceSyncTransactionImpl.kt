package io.github.taetae98coding.diary.core.database.impl.place.transaction

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountPlaceSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
    ) {
        database.clearPendingEach(placeList) { place ->
            database.accountPlaceSyncDao().clearPending(
                accountId = accountId,
                placeId = place.id,
                updatedAt = place.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.PLACE,
            cursor = cursor,
            pulledList = placeList,
            keyOf = { place -> place.id },
            updatedAtOf = { place -> place.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.placeDao().findUpdatedAt(pulledList.map { place -> place.id }) },
            upsert = { upsertList -> database.placeDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountPlaceSyncDao().insertIgnore(
                    pulledList.map { place ->
                        AccountPlaceLocalEntity(
                            accountId = accountId,
                            placeId = place.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
