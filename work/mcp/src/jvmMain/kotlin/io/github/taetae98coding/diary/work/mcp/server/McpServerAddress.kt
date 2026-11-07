package io.github.taetae98coding.diary.work.mcp.server

internal const val MCP_SERVER_HOST: String = "127.0.0.1"
internal const val MCP_SERVER_PATH: String = "/mcp"

internal fun mcpServerAddress(port: Int): String = "http://$MCP_SERVER_HOST:$port$MCP_SERVER_PATH"
