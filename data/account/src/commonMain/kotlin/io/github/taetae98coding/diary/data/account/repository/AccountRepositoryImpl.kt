package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.network.api.account.datasource.AccountRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.github.taetae98coding.diary.domain.account.repository.AccountRepository
import org.koin.core.annotation.Factory

@Factory
internal class AccountRepositoryImpl(
    private val accountRemoteDataSource: AccountRemoteDataSource,
) : AccountRepository {
    override suspend fun delete(appleAuthorization: AppleAuthorization?) {
        accountRemoteDataSource.delete(
            appleAuthorization =
                appleAuthorization?.let { authorization ->
                    AppleAuthorizationRemoteEntity(authorizationCode = authorization.code, clientId = authorization.clientId)
                },
        )
    }
}
