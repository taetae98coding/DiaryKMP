package io.github.taetae98coding.diary.core.model.mcp

public sealed interface McpServerState {
    public data object Off : McpServerState

    public data class Running(
        val address: String,
    ) : McpServerState

    public data class AddressInUse(
        val address: String,
    ) : McpServerState

    public data object StartFailed : McpServerState
}
