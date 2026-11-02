package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.datastore.api.account.datasource.LoginProviderLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.account.entity.LoginProviderLocalEntity
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import org.koin.core.annotation.Factory

@Factory
internal class LoginProviderRepositoryImpl(
    private val loginProviderLocalDataSource: LoginProviderLocalDataSource,
) : LoginProviderRepository {
    override suspend fun read(): LoginProvider? = loginProviderLocalDataSource.read()?.toDomain()

    override suspend fun set(provider: LoginProvider) {
        loginProviderLocalDataSource.upsert(provider = provider.toLocal())
    }

    override suspend fun unset() {
        loginProviderLocalDataSource.upsert(provider = null)
    }
}

private fun LoginProviderLocalEntity.toDomain(): LoginProvider =
    when (this) {
        LoginProviderLocalEntity.GOOGLE -> LoginProvider.GOOGLE
        LoginProviderLocalEntity.APPLE -> LoginProvider.APPLE
    }

private fun LoginProvider.toLocal(): LoginProviderLocalEntity =
    when (this) {
        LoginProvider.GOOGLE -> LoginProviderLocalEntity.GOOGLE
        LoginProvider.APPLE -> LoginProviderLocalEntity.APPLE
    }
