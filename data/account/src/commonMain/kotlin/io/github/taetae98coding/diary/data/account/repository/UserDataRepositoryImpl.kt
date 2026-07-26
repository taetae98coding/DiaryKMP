package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileUri
import io.github.taetae98coding.diary.core.model.account.UserData
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
    private val fileReader: FileReader,
    private val profileImageRemoteDataSource: ProfileImageRemoteDataSource,
) : UserDataRepository {
    override fun get(): Flow<UserData?> = supabaseAuth.getUserFlow().map { user -> user?.toUserData() }

    override suspend fun updateProfileImage(uri: FileUri) {
        val source = fileReader.open(uri)

        profileImageRemoteDataSource.upload(
            mimeType = source.mimeType,
            contentLength = source.size,
            openContent = source::openSource,
        )
    }

    private fun SupabaseUser.toUserData(): UserData =
        UserData(
            id = id,
            email = email,
            profileImage = profileImage,
        )
}
