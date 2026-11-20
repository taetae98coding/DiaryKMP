package io.github.taetae98coding.diary.work.mcp.tool

internal class McpToolArgumentException(
    override val message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
