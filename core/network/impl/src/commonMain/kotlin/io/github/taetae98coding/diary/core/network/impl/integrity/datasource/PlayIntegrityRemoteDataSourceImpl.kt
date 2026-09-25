package io.github.taetae98coding.diary.core.network.impl.integrity.datasource

import io.github.taetae98coding.diary.core.network.api.integrity.datasource.PlayIntegrityRemoteDataSource
import io.github.taetae98coding.diary.core.network.impl.integrity.entity.PlayIntegrityDecodeRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Factory

@Factory
internal class PlayIntegrityRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : PlayIntegrityRemoteDataSource {
    override suspend fun decode(
        token: String,
        packageName: String,
    ): JsonObject =
        supabaseFunction(
            function = DECODE_PLAY_INTEGRITY_FUNCTION,
            body = PlayIntegrityDecodeRequestRemoteEntity(token = token, packageName = packageName),
        ).body()

    private companion object {
        const val DECODE_PLAY_INTEGRITY_FUNCTION: String = "v1-play-integrity-decode"
    }
}
