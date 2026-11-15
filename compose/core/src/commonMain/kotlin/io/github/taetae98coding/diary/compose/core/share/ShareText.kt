package io.github.taetae98coding.diary.compose.core.share

public fun shareTextOf(vararg lines: String): String =
    lines
        .map(String::trim)
        .filter(String::isNotEmpty)
        .joinToString(separator = "\n")
