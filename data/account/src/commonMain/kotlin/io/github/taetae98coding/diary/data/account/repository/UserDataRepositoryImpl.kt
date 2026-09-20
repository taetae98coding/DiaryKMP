package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
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

    override suspend fun updateProfileImage(uri: FileUri) {
        imageConverter.toJpeg(uri = uri).use { jpeg ->
            profileImageRemoteDataSource.upload(
                mimeType = JPEG_MIME_TYPE,
                contentLength = jpeg.size,
                openContent = jpeg::openSource,
            )
        }
    }

    private fun SupabaseUser.toUserData(): UserData =
        UserData(
            id = id,
            email = email,
            profileImage = profileImage,
        )
}
