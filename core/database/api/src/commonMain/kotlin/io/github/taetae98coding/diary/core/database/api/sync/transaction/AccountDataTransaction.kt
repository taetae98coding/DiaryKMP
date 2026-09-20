package io.github.taetae98coding.diary.core.database.api.sync.transaction

import kotlin.uuid.Uuid

public interface AccountDataTransaction {
    public suspend fun delete(accountId: Uuid)
}
