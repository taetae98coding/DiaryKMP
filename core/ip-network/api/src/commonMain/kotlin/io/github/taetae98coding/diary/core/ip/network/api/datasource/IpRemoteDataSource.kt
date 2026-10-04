package io.github.taetae98coding.diary.core.ip.network.api.datasource

import io.github.taetae98coding.diary.core.ip.network.api.entity.IpRemoteEntity

public interface IpRemoteDataSource {
    public suspend fun get(): IpRemoteEntity
}
