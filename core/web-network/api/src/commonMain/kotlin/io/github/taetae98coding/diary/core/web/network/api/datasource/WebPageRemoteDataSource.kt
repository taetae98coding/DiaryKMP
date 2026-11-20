package io.github.taetae98coding.diary.core.web.network.api.datasource

import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageRemoteEntity

public interface WebPageRemoteDataSource {
    public suspend fun read(
        url: String,
        headerList: List<WebPageHeaderRemoteEntity>,
    ): WebPageRemoteEntity
}
