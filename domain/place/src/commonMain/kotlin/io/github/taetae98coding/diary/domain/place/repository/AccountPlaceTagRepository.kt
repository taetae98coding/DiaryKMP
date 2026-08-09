package io.github.taetae98coding.diary.domain.place.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountPlaceTagRepository {
    public fun getTagList(
        account: Account,
        placeId: Uuid,
    ): Flow<List<Tag>>

    public fun pageSelectableTag(
        account: Account,
        placeId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>>

    public suspend fun upsert(
        account: Account,
        placeId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
