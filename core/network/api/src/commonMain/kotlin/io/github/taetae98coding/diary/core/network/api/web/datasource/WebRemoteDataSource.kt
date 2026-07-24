package io.github.taetae98coding.diary.core.network.api.web.datasource

import io.github.taetae98coding.diary.core.network.api.web.entity.WebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity

public interface WebRemoteDataSource {
    public suspend fun push(webList: List<WebRemoteEntity>)

    public suspend fun pull(usn: Long): List<WebPullRemoteEntity>
}
