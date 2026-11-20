package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.core.toSortValue
import io.github.taetae98coding.diary.work.mcp.tool.json.colorOrNull
import io.github.taetae98coding.diary.work.mcp.tool.json.sort
import io.github.taetae98coding.diary.work.mcp.tool.paging.pageOffset

// 공통 동작은 인자를 문자열로 받아 스스로 읽는다. 여러 인자가 함께 잘못됐을 때 알리는 오류가 달라지지 않도록, 도구가 받은 순서대로 먼저 읽어 확인한 값을 같은 표기로 넘긴다.
internal fun McpToolArguments.idText(name: String): String = uuid(name).toString()

internal fun McpToolArguments.idTextOrNull(name: String): String? = uuidOrNull(name)?.toString()

internal fun McpToolArguments.idTextList(name: String): List<String> = uuidList(name).map { id -> id.toString() }

internal fun McpToolArguments.localDateTextOrNull(name: String): String? = localDateOrNull(name)?.toString()

internal fun McpToolArguments.sortText(sortList: List<ListSort>): String = sort(sortList).toSortValue()

internal fun McpToolArguments.cursorText(): String = pageOffset().toString()

internal fun McpToolArguments.colorTextOrNull(): String? = colorOrNull()?.toColorHex()
