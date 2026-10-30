package io.github.taetae98coding.diary.core.datastore.api.web.datasource

import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginLocalEntity

public interface WebLoginLocalDataSource {
    public suspend fun read(): WebLoginLocalEntity

    public suspend fun upsert(login: WebLoginLocalEntity)
}
