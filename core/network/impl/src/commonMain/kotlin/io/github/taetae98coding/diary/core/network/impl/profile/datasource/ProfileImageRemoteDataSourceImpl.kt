package io.github.taetae98coding.diary.core.network.impl.profile.datasource

import io.github.taetae98coding.diary.core.network.api.profile.datasource.ProfileImageRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.content.RawSourceContent
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import io.ktor.http.ContentType
import kotlinx.io.RawSource
import org.koin.core.annotation.Factory

@Factory
internal class ProfileImageRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : ProfileImageRemoteDataSource {
    override suspend fun upload(
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
    ): ProfileImageRemoteEntity =
        supabaseFunction(
            function = "v1-profile-upload",
            body =
                RawSourceContent(
                    contentType = ContentType.parse(mimeType),
                    contentLength = contentLength,
                    openContent = openContent,
                ),
        ).body()
}
