package io.github.taetae98coding.diary.domain.account.repository

import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import kotlinx.coroutines.flow.Flow

public interface UserDataRepository {
    public fun get(): Flow<UserData?>

    public suspend fun updateProfileImage(
        uri: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    )

    public suspend fun refresh()
}
