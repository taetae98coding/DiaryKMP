package io.github.taetae98coding.diary.work.mcp.tool.place

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.colorTextOrNull
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.idTextList
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory

private const val ADDRESS = "address"
private const val DESCRIPTION = "description"
private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val PLACE_ID = "placeId"
private const val TITLE = "title"

internal val placeSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val placeIdParameter: McpToolParameter =
    McpToolParameter(name = PLACE_ID, type = McpToolParameterType.STRING, description = "Id of the place.", isRequired = true)

@Factory(binds = [McpToolSet::class])
internal class PlaceMcpToolSet(
    private val placeAppAction: PlaceAppAction,
    private val memoListAppAction: MemoListAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "place_list",
                description = "Lists all places that are not deleted, as on the place home list.",
                parameterList = listOf(sortParameter(placeSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { placeAppAction.list(sort = arguments.sortText(placeSortList), cursor = arguments.cursorText()) }.toJson { place -> place.toJson() }
            },
            McpTool(
                name = "place_list_in_area",
                description = "Lists the places that are not deleted and lie inside a latitude and longitude area, as on the place home map. The whole result is returned at once.",
                parameterList = coordinateBoundsParameterList + sortParameter(placeSortList),
                kind = McpToolKind.READ,
            ) { arguments ->
                arguments.coordinateBounds()

                val placeList =
                    appActionCall {
                        placeAppAction.listInArea(
                            south = arguments.coordinateText("south"),
                            north = arguments.coordinateText("north"),
                            west = arguments.coordinateText("west"),
                            east = arguments.coordinateText("east"),
                            sort = arguments.sortText(placeSortList),
                        )
                    }

                buildJsonObject { put("items", placeList.toJsonArray { place -> place.toJson() }) }
            },
            McpTool(
                name = "place_get",
                description = "Gets one place with its linked tags. Returns null when the place does not exist or is deleted.",
                parameterList = listOf(placeIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { placeAppAction.get(placeId = arguments.idText(PLACE_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "place_memo_list",
                description = "Lists the memos linked to a place that are neither finished nor deleted.",
                parameterList = listOf(placeIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.placeList(placeId = arguments.idText(PLACE_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "place_memo_finished_list",
                description = "Lists the finished memos linked to a place that are not deleted.",
                parameterList = listOf(placeIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.placeFinishedList(placeId = arguments.idText(PLACE_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "place_add",
                description =
                    "Adds a place. The title must not be blank and latitude and longitude must be within range. " +
                        "Coordinates and address are not looked up, so pass the values you already know.",
                parameterList =
                    listOf(
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "Title of the place.", isRequired = true),
                        coordinateParameter(name = LATITUDE, description = "Latitude between -90 and 90.", isRequired = true),
                        coordinateParameter(name = LONGITUDE, description = "Longitude between -180 and 180.", isRequired = true),
                        McpToolParameter(name = ADDRESS, type = McpToolParameterType.STRING, description = "Address of the place."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the place."),
                        colorParameter(description = "Color of the place. A random color is used when omitted."),
                        McpToolParameter(name = "tagIds", type = McpToolParameterType.STRING_LIST, description = "Ids of tags to link."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments ->
                appActionCall {
                    placeAppAction.add(
                        title = arguments.string(TITLE),
                        description = arguments.stringOrNull(DESCRIPTION),
                        color = arguments.colorTextOrNull(),
                        latitude = arguments.coordinateText(LATITUDE),
                        longitude = arguments.coordinateText(LONGITUDE),
                        address = arguments.stringOrNull(ADDRESS),
                        tagIds = arguments.idTextList("tagIds"),
                    )
                }.toJson()
            },
            McpTool(
                name = "place_update",
                description =
                    "Updates the title, description, color, address or coordinates of a place. Omitted fields keep their current value. " +
                        "A blank title keeps the current title. Tags are changed with place_tag_add and place_tag_remove.",
                parameterList =
                    listOf(
                        placeIdParameter,
                        McpToolParameter(name = TITLE, type = McpToolParameterType.STRING, description = "New title."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        colorParameter(description = "New color."),
                        McpToolParameter(name = ADDRESS, type = McpToolParameterType.STRING, description = "New address."),
                        coordinateParameter(name = LATITUDE, description = "New latitude between -90 and 90."),
                        coordinateParameter(name = LONGITUDE, description = "New longitude between -180 and 180."),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updatePlace(arguments) },
            McpTool(
                name = "place_delete",
                description = "Deletes a place. Links to memos are kept. Use place_restore with the same id to undo.",
                parameterList = listOf(placeIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { placeAppAction.delete(placeId = arguments.idText(PLACE_ID)) }.toJson() },
            McpTool(
                name = "place_restore",
                description = "Restores a deleted place.",
                parameterList = listOf(placeIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { placeAppAction.restore(placeId = arguments.idText(PLACE_ID)) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "place",
                entityName = "a place",
                kind = AppActionEntityTagKind.PLACE,
                entityIdParameter = placeIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun updatePlace(arguments: McpToolArguments) =
        run {
            val placeId = arguments.uuid(PLACE_ID)
            val current = findPlaceUseCase.readActive(id = placeId, isDeleted = Place::isDeleted)?.detail ?: return@run changedCountResult(count = 0)
            val detail =
                PlaceDetail(
                    title = arguments.stringOrNull(TITLE) ?: current.title,
                    description = arguments.stringOrNull(DESCRIPTION) ?: current.description,
                    color = arguments.colorOrNull() ?: current.color,
                    coordinate =
                        Coordinate(
                            latitude = arguments.coordinateOrNull(LATITUDE) ?: current.coordinate.latitude,
                            longitude = arguments.coordinateOrNull(LONGITUDE) ?: current.coordinate.longitude,
                        ),
                    address = arguments.stringOrNull(ADDRESS) ?: current.address,
                )

            changedCountResult(updatePlaceUseCase(parameter = UpdatePlaceUseCase.Parameter(id = placeId, detail = detail)).getOrThrow())
        }
}
