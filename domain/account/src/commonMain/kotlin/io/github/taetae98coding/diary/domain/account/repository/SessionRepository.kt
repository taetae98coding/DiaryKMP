package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.model.authentication.Session
import kotlinx.coroutines.flow.Flow

public interface SessionRepository {
    public fun get(): Flow<Session>

    public suspend fun create(credential: GoogleCredential)

    public suspend fun create(credential: AppleCredential)

    public suspend fun delete()
}
