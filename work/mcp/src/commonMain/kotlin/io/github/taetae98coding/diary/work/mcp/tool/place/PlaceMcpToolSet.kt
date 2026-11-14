package io.github.taetae98coding.diary.work.mcp.tool.place

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.changeActive
import io.github.taetae98coding.diary.work.mcp.tool.isActive
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrRandom
import io.github.taetae98coding.diary.work.mcp.tool.json.colorParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.doneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.notDoneResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.pageActive
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset
import io.github.taetae98coding.diary.work.mcp.tool.paging.readPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.readActive
import io.github.taetae98coding.diary.work.mcp.tool.readFirst
import io.github.taetae98coding.diary.work.mcp.tool.tag.NOT_DONE_DESCRIPTION
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val ADDRESS = "address"
private const val DESCRIPTION = "description"
private const val LATITUDE = "latitude"
private const val LONGITUDE = "longitude"
private const val PLACE_ID = "placeId"
private const val TITLE = "title"

internal val placeSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

internal val placeIdParameter: McpToolParameter =
    McpToolParameter(name = PLACE_ID, type = McpToolParameterType.STRING, description = "Id of the place.", isRequired = true)

@Factory
internal class PlaceMcpToolSet(
    private val pagePlaceHomeUseCase: PagePlaceHomeUseCase,
    private val getPlaceListUseCase: GetPlaceListUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val pagePlaceMemoUseCase: PagePlaceMemoUseCase,
    private val pageFinishedPlaceMemoUseCase: PageFinishedPlaceMemoUseCase,
    private val addPlaceUseCase: AddPlaceUseCase,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "place_list",
                description = "Lists all places that are not deleted, as on the place home list.",
                parameterList = listOf(sortParameter(placeSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                pagePlaceHomeUseCase
                    .readPage(parameter = arguments.sort(placeSortList), offset = arguments.pageOffset())
                    .toJson { place -> place.toJson() }
            },
            McpTool(
                name = "place_list_in_area",
                description = "Lists the places that are not deleted and lie inside a latitude and longitude area, as on the place home map. The whole result is returned at once.",
                parameterList = coordinateBoundsParameterList + sortParameter(placeSortList),
                kind = McpToolKind.READ,
            ) { arguments ->
                val placeList =
                    getPlaceListUseCase.readFirst(
                        parameter = GetPlaceListUseCase.Parameter(bounds = arguments.coordinateBounds(), sort = arguments.sort(placeSortList)),
                    )

                buildJsonObject { put("items", placeList.toJsonArray { place -> place.toJson() }) }
            },
            McpTool(
                name = "place_get",
                description = "Gets one place with its linked tags. Returns null when the place does not exist or is deleted.",
                parameterList = listOf(placeIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> getPlace(arguments) },
            McpTool(
                name = "place_memo_list",
                description = "Lists the memos linked to a place that are neither finished nor deleted.",
                parameterList = listOf(placeIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PagePlaceMemoUseCase.Parameter(placeId = arguments.uuid(PLACE_ID), sort = arguments.sort(memoSortList))
                val offset = arguments.pageOffset()

                findPlaceUseCase.pageActive(id = parameter.placeId, isDeleted = Place::isDeleted) {
                    pagePlaceMemoUseCase.readPage(parameter = parameter, offset = offset).toJson { memo -> memo.toJson() }
                }
            },
            McpTool(
                name = "place_memo_finished_list",
                description = "Lists the finished memos linked to a place that are not deleted.",
                parameterList = listOf(placeIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                val parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = arguments.uuid(PLACE_ID), sort = arguments.sort(memoSortList))
                val offset = arguments.pageOffset()

                findPlaceUseCase.pageActive(id = parameter.placeId, isDeleted = Place::isDeleted) {
                    pageFinishedPlaceMemoUseCase.readPage(parameter = parameter, offset = offset).toJson { memo -> memo.toJson() }
                }
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
                val id =
                    addPlaceUseCase(
                        parameter =
                            AddPlaceUseCase.Parameter(
                                detail =
                                    PlaceDetail(
                                        title = arguments.string(TITLE),
                                        description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                                        color = arguments.colorOrRandom(),
                                        coordinate = Coordinate(latitude = arguments.coordinate(LATITUDE), longitude = arguments.coordinate(LONGITUDE)),
                                        address = arguments.stringOrNull(ADDRESS).orEmpty(),
                                    ),
                                tagIdSet = arguments.uuidSet("tagIds"),
                            ),
                    ).getOrThrow()

                idResult(id)
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
            ) { arguments -> findPlaceUseCase.changeActive(id = arguments.uuid(PLACE_ID), isDeleted = Place::isDeleted) { id -> deletePlaceUseCase(parameter = id) } },
            McpTool(
                name = "place_restore",
                description = "Restores a deleted place.",
                parameterList = listOf(placeIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> changedCountResult(restorePlaceUseCase(parameter = arguments.uuid(PLACE_ID)).getOrThrow()) },
            tagLinkTool(name = "place_tag_add", description = "Links a tag to a place.") { placeId, tagId ->
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId, tagId = tagId)).getOrThrow()
            },
            tagLinkTool(name = "place_tag_remove", description = "Unlinks a tag from a place.") { placeId, tagId ->
                removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId, tagId = tagId)).getOrThrow()
            },
        )

    private suspend fun getPlace(arguments: McpToolArguments) =
        run {
            val placeId = arguments.uuid(PLACE_ID)
            val place = findPlaceUseCase.readFirst(parameter = placeId)?.takeUnless { place -> place.isDeleted } ?: return@run JsonNull

            buildJsonObject {
                put("place", place.toJson())
                put("tags", getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = placeId)).toJsonArray { tag -> tag.toJson() })
            }
        }

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

    private fun tagLinkTool(
        name: String,
        description: String,
        link: suspend (placeId: Uuid, tagId: Uuid) -> Unit,
    ): McpTool =
        McpTool(
            name = name,
            description = "$description $NOT_DONE_DESCRIPTION",
            parameterList =
                listOf(
                    placeIdParameter,
                    McpToolParameter(name = "tagId", type = McpToolParameterType.STRING, description = "Id of the tag.", isRequired = true),
                ),
            kind = McpToolKind.WRITE,
        ) { arguments: McpToolArguments ->
            val entityId = arguments.uuid(PLACE_ID)
            val tagId = arguments.uuid("tagId")
            val isLinkable = findPlaceUseCase.isActive(id = entityId, isDeleted = Place::isDeleted) && findTagUseCase.isActive(id = tagId, isDeleted = Tag::isDeleted)

            if (isLinkable) {
                link(entityId, tagId)
                doneResult
            } else {
                notDoneResult
            }
        }
}
