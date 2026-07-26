package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.file.api.FileUri
import io.github.taetae98coding.diary.core.model.account.UserData
import kotlinx.coroutines.flow.Flow

public interface UserDataRepository {
    public fun get(): Flow<UserData?>

    public suspend fun updateProfileImage(uri: FileUri)
}
