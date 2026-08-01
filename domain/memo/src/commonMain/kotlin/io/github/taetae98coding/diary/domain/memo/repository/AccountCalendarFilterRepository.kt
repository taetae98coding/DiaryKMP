package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountCalendarFilterRepository {
    public fun getTagList(account: Account): Flow<List<Tag>>

    public suspend fun upsert(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun delete(
        account: Account,
        tagId: Uuid,
    )

    public suspend fun deleteAll(account: Account)
}
