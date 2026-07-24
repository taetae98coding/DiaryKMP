package io.github.taetae98coding.diary.core.network.api.contact.datasource

import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity

public interface ContactRemoteDataSource {
    public suspend fun push(contactList: List<ContactRemoteEntity>)

    public suspend fun pull(usn: Long): List<ContactPullRemoteEntity>
}
