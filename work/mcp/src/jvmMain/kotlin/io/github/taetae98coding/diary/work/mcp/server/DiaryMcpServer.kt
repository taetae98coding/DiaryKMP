package io.github.taetae98coding.diary.work.mcp.server

import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.ktor.server.application.Application
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private const val SERVER_NAME = "diary"
private const val SERVER_VERSION = "1.0.0"

private val resultJson = Json { explicitNulls = true }

// 브라우저는 다른 사이트에서 보낸 요청에도 Origin을 붙이므로, 허용 목록을 비워 Origin이 있는 요청을 모두 거절한다.
internal fun Application.installDiaryMcp(toolList: List<McpTool>) {
    mcpStatelessStreamableHttp(
        path = MCP_SERVER_PATH,
        allowedOrigins = emptyList(),
    ) {
        createDiaryMcpServer(toolList = toolList)
    }
}

internal fun createDiaryMcpServer(toolList: List<McpTool>): Server =
    Server(
        serverInfo = Implementation(name = SERVER_NAME, version = SERVER_VERSION),
        options = ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
    ) {
        toolList.forEach { tool ->
            addTool(
                name = tool.name,
                description = tool.description,
                inputSchema = ToolSchema(properties = tool.propertySchema, required = tool.requiredNameList),
                toolAnnotations =
                    ToolAnnotations(
                        readOnlyHint = tool.kind == McpToolKind.READ,
                        destructiveHint = tool.kind == McpToolKind.DESTRUCTIVE,
                        openWorldHint = false,
                    ),
            ) { request ->
                tool.call(arguments = request.arguments ?: JsonObject(emptyMap())).toCallToolResult()
            }
        }
    }

private fun McpToolCallResult.toCallToolResult(): CallToolResult =
    when (this) {
        is McpToolCallResult.Success -> CallToolResult(content = listOf(TextContent(text = resultJson.encodeToString(content))))
        is McpToolCallResult.Failure -> CallToolResult(content = listOf(TextContent(text = message)), isError = true)
    }
