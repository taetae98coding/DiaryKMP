package io.github.taetae98coding.diary.domain.web.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountWebTagRepository {
    public fun getTagList(
        account: Account,
        webId: Uuid,
    ): Flow<List<Tag>>

    public fun pageSelectableTag(
        account: Account,
        webId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>>

    public suspend fun upsert(
        account: Account,
        webId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
