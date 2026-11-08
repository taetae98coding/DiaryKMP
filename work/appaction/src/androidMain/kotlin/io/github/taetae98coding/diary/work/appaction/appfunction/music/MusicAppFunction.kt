package io.github.taetae98coding.diary.work.appaction.appfunction.music

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction

internal class MusicAppFunction(
    private val musicAppAction: MusicAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionMusicPage = appFunctionCall { musicAppAction.list(sort = sort, cursor = cursor).toAppFunctionMusicPage() }

    suspend fun get(musicId: String): AppFunctionMusic? = appFunctionCall { musicAppAction.get(musicId = musicId)?.toAppFunctionMusic() }

    suspend fun add(
        title: String,
        artist: String?,
        link: String?,
    ): AppFunctionId = appFunctionCall { musicAppAction.add(title = title, artist = artist, link = link).toAppFunctionId() }

    suspend fun update(
        musicId: String,
        title: String?,
        artist: String?,
        link: String?,
    ): AppFunctionChangedCount = appFunctionCall { musicAppAction.update(musicId = musicId, title = title, artist = artist, link = link).toAppFunctionChangedCount() }

    suspend fun delete(musicId: String): AppFunctionChangedCount = appFunctionCall { musicAppAction.delete(musicId = musicId).toAppFunctionChangedCount() }

    suspend fun restore(musicId: String): AppFunctionChangedCount = appFunctionCall { musicAppAction.restore(musicId = musicId).toAppFunctionChangedCount() }
}
