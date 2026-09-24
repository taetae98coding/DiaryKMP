package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.place.Place
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoPlaceRepository {
    public fun getPlaceList(
        account: Account,
        memoId: Uuid,
    ): Flow<List<Place>>

    public suspend fun findPlaceIdSet(
        account: Account,
        memoId: Uuid,
    ): Set<Uuid>

    public suspend fun upsert(
        account: Account,
        memoId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
