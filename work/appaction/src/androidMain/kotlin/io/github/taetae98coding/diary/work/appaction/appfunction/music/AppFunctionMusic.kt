package io.github.taetae98coding.diary.work.appaction.appfunction.music

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusicDetail

/** A music in the playlist. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMusic(
    /** Id of the music. */
    val id: String,
    /** Title of the music. */
    val title: String,
    /** Artist of the music. */
    val artist: String,
    /** YouTube video link of the music. Empty when the music has no link. */
    val link: String,
    /** When the music was created, in ISO-8601. */
    val createdAt: String,
    /** When the music was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** A music in the playlist with its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMusicDetail(
    /** The music. */
    val music: AppFunctionMusic,
    /** Tags linked to the music. */
    val tags: List<AppFunctionTag>,
)

/** One page of music in the playlist. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionMusicPage(
    /** Music in this page, at most 100. */
    val items: List<AppFunctionMusic>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

internal fun AppActionMusic.toAppFunctionMusic(): AppFunctionMusic =
    AppFunctionMusic(
        id = id,
        title = title,
        artist = artist,
        link = link,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionPage<AppActionMusic>.toAppFunctionMusicPage(): AppFunctionMusicPage = AppFunctionMusicPage(items = items.map { music -> music.toAppFunctionMusic() }, nextCursor = nextCursor)

internal fun AppActionMusicDetail.toAppFunctionMusicDetail(): AppFunctionMusicDetail = AppFunctionMusicDetail(music = music.toAppFunctionMusic(), tags = tags.map { tag -> tag.toAppFunctionTag() })
