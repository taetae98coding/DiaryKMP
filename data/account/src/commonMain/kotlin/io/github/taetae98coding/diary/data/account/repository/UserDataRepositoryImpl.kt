package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import io.github.taetae98coding.diary.core.network.api.profile.datasource.ProfileImageRemoteDataSource
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class UserDataRepositoryImpl(
    private val supabaseAuth: SupabaseAuth,
    private val imageConverter: ImageConverter,
    private val fileLocalDataSource: FileLocalDataSource,
    private val profileImageRemoteDataSource: ProfileImageRemoteDataSource,
) : UserDataRepository {
    override fun get(): Flow<UserData?> = supabaseAuth.getUserFlow().map { user -> user?.toUserData() }

    override suspend fun updateProfileImage(
        uri: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    ) {
        val converted = imageConverter.convert(source = uri, format = format, cropRegion = cropRegion, maxSideLength = maxSideLength, quality = quality)

        try {
            profileImageRemoteDataSource.upload(
                mimeType = format.toMimeType(),
                contentLength = fileLocalDataSource.size(uri = converted),
                openContent = { fileLocalDataSource.openSource(uri = converted) },
            )
        } finally {
            fileLocalDataSource.delete(uri = converted)
        }
    }

    override suspend fun refresh() {
        supabaseAuth.retrieveUserForCurrentSession()
    }

    private fun ImageFormat.toMimeType(): String =
        when (this) {
            ImageFormat.JPEG -> "image/jpeg"
        }

    private fun SupabaseUser.toUserData(): UserData =
        UserData(
            id = id,
            email = email,
            profileImage = profileImage,
        )
}
