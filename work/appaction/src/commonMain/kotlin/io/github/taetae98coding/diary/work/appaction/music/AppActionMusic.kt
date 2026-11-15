package io.github.taetae98coding.diary.work.appaction.music

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.work.appaction.core.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag

public data class AppActionMusic(
    val id: String,
    val title: String,
    val artist: String,
    val link: String,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionMusicDetail(
    val music: AppActionMusic,
    val tags: List<AppActionTag>,
)

internal fun Music.toAppActionMusic(): AppActionMusic =
    AppActionMusic(
        id = id.toString(),
        title = detail.title,
        artist = detail.artist,
        link = detail.link,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

public fun List<AppActionMusic>.filterBySearchQuery(query: String): List<AppActionMusic> = filterBySearchQuery(query = query) { music -> listOf(music.title, music.artist) }
