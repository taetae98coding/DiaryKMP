package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.flow.Flow

public interface UserDataRepository {
    public fun get(): Flow<UserData?>

    public suspend fun updateProfileImage(uri: FileUri)
}
