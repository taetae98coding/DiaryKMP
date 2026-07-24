package io.github.taetae98coding.diary.core.network.api.webtag.datasource

import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity

public interface WebTagRemoteDataSource {
    public suspend fun push(webTagList: List<WebTagRemoteEntity>)

    public suspend fun pull(usn: Long): List<WebTagPullRemoteEntity>
}
