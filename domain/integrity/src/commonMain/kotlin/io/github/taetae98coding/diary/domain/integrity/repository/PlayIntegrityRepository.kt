package io.github.taetae98coding.diary.domain.integrity.repository

import kotlinx.serialization.json.JsonObject

public interface PlayIntegrityRepository {
    public suspend fun fetch(): JsonObject?
}
