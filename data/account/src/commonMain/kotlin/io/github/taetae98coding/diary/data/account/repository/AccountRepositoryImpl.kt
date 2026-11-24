package io.github.taetae98coding.diary.data.account.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.network.api.account.datasource.AccountRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.github.taetae98coding.diary.domain.account.repository.AccountRepository

@Inject
@ContributesBinding(AppScope::class)
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
