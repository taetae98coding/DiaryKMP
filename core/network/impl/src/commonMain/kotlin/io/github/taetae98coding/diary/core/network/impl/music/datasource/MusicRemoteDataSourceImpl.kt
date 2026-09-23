package io.github.taetae98coding.diary.core.network.impl.music.datasource

import io.github.taetae98coding.diary.core.network.api.music.datasource.MusicRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.music.entity.MusicPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.music.entity.MusicPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class MusicRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : MusicRemoteDataSource {
    override suspend fun push(musicList: List<MusicRemoteEntity>) {
        supabaseFunction(
            function = PUSH_MUSIC_FUNCTION,
            body = MusicPushRequestRemoteEntity(musicList = musicList),
        )
    }

    override suspend fun pull(usn: Long): List<MusicPullRemoteEntity> =
        supabaseFunction(
            function = PULL_MUSIC_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<MusicPullResponseRemoteEntity>().musicList

    private companion object {
        const val PUSH_MUSIC_FUNCTION: String = "v1-sync-push-music"
        const val PULL_MUSIC_FUNCTION: String = "v1-sync-pull-music"
    }
}
