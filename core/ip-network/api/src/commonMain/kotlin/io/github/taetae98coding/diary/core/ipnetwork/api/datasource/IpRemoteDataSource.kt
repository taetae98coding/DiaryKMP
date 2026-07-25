package io.github.taetae98coding.diary.core.ipnetwork.api.datasource

import io.github.taetae98coding.diary.core.ipnetwork.api.entity.IpRemoteEntity

public interface IpRemoteDataSource {
    public suspend fun get(): IpRemoteEntity
}
