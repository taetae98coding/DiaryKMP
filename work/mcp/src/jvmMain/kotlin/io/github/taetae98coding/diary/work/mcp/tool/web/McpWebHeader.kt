package io.github.taetae98coding.diary.work.mcp.tool.web

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebDetail
import io.github.taetae98coding.diary.work.appaction.web.AppActionWebWithHeaders
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val HEADERS = "headers"

// 헤더는 순서를 지키고 같은 이름도 각각 기록하므로, 이름을 키로 쓰는 객체 대신 "이름: 값" 문자열 목록으로 받는다.
internal fun headersParameter(description: String): McpToolParameter =
    McpToolParameter(
        name = HEADERS,
        type = McpToolParameterType.STRING_LIST,
        description = "$description Each item is \"Name: value\", split at the first colon. Order is kept and duplicate names are allowed.",
    )

internal fun McpToolArguments.headerListOrNull(): List<WebHeader>? {
    if (!has(HEADERS)) return null

    return stringList(HEADERS).map { text ->
        if (!text.contains(':')) throw McpToolArgumentException(message = "Each '$HEADERS' item must be \"Name: value\".")

        WebHeader(
            name = text.substringBefore(':'),
            value = text.substringAfter(':').removePrefix(" "),
        )
    }
}

// 공통 동작은 헤더를 글 목록으로 받아 같은 규칙으로 읽으므로, 확인한 목록을 받은 표기 그대로 넘긴다.
internal fun McpToolArguments.headerTextList(): List<String> {
    headerListOrNull()

    return stringList(HEADERS)
}

internal fun AppActionWebWithHeaders.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", title)
        put("description", description)
        put("url", url)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put(
            HEADERS,
            headers.toJsonArray { header ->
                buildJsonObject {
                    put("name", header.name)
                    put("value", header.value)
                }
            },
        )
    }

internal fun AppActionWebDetail.toJson(): JsonObject =
    buildJsonObject {
        put("web", web.toJson())
        put("tags", tags.toJsonArray { tag -> tag.toJson() })
    }
