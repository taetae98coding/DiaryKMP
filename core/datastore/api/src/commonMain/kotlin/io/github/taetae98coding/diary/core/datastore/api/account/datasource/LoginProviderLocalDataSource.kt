package io.github.taetae98coding.diary.core.datastore.api.account.datasource

import io.github.taetae98coding.diary.core.datastore.api.account.entity.LoginProviderLocalEntity

public interface LoginProviderLocalDataSource {
    public suspend fun read(): LoginProviderLocalEntity?

    public suspend fun upsert(provider: LoginProviderLocalEntity?)
}
