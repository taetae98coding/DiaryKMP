package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.network.api.profile.datasource.ProfileImageRemoteDataSource
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

private const val JPEG_MIME_TYPE = "image/jpeg"

@Factory
internal class UserDataRepositoryImpl(
    private val supabaseAuth: SupabaseAuth,
    private val imageConverter: ImageConverter,
    private val profileImageRemoteDataSource: ProfileImageRemoteDataSource,
) : UserDataRepository {
    override fun get(): Flow<UserData?> = supabaseAuth.getUserFlow().map { user -> user?.toUserData() }

    override suspend fun updateProfileImage(
        uri: FileUri,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        jpegQuality: Int,
    ) {
        imageConverter.toJpeg(uri = uri, cropRegion = cropRegion, maxSideLength = maxSideLength, jpegQuality = jpegQuality).use { jpeg ->
            profileImageRemoteDataSource.upload(
                mimeType = JPEG_MIME_TYPE,
                contentLength = jpeg.size,
                openContent = jpeg::openSource,
            )
        }
    }

    override suspend fun refresh() {
        supabaseAuth.retrieveUserForCurrentSession()
    }

    private fun SupabaseUser.toUserData(): UserData =
        UserData(
            id = id,
            email = email,
            profileImage = profileImage,
        )
}
