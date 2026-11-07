package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.model.authentication.LoginProvider

public interface LoginProviderRepository {
    public suspend fun read(): LoginProvider?

    public suspend fun set(provider: LoginProvider)

    public suspend fun unset()
}
