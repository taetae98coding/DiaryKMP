package io.github.taetae98coding.diary.data.web.repository

import io.github.taetae98coding.diary.core.datastore.api.web.datasource.WebLoginLocalDataSource
import io.github.taetae98coding.diary.core.model.web.WebLogin
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieDataSource
import io.github.taetae98coding.diary.data.web.WebLoginRestoringState
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.data.web.mapper.toLocal
import io.github.taetae98coding.diary.data.web.mapper.toWebView
import io.github.taetae98coding.diary.domain.web.repository.WebLoginRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class WebLoginRepositoryImpl(
    private val webViewCookieDataSource: WebViewCookieDataSource,
    private val webLoginLocalDataSource: WebLoginLocalDataSource,
    private val restoringState: WebLoginRestoringState,
) : WebLoginRepository {
    override suspend fun readWebViewLogin(siteUrlSet: Set<String>): WebLogin? = webViewCookieDataSource.readSessionCookie(siteUrlSet = siteUrlSet)?.toDomain()

    override suspend fun upsertWebViewLogin(login: WebLogin) {
        webViewCookieDataSource.upsertCookie(cookieList = login.sessionCookieList.map { cookie -> cookie.toWebView() })
    }

    override suspend fun flushWebViewLogin() {
        webViewCookieDataSource.flush()
    }

    override suspend fun readSavedLogin(): WebLogin = webLoginLocalDataSource.read().toDomain()

    override suspend fun submitSavedLogin(login: WebLogin) {
        webLoginLocalDataSource.upsert(login = login.toLocal())
    }

    override fun getRestoring(): Flow<Boolean> = restoringState.isRestoring

    override fun setRestoring() {
        restoringState.isRestoring.value = true
    }

    override fun unsetRestoring() {
        restoringState.isRestoring.value = false
    }
}
