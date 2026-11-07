package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.account.datasource.LoginProviderLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.account.entity.LoginProviderLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.LoginProviderData
import io.github.taetae98coding.diary.core.datastore.impl.di.LoginProviderDataStore
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
internal class LoginProviderLocalDataSourceImpl(
    @LoginProviderDataStore
    private val dataStore: DataStore<LoginProviderData>,
) : LoginProviderLocalDataSource {
    override suspend fun read(): LoginProviderLocalEntity? =
        LoginProviderLocalEntity.fromPersistentValue(
            persistentValue = dataStore.data.first().provider,
        )

    override suspend fun upsert(provider: LoginProviderLocalEntity?) {
        dataStore.updateData { LoginProviderData(provider = provider?.persistentValue.orEmpty()) }
    }
}
