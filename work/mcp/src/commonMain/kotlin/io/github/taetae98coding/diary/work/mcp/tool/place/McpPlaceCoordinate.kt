package io.github.taetae98coding.diary.work.mcp.tool.place

import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateOrNaN
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlinx.serialization.json.JsonPrimitive

internal fun coordinateParameter(
    name: String,
    description: String,
    isRequired: Boolean = false,
): McpToolParameter =
    McpToolParameter(
        name = name,
        type = McpToolParameterType.STRING,
        description = "$description Use a decimal number like 37.566500.",
        isRequired = isRequired,
    )

// AI 도구가 문자열 대신 JSON 숫자로 보내도 그 표기를 그대로 읽는다.
internal fun McpToolArguments.coordinateOrNull(name: String): Double? {
    val element = element(name) ?: return null
    val text = (element as? JsonPrimitive)?.content ?: throw McpToolArgumentException(message = "'$name' must be a decimal number like 37.566500.")

    return text.toPlaceCoordinateOrNaN()
}

internal fun McpToolArguments.coordinate(name: String): Double = coordinateOrNull(name) ?: throw McpToolArgumentException(message = "'$name' is required.")

internal val coordinateBoundsParameterList: List<McpToolParameter> =
    listOf(
        coordinateParameter(name = "south", description = "Southern latitude of the area.", isRequired = true),
        coordinateParameter(name = "north", description = "Northern latitude of the area.", isRequired = true),
        coordinateParameter(name = "west", description = "Western longitude of the area. It may be greater than east when the area crosses the 180th meridian.", isRequired = true),
        coordinateParameter(name = "east", description = "Eastern longitude of the area.", isRequired = true),
    )

// 지도는 항상 올바른 영역을 넘기므로 UseCase가 영역을 검사하지 않는다. 그래서 AI 도구가 넘긴 영역은 여기서 확인한다.
internal fun McpToolArguments.coordinateBounds(): CoordinateBounds {
    val bounds =
        CoordinateBounds(
            south = coordinate("south"),
            north = coordinate("north"),
            west = coordinate("west"),
            east = coordinate("east"),
        )

    bounds.validate()

    return bounds
}

private fun CoordinateBounds.validate() {
    validateRange()
    if (north < south) throw McpToolArgumentException(message = "'north' must not be below 'south'.")
}

private fun CoordinateBounds.validateRange() {
    if (south !in LATITUDE_RANGE || north !in LATITUDE_RANGE) throw McpToolArgumentException(message = "'south' and 'north' must be latitudes between -90 and 90.")
    if (west !in LONGITUDE_RANGE || east !in LONGITUDE_RANGE) throw McpToolArgumentException(message = "'west' and 'east' must be longitudes between -180 and 180.")
}

private val LATITUDE_RANGE = -90.0..90.0
private val LONGITUDE_RANGE = -180.0..180.0
