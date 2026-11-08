package io.github.taetae98coding.diary.work.appaction.music

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory

private const val MUSIC_ID = "musicId"

internal val musicSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class MusicAppAction internal constructor(
    private val pageMusicUseCase: PageMusicUseCase,
    private val findMusicUseCase: FindMusicUseCase,
    private val addMusicUseCase: AddMusicUseCase,
    private val updateMusicUseCase: UpdateMusicUseCase,
    private val deleteMusicUseCase: DeleteMusicUseCase,
    private val restoreMusicUseCase: RestoreMusicUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMusic> =
        runAppAction {
            pageMusicUseCase
                .readPage(parameter = sort.toSortArgument(musicSortList), offset = cursor.toPageOffset())
                .map { music -> music.toAppActionMusic() }
        }

    public suspend fun get(musicId: String): AppActionMusic? = runAppAction { findMusicUseCase.readFirst(parameter = musicId.toUuidArgument(name = MUSIC_ID))?.toAppActionMusic() }

    public suspend fun add(
        title: String,
        artist: String?,
        link: String?,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddMusicUseCase.Parameter(
                    detail = MusicDetail(title = title, artist = artist.orEmpty(), link = link.orEmpty()),
                    tagIdSet = emptySet(),
                )

            addMusicUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        musicId: String,
        title: String?,
        artist: String?,
        link: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = musicId.toUuidArgument(name = MUSIC_ID)
            val current = findMusicUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                MusicDetail(
                    title = title ?: current.title,
                    artist = artist ?: current.artist,
                    link = link ?: current.link,
                )

            updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun delete(musicId: String): AppActionChangedCount = runAppAction { deleteMusicUseCase(parameter = musicId.toUuidArgument(name = MUSIC_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(musicId: String): AppActionChangedCount = runAppAction { restoreMusicUseCase(parameter = musicId.toUuidArgument(name = MUSIC_ID)).getOrThrow().toAppActionChangedCount() }
}
