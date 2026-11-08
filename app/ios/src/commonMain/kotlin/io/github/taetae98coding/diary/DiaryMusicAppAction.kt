package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction
import io.github.taetae98coding.diary.work.appaction.music.filterBySearchQuery

public data class DiaryAppActionMusic(
    val id: String,
    val title: String,
    val artist: String,
    val link: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionMusicPage(
    val items: List<DiaryAppActionMusic>,
    val nextPageNumber: Int?,
)

public object DiaryMusicAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionMusicPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> musicAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionMusicPage(items = page.items.map { music -> music.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun search(query: String): List<DiaryAppActionMusic> =
        readNumberedPage(pageNumber = SEARCH_PAGE_NUMBER) { cursor -> musicAppAction().list(sort = null, cursor = cursor) }
            .items
            .filterBySearchQuery(query = query)
            .map { music -> music.toDiary() }

    public suspend fun get(musicId: String): DiaryAppActionMusic? = musicAppAction().get(musicId = musicId)?.toDiary()

    public suspend fun add(
        title: String,
        artist: String?,
        link: String?,
    ): String = musicAppAction().add(title = title, artist = artist, link = link).id

    public suspend fun update(
        musicId: String,
        title: String?,
        artist: String?,
        link: String?,
    ): Int = musicAppAction().update(musicId = musicId, title = title, artist = artist, link = link).changedCount

    public suspend fun delete(musicId: String): Int = musicAppAction().delete(musicId = musicId).changedCount

    public suspend fun restore(musicId: String): Int = musicAppAction().restore(musicId = musicId).changedCount
}

private fun musicAppAction(): MusicAppAction = diaryAppAction()

private fun AppActionMusic.toDiary(): DiaryAppActionMusic =
    DiaryAppActionMusic(
        id = id,
        title = title,
        artist = artist,
        link = link,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )
