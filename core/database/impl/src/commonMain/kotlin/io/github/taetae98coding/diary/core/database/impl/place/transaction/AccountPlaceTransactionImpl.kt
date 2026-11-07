package io.github.taetae98coding.diary.core.database.impl.place.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.placeTagTable
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTransactionImpl(
    private val database: DiaryDatabase,
) : AccountPlaceTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
        placeTagList: List<EntityTagLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.placeDao().upsert(placeList)
            database.accountPlaceDao().upsert(
                placeList.map { place ->
                    AccountPlaceLocalEntity(
                        accountId = accountId,
                        placeId = place.id,
                        isDirty = true,
                    )
                },
            )
            database.placeTagTable().upsertPending(accountId = accountId, entityTagList = placeTagList)
        }
    }

    override suspend fun updateDetail(
        accountId: Uuid,
        placeId: Uuid,
        detail: PlaceDetailLocalEntity,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            placeId = placeId,
        ) {
            database.accountPlaceDao().updateDetail(
                accountId = accountId,
                placeId = placeId,
                title = detail.title,
                description = detail.description,
                color = detail.color,
                latitude = detail.latitude,
                longitude = detail.longitude,
                address = detail.address,
                updatedAt = updatedAt,
            )
        }

    override suspend fun updateDeleted(
        accountId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        updateAndMarkPending(
            accountId = accountId,
            placeId = placeId,
        ) {
            database.accountPlaceDao().updateDeleted(
                accountId = accountId,
                placeId = placeId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
            )
        }

    private suspend fun updateAndMarkPending(
        accountId: Uuid,
        placeId: Uuid,
        update: suspend () -> Int,
    ): Int =
        database.withWriteTransaction {
            val updatedCount = update()

            if (updatedCount > 0) {
                database.accountPlaceDao().markPending(accountId = accountId, placeId = placeId)
            }

            updatedCount
        }
}
