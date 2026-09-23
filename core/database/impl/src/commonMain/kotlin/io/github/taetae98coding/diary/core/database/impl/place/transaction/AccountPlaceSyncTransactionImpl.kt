package io.github.taetae98coding.diary.core.database.impl.place.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
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
        database.withWriteTransaction {
            placeList.forEach { place ->
                database.accountPlaceSyncDao().clearPending(
                    accountId = accountId,
                    placeId = place.id,
                    updatedAt = place.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.placeDao().findUpdatedAt(placeList.map { place -> place.id })
            database.placeDao().upsert(
                placeList.filter { place ->
                    val localUpdatedAt = localUpdatedAtMap[place.id]
                    localUpdatedAt == null || place.updatedAt >= localUpdatedAt
                },
            )
            database.accountPlaceSyncDao().insertIgnore(
                placeList.map { place ->
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.PLACE),
                    usn = cursor,
                ),
            )
        }
    }
}
