package io.github.taetae98coding.diary.work.mcp.tool.place

import io.github.taetae98coding.diary.work.appaction.place.AppActionPlaceDetail
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

internal fun AppActionPlaceDetail.toJson(): JsonObject =
    buildJsonObject {
        put("place", place.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
    }
