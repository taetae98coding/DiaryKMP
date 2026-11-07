package io.github.taetae98coding.diary.work.mcp.tool

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal sealed interface McpToolCallResult {
    data class Success(
        val content: JsonElement,
    ) : McpToolCallResult

    data class Failure(
        val message: String,
    ) : McpToolCallResult
}

internal suspend fun McpTool.call(arguments: JsonObject): McpToolCallResult =
    try {
        McpToolCallResult.Success(content = execute(McpToolArguments(arguments)))
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        McpToolCallResult.Failure(message = throwable.toFailureMessage())
    }

private fun Throwable.toFailureMessage(): String {
    val name = this::class.simpleName.orEmpty()
    val detail = message.orEmpty()

    return when {
        this is McpToolArgumentException -> detail
        detail.isBlank() -> name
        else -> "$name: $detail"
    }
}
