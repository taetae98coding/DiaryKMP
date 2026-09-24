package io.github.taetae98coding.diary.core.network.api.profile.datasource

import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import kotlinx.io.RawSource

public interface ProfileImageRemoteDataSource {
    public suspend fun upload(
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
    ): ProfileImageRemoteEntity
}
