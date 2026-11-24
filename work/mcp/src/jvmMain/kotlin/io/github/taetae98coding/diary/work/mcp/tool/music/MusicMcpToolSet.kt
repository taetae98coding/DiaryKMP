package io.github.taetae98coding.diary.work.mcp.tool.music

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusicDetail
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.idTextList
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagIdsParameter
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ARTIST = "artist"
private const val MUSIC_ID = "musicId"
private const val TITLE = "title"

internal val musicSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

private val musicIdParameter: McpToolParameter =
    McpToolParameter(name = MUSIC_ID, type = McpToolParameterType.STRING, description = "Id of the music.", isRequired = true)

private const val LINK_DESCRIPTION = "A YouTube video link. An empty string means no link. A non-empty link that is not a YouTube link is rejected."

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<McpToolSet>())
internal class MusicMcpToolSet(
    private val musicAppAction: MusicAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findMusicUseCase: FindMusicUseCase,
    private val updateMusicUseCase: UpdateMusicUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "music_list",
                description = "Lists the music in the playlist that is not deleted.",
                parameterList = listOf(sortParameter(musicSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { musicAppAction.list(sort = arguments.sortText(musicSortList), cursor = arguments.cursorText()) }.toJson { music -> music.toJson() }
            },
            McpTool(
                name = "music_get",
                description = "Gets one music in the playlist with its linked tags. Returns null when the music does not exist or is deleted.",
                parameterList = listOf(musicIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { musicAppAction.get(musicId = arguments.idText(MUSIC_ID)) }?.toJson() ?: JsonNull },
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
                        tagIdsParameter,
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    musicAppAction.add(
                        title = arguments.string(TITLE),
                        artist = arguments.stringOrNull(ARTIST),
                        link = arguments.stringOrNull("link"),
                        tagIds = arguments.idTextList("tagIds"),
                    )
                }.toJson()
            },
            McpTool(
                name = "music_update",
                description =
                    "Updates the title, artist or link of a music. Omitted fields keep their current value. A blank title keeps the current title. " +
                        "Tags are changed with music_tag_add and music_tag_remove.",
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
            ) { arguments -> appActionCall { musicAppAction.delete(musicId = arguments.idText(MUSIC_ID)) }.toJson() },
            McpTool(
                name = "music_restore",
                description = "Restores a deleted music.",
                parameterList = listOf(musicIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { musicAppAction.restore(musicId = arguments.idText(MUSIC_ID)) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "music",
                entityName = "a music",
                kind = AppActionEntityTagKind.MUSIC,
                entityIdParameter = musicIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun updateMusic(arguments: McpToolArguments): JsonElement {
        val musicId = arguments.uuid(MUSIC_ID)
        val current = findMusicUseCase.readActive(id = musicId, isDeleted = Music::isDeleted)?.detail ?: return changedCountResult(count = 0)
        val detail =
            MusicDetail(
                title = arguments.stringOrNull(TITLE) ?: current.title,
                artist = arguments.stringOrNull(ARTIST) ?: current.artist,
                link = arguments.stringOrNull("link") ?: current.link,
            )

        return changedCountResult(updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = musicId, detail = detail)).getOrThrow())
    }
}

internal fun AppActionMusic.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put(TITLE, title)
        put(ARTIST, artist)
        put("link", link)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

private fun AppActionMusicDetail.toJson(): JsonObject =
    buildJsonObject {
        put("music", music.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
    }
