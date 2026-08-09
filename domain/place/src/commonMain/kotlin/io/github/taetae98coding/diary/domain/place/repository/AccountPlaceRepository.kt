package io.github.taetae98coding.diary.domain.place.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountPlaceRepository {
    public fun page(
        account: Account,
        query: String,
        sort: ListSort,
    ): Flow<PagingData<Place>>

    public fun get(
        account: Account,
        placeIdSet: Set<Uuid>,
    ): Flow<List<Place>>

    public fun get(
        account: Account,
        bounds: CoordinateBounds,
        sort: ListSort,
    ): Flow<List<Place>>

    public fun find(
        account: Account,
        placeId: Uuid,
    ): Flow<Place?>

    public suspend fun upsert(
        account: Account,
        place: Place,
        tagIdSet: Set<Uuid>,
    )

    public suspend fun updateDetail(
        account: Account,
        placeId: Uuid,
        detail: PlaceDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
