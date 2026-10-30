package io.github.taetae98coding.diary.domain.web.repository

import io.github.taetae98coding.diary.core.model.web.WebLogin
import kotlinx.coroutines.flow.Flow

public interface WebLoginRepository {
    /** 세션 쿠키를 확인할 수 없는 웹 표시 수단이면 `null`이다. */
    public suspend fun readWebViewLogin(siteUrlSet: Set<String>): WebLogin?

    public suspend fun upsertWebViewLogin(login: WebLogin)

    public suspend fun flushWebViewLogin()

    public suspend fun readSavedLogin(): WebLogin

    public suspend fun submitSavedLogin(login: WebLogin)

    public fun getRestoring(): Flow<Boolean>

    public fun setRestoring()

    public fun unsetRestoring()
}
