package io.github.taetae98coding.diary.work.mcp.manager

import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.work.mcp.server.mcpServerAddress
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolRegistry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.time.Duration.Companion.seconds

class JvmMcpServerManagerTest :
    FunSpec({
        test("TC-af4cbda8-1a65-4f20-bc57-160e76364072 연결 주소를 열 수 있으면 연결을 시작해 동작 중이 된다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val manager = createManager(port = port, toolList = emptyList())

                try {
                    manager.getState().first() shouldBe McpServerState.Off

                    manager.start()

                    manager.getState().first() shouldBe McpServerState.Running(address = mcpServerAddress(port = port))
                } finally {
                    manager.stop()
                }
            }
        }

        test("TC-ed294eaa-40c8-48a5-b9d8-e5b2c70ccbdc 동작 중인 연결을 끄면 꺼짐이 되고 새 요청을 받지 않는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val manager = createManager(port = port, toolList = emptyList())
                manager.start()

                manager.stop()

                manager.getState().first() shouldBe McpServerState.Off
                // 연결이 주소를 놓았으면 같은 주소를 다른 프로그램이 열 수 있다.
                ServerSocket(port, BACKLOG, LOOPBACK).close()
            }
        }

        test("TC-e168b09e-2a42-46e5-a369-7338871dfde5 다른 프로그램이 주소를 쓰고 있으면 주소를 다른 프로그램이 쓰는 시작 실패가 된다") {
            runTest(timeout = TIMEOUT) {
                ServerSocket(0, BACKLOG, LOOPBACK).use { otherProgram ->
                    val manager = createManager(port = otherProgram.localPort, toolList = emptyList())

                    try {
                        manager.start()

                        manager.getState().first() shouldBe McpServerState.AddressInUse(address = mcpServerAddress(port = otherProgram.localPort))
                    } finally {
                        manager.stop()
                    }
                }
            }
        }

        test("TC-e168b09e-2a42-46e5-a369-7338871dfde5 그 밖의 이유로 주소를 열지 못하면 그 밖의 이유의 시작 실패가 된다") {
            runTest(timeout = TIMEOUT) {
                listOf(-1, 70_000).forEach { port ->
                    val manager = createManager(port = port, toolList = emptyList())

                    try {
                        manager.start()

                        manager.getState().first() shouldBe McpServerState.StartFailed
                    } finally {
                        manager.stop()
                    }
                }
            }
        }

        test("TC-bf7dadaf-e25a-402f-a814-ad600def73e4 주소를 쓰던 프로그램이 주소를 놓아도 시작 실패로 남고 주소를 다시 열지 않는다") {
            runTest(timeout = TIMEOUT) {
                val otherProgram = ServerSocket(0, BACKLOG, LOOPBACK)
                val port = otherProgram.localPort
                val registry = toolRegistry(toolList = emptyList())
                val manager = JvmMcpServerManager(port = port, dispatcher = StandardTestDispatcher(testScheduler), toolRegistry = registry)

                try {
                    manager.start()
                    otherProgram.close()
                    withContext(Dispatchers.Default) { delay(RETRY_OBSERVATION) }

                    manager.getState().first() shouldBe McpServerState.AddressInUse(address = mcpServerAddress(port = port))
                    verify(exactly = 1) { registry.toolList }
                    // 연결이 주소를 다시 열지 않았으므로 다른 프로그램이 같은 주소를 열 수 있다.
                    ServerSocket(port, BACKLOG, LOOPBACK).close()
                } finally {
                    otherProgram.close()
                    manager.stop()
                }
            }
        }

        test("TC-ed294eaa-40c8-48a5-b9d8-e5b2c70ccbdc 시작에 실패한 연결을 끄면 꺼짐이 된다") {
            runTest(timeout = TIMEOUT) {
                ServerSocket(0, BACKLOG, LOOPBACK).use { otherProgram ->
                    val manager = createManager(port = otherProgram.localPort, toolList = emptyList())
                    manager.start()

                    manager.stop()

                    manager.getState().first() shouldBe McpServerState.Off
                }
            }
        }

        test("TC-b5f12a78-7ced-46c6-b06c-115f6379320f 브라우저를 거치지 않은 요청은 인증 없이 기능 목록을 받는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val toolList = listOf(readTool(name = toolName()), readTool(name = toolName()))
                val manager = createManager(port = port, toolList = toolList)

                try {
                    manager.start()
                    val response = HttpClient(CIO).use { client -> client.postMcp(port = port, body = TOOLS_LIST_REQUEST) }

                    response.status shouldBe HttpStatusCode.OK
                    response
                        .jsonBody()
                        .getValue("result")
                        .jsonObject
                        .getValue("tools")
                        .jsonArray
                        .map { tool ->
                            tool.jsonObject
                                .getValue("name")
                                .jsonPrimitive.content
                        } shouldContainExactlyInAnyOrder toolList.map { tool -> tool.name }
                } finally {
                    manager.stop()
                }
            }
        }

        test("TC-cc2c0f76-ead1-461a-a234-e97ca13f9c44 웹 페이지가 브라우저를 통해 보낸 요청은 거절하고 기능을 실행하지 않는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val name = toolName()
                val execute = mockk<suspend (McpToolArguments) -> JsonElement>()
                coEvery { execute(any()) } returns JsonPrimitive(Arb.string().next())
                val manager = createManager(port = port, toolList = listOf(McpTool(name = name, description = name, kind = McpToolKind.WRITE, execute = execute)))

                try {
                    manager.start()

                    listOf("https://example.com", "http://localhost:$port", "http://127.0.0.1:$port", "null").forEach { origin ->
                        HttpClient(CIO).use { client ->
                            client.postMcp(port = port, body = toolCallRequest(name = name), origin = origin).status shouldBe HttpStatusCode.Forbidden
                            client.postMcp(port = port, body = TOOLS_LIST_REQUEST, origin = origin).status shouldBe HttpStatusCode.Forbidden
                        }
                    }

                    coVerify(exactly = 0) { execute(any()) }
                    manager.getState().first().shouldBeInstanceOf<McpServerState.Running>()
                } finally {
                    manager.stop()
                }
            }
        }

        test("TC-78e9deb8-0990-4954-9903-4337415a5943 기능이 실패하면 실패한 결과를 받고 연결은 동작 중으로 남는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val name = toolName()
                val message = Arb.alphanumericString(prefix = "failure").next()
                val manager =
                    createManager(
                        port = port,
                        toolList = listOf(McpTool(name = name, description = name, kind = McpToolKind.WRITE) { throw IllegalStateException(message) }),
                    )

                try {
                    manager.start()
                    val response = HttpClient(CIO).use { client -> client.postMcp(port = port, body = toolCallRequest(name = name)) }

                    response.status shouldBe HttpStatusCode.OK
                    val result = response.jsonBody().getValue("result").jsonObject
                    result.getValue("isError").jsonPrimitive.boolean shouldBe true
                    result
                        .getValue("content")
                        .jsonArray
                        .single()
                        .jsonObject
                        .getValue("text")
                        .jsonPrimitive.content shouldBe "IllegalStateException: $message"
                    manager.getState().first() shouldBe McpServerState.Running(address = mcpServerAddress(port = port))
                } finally {
                    manager.stop()
                }
            }
        }

        test("TC-13865563-40d3-4f3d-a063-b0e955d7ef6f 동작 중에 계정이 바뀌면 다시 시작하지 않고 다음 요청부터 바뀐 계정의 결과를 받는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val name = toolName()
                val beforeAccount = Arb.alphanumericString(prefix = "before").next()
                val afterAccount = Arb.alphanumericString(prefix = "after").next()
                val currentAccount = MutableStateFlow(beforeAccount)
                val registry = toolRegistry(toolList = listOf(McpTool(name = name, description = name, kind = McpToolKind.READ) { JsonPrimitive(currentAccount.value) }))
                val manager = JvmMcpServerManager(port = port, dispatcher = StandardTestDispatcher(testScheduler), toolRegistry = registry)

                try {
                    manager.start()
                    HttpClient(CIO).use { client ->
                        client.postMcp(port = port, body = toolCallRequest(name = name)).toolResultText() shouldBe "\"$beforeAccount\""
                        currentAccount.value = afterAccount
                        client.postMcp(port = port, body = toolCallRequest(name = name)).toolResultText() shouldBe "\"$afterAccount\""
                    }

                    manager.getState().first() shouldBe McpServerState.Running(address = mcpServerAddress(port = port))
                    verify(exactly = 1) { registry.toolList }
                } finally {
                    manager.stop()
                }
            }
        }

        test("기능이 성공하면 돌려준 내용을 결과로 받는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val name = toolName()
                val value = Arb.alphanumericString(prefix = "value").next()
                val manager =
                    createManager(
                        port = port,
                        toolList =
                            listOf(
                                McpTool(name = name, description = name, kind = McpToolKind.READ) { arguments -> JsonObject(mapOf("echo" to JsonPrimitive(arguments.string("value")))) },
                            ),
                    )

                try {
                    manager.start()
                    val response = HttpClient(CIO).use { client -> client.postMcp(port = port, body = toolCallRequest(name = name, value = value)) }

                    val result = response.jsonBody().getValue("result").jsonObject
                    (result["isError"]?.jsonPrimitive?.boolean ?: false) shouldBe false
                    Json.parseToJsonElement(
                        result
                            .getValue("content")
                            .jsonArray
                            .single()
                            .jsonObject
                            .getValue("text")
                            .jsonPrimitive.content,
                    ) shouldBe
                        JsonObject(mapOf("echo" to JsonPrimitive(value)))
                } finally {
                    manager.stop()
                }
            }
        }

        test("이미 동작 중이면 다시 시작해도 주소를 새로 열지 않는다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val registry = toolRegistry(toolList = emptyList())
                val manager = JvmMcpServerManager(port = port, dispatcher = StandardTestDispatcher(testScheduler), toolRegistry = registry)

                try {
                    manager.start()
                    manager.start()

                    manager.getState().first() shouldBe McpServerState.Running(address = mcpServerAddress(port = port))
                    verify(exactly = 1) { registry.toolList }
                } finally {
                    manager.stop()
                }
            }
        }

        test("꺼진 연결을 다시 켜면 같은 주소로 다시 동작한다") {
            runTest(timeout = TIMEOUT) {
                val port = freePort()
                val manager = createManager(port = port, toolList = emptyList())

                try {
                    manager.start()
                    manager.stop()
                    manager.start()

                    manager.getState().first() shouldBe McpServerState.Running(address = mcpServerAddress(port = port))
                } finally {
                    manager.stop()
                }
            }
        }
    })

