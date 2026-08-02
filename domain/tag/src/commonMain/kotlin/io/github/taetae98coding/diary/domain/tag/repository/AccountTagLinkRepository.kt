package io.github.taetae98coding.diary.domain.tag.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountTagLinkRepository {
    public fun getTagList(
        account: Account,
        fromTagId: Uuid,
    ): Flow<List<Tag>>

    public fun pageSelectableTag(
        account: Account,
        fromTagId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>>

    public suspend fun upsert(
        account: Account,
        fromTagId: Uuid,
        toTagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
