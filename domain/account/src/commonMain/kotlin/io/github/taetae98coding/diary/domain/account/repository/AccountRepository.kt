package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization

public interface AccountRepository {
    public suspend fun delete(appleAuthorization: AppleAuthorization?)
}