private val TIMEOUT = 30.seconds
private const val RETRY_OBSERVATION = 300L
private const val BACKLOG = 50
private val LOOPBACK: InetAddress = InetAddress.getByName("127.0.0.1")

private const val TOOLS_LIST_REQUEST = """{"jsonrpc":"2.0","id":1,"method":"tools/list"}"""

private fun toolCallRequest(
    name: String,
    value: String = "",
): String = """{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"$name","arguments":{"value":"$value"}}}"""

private fun freePort(): Int = ServerSocket(0, BACKLOG, LOOPBACK).use { socket -> socket.localPort }

private fun toolName(): String = Arb.alphanumericString(prefix = "tool_").next()

private fun readTool(name: String): McpTool = McpTool(name = name, description = name, kind = McpToolKind.READ) { JsonPrimitive(name) }

private fun toolRegistry(toolList: List<McpTool>): McpToolRegistry =
    mockk<McpToolRegistry>().also { registry ->
        every { registry.toolList } returns toolList
    }

private fun TestScope.createManager(
    port: Int,
    toolList: List<McpTool>,
): JvmMcpServerManager = JvmMcpServerManager(port = port, dispatcher = StandardTestDispatcher(testScheduler), toolRegistry = toolRegistry(toolList = toolList))

private suspend fun HttpClient.postMcp(
    port: Int,
    body: String,
    origin: String? = null,
): HttpResponse =
    post(mcpServerAddress(port = port)) {
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Accept, "application/json, text/event-stream")
        origin?.let { value -> header(HttpHeaders.Origin, value) }
        setBody(body)
    }

private suspend fun HttpResponse.jsonBody(): JsonObject = Json.parseToJsonElement(bodyAsText()).jsonObject

private suspend fun HttpResponse.toolResultText(): String =
    jsonBody()
        .getValue("result")
        .jsonObject
        .getValue("content")
        .jsonArray
        .single()
        .jsonObject
        .getValue("text")
        .jsonPrimitive.content
