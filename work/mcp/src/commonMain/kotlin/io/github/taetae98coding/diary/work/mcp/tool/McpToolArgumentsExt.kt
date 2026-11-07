package io.github.taetae98coding.diary.work.mcp.tool

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

internal fun McpToolArguments.uuidOrNull(name: String): Uuid? = stringOrNull(name)?.let { value -> parseUuid(name = name, value = value) }

internal fun McpToolArguments.uuid(name: String): Uuid = uuidOrNull(name) ?: missing(name)

internal fun McpToolArguments.uuidSet(name: String): Set<Uuid> = stringList(name).map { value -> parseUuid(name = name, value = value) }.toSet()

internal fun McpToolArguments.uuidList(name: String): List<Uuid> = stringList(name).map { value -> parseUuid(name = name, value = value) }

internal fun McpToolArguments.localDateOrNull(name: String): LocalDate? = stringOrNull(name)?.let { value -> parse(name = name, expected = "a date like 2026-01-31") { LocalDate.parse(value) } }

internal fun McpToolArguments.localDate(name: String): LocalDate = localDateOrNull(name) ?: missing(name)

internal fun McpToolArguments.localDateTimeOrNull(name: String): LocalDateTime? = stringOrNull(name)?.let { value -> parse(name = name, expected = "a date time like 2026-01-31T09:30") { LocalDateTime.parse(value) } }

private fun McpToolArguments.parseUuid(
    name: String,
    value: String,
): Uuid = parse(name = name, expected = "an id") { Uuid.parse(value) }

private inline fun <T> McpToolArguments.parse(
    name: String,
    expected: String,
    block: () -> T,
): T =
    try {
        block()
    } catch (_: IllegalArgumentException) {
        invalid(name, expected)
    }
