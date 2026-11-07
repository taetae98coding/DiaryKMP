package io.github.taetae98coding.diary.work.mcp.manager

import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.manager.McpServerManager
import io.github.taetae98coding.diary.work.mcp.di.McpServerDispatcher
import io.github.taetae98coding.diary.work.mcp.di.McpServerPort
import io.github.taetae98coding.diary.work.mcp.server.MCP_SERVER_HOST
import io.github.taetae98coding.diary.work.mcp.server.installDiaryMcp
import io.github.taetae98coding.diary.work.mcp.server.mcpServerAddress
import io.github.taetae98coding.diary.work.mcp.tool.McpToolRegistry
import io.ktor.server.application.serverConfig
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.net.BindException

private const val STOP_GRACE_PERIOD_MILLIS = 0L
private const val STOP_TIMEOUT_MILLIS = 1_000L

@Single(binds = [McpServerManager::class])
internal class JvmMcpServerManager(
    @McpServerPort
    private val port: Int,
    @McpServerDispatcher
    private val dispatcher: CoroutineDispatcher,
    private val toolRegistry: McpToolRegistry,
) : McpServerManager {
    private val state = MutableStateFlow<McpServerState>(McpServerState.Off)
    private val mutex = Mutex()
    private var server: EmbeddedServer<*, *>? = null

    override fun isSupported(): Boolean = true

    override fun getState(): Flow<McpServerState> = state

    override suspend fun start() {
        mutex.withLock {
            if (server != null) return

            val address = mcpServerAddress(port = port)
            val toolList = toolRegistry.toolList
            val newServer =
                embeddedServer(
                    factory = CIO,
                    rootConfig =
                        serverConfig {
                            // 주소를 열지 못한 실패는 시작 결과로 상태에 남기므로, 서버 작업의 같은 실패를 처리되지 않은 예외로 다시 알리지 않는다.
                            parentCoroutineContext = CoroutineExceptionHandler { _, _ -> }
                            module { installDiaryMcp(toolList = toolList) }
                        },
                ) {
                    connector {
                        host = MCP_SERVER_HOST
                        port = this@JvmMcpServerManager.port
                    }
                }

            // 포트를 여는 동안 호출한 스레드가 막히므로 메인 스레드 밖에서 연다.
            state.value =
                withContext(dispatcher) {
                    try {
                        newServer.startSuspend(wait = false)
                        server = newServer
                        McpServerState.Running(address = address)
                    } catch (throwable: Throwable) {
                        newServer.stopQuietly()
                        // 서버는 주소를 열지 못하면 그 원인을 담은 취소 예외로 알리므로, 이 호출이 취소된 경우에만 취소로 전한다.
                        ensureActive()
                        if (throwable.isBindFailure()) McpServerState.AddressInUse(address = address) else McpServerState.StartFailed
                    }
                }
        }
    }

    override suspend fun stop() {
        mutex.withLock {
            val runningServer = server
            server = null
            state.value = McpServerState.Off

            // 연결을 끊고 포트를 닫을 때까지 호출한 스레드가 막히므로 메인 스레드 밖에서 닫는다.
            runningServer?.let { embeddedServer -> withContext(dispatcher) { embeddedServer.stopQuietly() } }
        }
    }

    private fun EmbeddedServer<*, *>.stopQuietly() {
        try {
            stop(gracePeriodMillis = STOP_GRACE_PERIOD_MILLIS, timeoutMillis = STOP_TIMEOUT_MILLIS)
        } catch (_: Exception) {
            // 시작하지 못한 서버는 닫을 자원이 없어 닫다가 실패할 수 있다.
        }
    }

    private fun Throwable.isBindFailure(): Boolean = generateSequence(this) { throwable -> throwable.cause }.any { throwable -> throwable is BindException }
}
