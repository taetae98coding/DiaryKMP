package io.github.taetae98coding.diary.core.database.api.contact.datasource

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import kotlin.uuid.Uuid

public interface AccountContactSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<ContactLocalEntity>
}
