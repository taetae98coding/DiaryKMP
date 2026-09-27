package io.github.taetae98coding.diary.core.network.api.integrity.datasource

import kotlinx.serialization.json.JsonObject

public interface PlayIntegrityRemoteDataSource {
    public suspend fun decode(
        token: String,
        packageName: String,
    ): JsonObject
}
