package io.github.taetae98coding.diary.work.mcp.tool.music

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val ARTIST = "artist"
private const val MUSIC_ID = "musicId"
private const val TITLE = "title"

private val musicSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

private val musicIdParameter: McpToolParameter =
    McpToolParameter(name = MUSIC_ID, type = McpToolParameterType.STRING, description = "Id of the music.", isRequired = true)

private const val LINK_DESCRIPTION = "A YouTube video link. An empty string means no link. A non-empty link that is not a YouTube link is rejected."

@Factory
internal class MusicMcpToolSet(
    private val pageMusicUseCase: PageMusicUseCase,
    private val findMusicUseCase: FindMusicUseCase,
    private val addMusicUseCase: AddMusicUseCase,
    private val updateMusicUseCase: UpdateMusicUseCase,
    private val deleteMusicUseCase: DeleteMusicUseCase,
    private val restoreMusicUseCase: RestoreMusicUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "music_list",
                description = "Lists the music in the playlist that is not deleted.",
                parameterList = listOf(sortParameter(musicSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pageMusicUseCase
                    .readPage(parameter = arguments.sort(musicSortList), offset = arguments.pageOffset())
                    .toJson { music -> music.toJson() }
            },
            McpTool(
                name = "music_get",
                description = "Gets one music in the playlist. Returns null when the music does not exist.",
                parameterList = listOf(musicIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> findMusicUseCase.readFirst(parameter = arguments.uuid(MUSIC_ID))?.toJson() ?: JsonNull },
            McpTool(
                name = "music_add",
                description =
                    "Adds a music to the playlist. The title must not be blank. " +
                        "Video information is not fetched from the link, so give the title and artist yourself.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the music.", isRequired = true),
                        McpToolParameter(name = ARTIST, type = McpToolParameterType.STRING, description = "Artist of the music."),
                        McpToolParameter(name = "link", type = McpToolParameterType.STRING, description = LINK_DESCRIPTION),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                val id =
                    addMusicUseCase(
                        parameter =
                            AddMusicUseCase.Parameter(
                                detail =
                                    MusicDetail(
                                        title = arguments.string(TITLE),
                                        artist = arguments.stringOrNull(ARTIST).orEmpty(),
                                        link = arguments.stringOrNull("link").orEmpty(),
                                    ),
                                tagIdSet = emptySet(),
                            ),
                    ).getOrThrow()

                idResult(id)
            },
            McpTool(
                name = "music_update",
                description = "Updates the title, artist or link of a music. Omitted fields keep their current value. A blank title keeps the current title.",
                parameterList =
                    listOf(
                        musicIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = ARTIST, type = McpToolParameterType.STRING, description = "New artist."),
                        McpToolParameter(name = "link", type = McpToolParameterType.STRING, description = LINK_DESCRIPTION),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateMusic(arguments) },
            McpTool(
                name = "music_delete",
                description = "Deletes a music from the playlist. Use music_restore with the same id to undo.",
                parameterList = listOf(musicIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> changedCountResult(deleteMusicUseCase(parameter = arguments.uuid(MUSIC_ID)).getOrThrow()) },
            McpTool(
                name = "music_restore",
                description = "Restores a deleted music.",
                parameterList = listOf(musicIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restoreMusicUseCase(parameter = arguments.uuid(MUSIC_ID)).getOrThrow()) },
        )

    private suspend fun updateMusic(arguments: McpToolArguments): JsonElement {
        val musicId = arguments.uuid(MUSIC_ID)
        val current = findMusicUseCase.readFirst(parameter = musicId)?.detail ?: return changedCountResult(count = 0)
        val detail =
            MusicDetail(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                artist = arguments.stringOrNull(ARTIST) ?: current.artist,
                link = arguments.stringOrNull("link") ?: current.link,
            )

        return changedCountResult(updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = musicId, detail = detail)).getOrThrow())
    }
}

private fun Music.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, detail.title)
        put(ARTIST, detail.artist)
        put("link", detail.link)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
