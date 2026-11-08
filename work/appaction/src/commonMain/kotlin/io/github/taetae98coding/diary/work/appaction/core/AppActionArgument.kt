package io.github.taetae98coding.diary.work.appaction.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

internal fun invalidArgument(message: String): Nothing = throw AppActionException.InvalidArgument(message = message)

internal fun missingArgument(name: String): Nothing = invalidArgument(message = "'$name' is required.")

internal fun String.toUuidArgument(name: String): Uuid = parseArgument(name = name, expected = "an id") { Uuid.parse(this) }

internal fun String?.toUuidArgumentOrNull(name: String): Uuid? = this?.toUuidArgument(name = name)

internal fun List<String>.toUuidSetArgument(name: String): Set<Uuid> = map { value -> value.toUuidArgument(name = name) }.toSet()

internal fun List<String>.toUuidListArgument(name: String): List<Uuid> = map { value -> value.toUuidArgument(name = name) }

internal fun String.toLocalDateArgument(name: String): LocalDate = parseArgument(name = name, expected = "a date like 2026-01-31") { LocalDate.parse(this) }

internal fun String?.toLocalDateArgumentOrNull(name: String): LocalDate? = this?.toLocalDateArgument(name = name)

internal fun String?.toLocalDateTimeArgumentOrNull(name: String): LocalDateTime? = this?.let { value -> parseArgument(name = name, expected = "a date time like 2026-01-31T09:30") { LocalDateTime.parse(value) } }

private inline fun <T> parseArgument(
    name: String,
    expected: String,
    block: () -> T,
): T =
    try {
        block()
    } catch (_: IllegalArgumentException) {
        invalidArgument(message = "'$name' must be $expected.")
    }
