package io.github.taetae98coding.diary.core.network.api.account.datasource

import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity

public interface AccountRemoteDataSource {
    public suspend fun delete(appleAuthorization: AppleAuthorizationRemoteEntity?)
}
