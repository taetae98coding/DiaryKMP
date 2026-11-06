package io.github.taetae98coding.diary.domain.tag.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountEntityTagRepository {
    public fun getTagList(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
    ): Flow<List<Tag>>

    public fun pageSelectableTag(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
        query: String,
    ): Flow<PagingData<Tag>>

    public suspend fun upsert(
        account: Account,
        kind: EntityTagKind,
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
