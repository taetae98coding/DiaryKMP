package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.web.datasource.WebLoginLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.WebLoginCookieData
import io.github.taetae98coding.diary.core.datastore.impl.WebLoginData
import io.github.taetae98coding.diary.core.datastore.impl.di.WebLoginDataStore
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
internal class WebLoginLocalDataSourceImpl(
    @WebLoginDataStore
    private val dataStore: DataStore<WebLoginData>,
) : WebLoginLocalDataSource {
    override suspend fun read(): WebLoginLocalEntity = dataStore.data.first().toLocalEntity()

    override suspend fun upsert(login: WebLoginLocalEntity) {
        dataStore.updateData { login.toData() }
    }
}

private fun WebLoginData.toLocalEntity(): WebLoginLocalEntity =
    WebLoginLocalEntity(
        sessionCookieList = sessionCookieList.map { cookie -> cookie.toLocalEntity() },
        siteUrlSet = siteUrlSet,
    )

private fun WebLoginCookieData.toLocalEntity(): WebLoginCookieLocalEntity =
    WebLoginCookieLocalEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            WebLoginCookieSameSiteLocalEntity.entries.firstOrNull { entity -> entity.persistentValue == sameSite }
                ?: WebLoginCookieSameSiteLocalEntity.UNSPECIFIED,
    )

private fun WebLoginLocalEntity.toData(): WebLoginData =
    WebLoginData(
        sessionCookieList = sessionCookieList.map { cookie -> cookie.toData() },
        siteUrlSet = siteUrlSet,
    )

private fun WebLoginCookieLocalEntity.toData(): WebLoginCookieData =
    WebLoginCookieData(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite = sameSite.persistentValue,
    )
