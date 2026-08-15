package io.github.taetae98coding.diary.core.webnetwork.api.datasource

import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageRemoteEntity

public interface WebPageRemoteDataSource {
    public suspend fun get(
        url: String,
        headerList: List<WebPageHeaderRemoteEntity>,
    ): WebPageRemoteEntity
}
