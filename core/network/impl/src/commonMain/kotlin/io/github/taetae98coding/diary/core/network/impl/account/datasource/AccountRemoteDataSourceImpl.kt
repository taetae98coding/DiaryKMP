package io.github.taetae98coding.diary.core.network.impl.account.datasource

import io.github.taetae98coding.diary.core.network.api.account.datasource.AccountRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.account.entity.AccountDeleteRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import org.koin.core.annotation.Factory

@Factory
internal class AccountRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : AccountRemoteDataSource {
    override suspend fun delete(appleAuthorization: AppleAuthorizationRemoteEntity?) {
        supabaseFunction(
            function = DELETE_ACCOUNT_FUNCTION,
            body = AccountDeleteRequestRemoteEntity(apple = appleAuthorization),
        )
    }

    private companion object {
        const val DELETE_ACCOUNT_FUNCTION: String = "v1-account-delete"
    }
}
