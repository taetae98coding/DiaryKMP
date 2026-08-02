package io.github.taetae98coding.diary.domain.tag.repository

import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.flow.Flow

public interface AccountTagFilterRepository {
    public fun getTopLevelOnly(account: Account): Flow<Boolean>

    public suspend fun upsert(
        account: Account,
        isTopLevelOnly: Boolean,
    )
}
