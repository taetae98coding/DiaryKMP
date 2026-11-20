package io.github.taetae98coding.diary.work.mcp.tool.memo

import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundMemo
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundTag
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MemoMcpToolSetTest :
    FunSpec({
        test("TC-77c32c1d-a54a-480f-9654-066a1f73fa1f 삭제한 메모를 되돌리면 성공하고 되돌린 수를 받는다") {
            val memoId = Arb.kotlinUuid().next()
            val changedCount = Arb.int(0..1).next()
            val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
            coEvery { restoreMemoUseCase(parameter = any()) } returns Result.success(changedCount)
            val tool = createMemoToolSet(restoreMemoUseCase = restoreMemoUseCase).toolList.single { tool -> tool.name == "memo_restore" }

            val result = tool.call(arguments = JsonObject(mapOf("memoId" to JsonPrimitive(memoId.toString()))))

            result shouldBe McpToolCallResult.Success(content = buildJsonObject { put("changedCount", changedCount) })
            coVerify(exactly = 1) { restoreMemoUseCase(parameter = memoId) }
        }

        test("TC-78e9deb8-0990-4954-9903-4337415a5943 되돌리는 앱 기능이 실패하면 실패를 알린다") {
            val message = Arb.string().next()
            val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
            coEvery { restoreMemoUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
            val tool = createMemoToolSet(restoreMemoUseCase = restoreMemoUseCase).toolList.single { tool -> tool.name == "memo_restore" }

            tool.call(arguments = JsonObject(mapOf("memoId" to JsonPrimitive(Arb.kotlinUuid().next().toString())))).shouldBeInstanceOf<McpToolCallResult.Failure>()
        }

        test("되돌릴 메모를 지정하지 않으면 앱 기능을 실행하지 않고 실패를 알린다") {
            val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
            val tool = createMemoToolSet(restoreMemoUseCase = restoreMemoUseCase).toolList.single { tool -> tool.name == "memo_restore" }

            tool.call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Failure(message = "'memoId' is required.")
            coVerify(exactly = 0) { restoreMemoUseCase(parameter = any()) }
        }

        test("TC-451290c7-912e-4b4d-bf15-e61caacbe1ca 대표 태그 지정과 해제는 받은 메모와 태그로 앱 기능을 실행한다") {
            runTest {
                val memo = stubFoundMemo(isDeleted = false)
                val tag = stubFoundTag(isDeleted = false)
                val setMemoPrimaryTagUseCase = mockk<SetMemoPrimaryTagUseCase>()
                val unsetMemoPrimaryTagUseCase = mockk<UnsetMemoPrimaryTagUseCase>()
                coEvery { setMemoPrimaryTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { unsetMemoPrimaryTagUseCase(parameter = any()) } returns Result.success(Unit)
                val toolSet = toolSetOf<MemoLinkMcpToolSet>(setMemoPrimaryTagUseCase, unsetMemoPrimaryTagUseCase, *memo.useCaseList.toTypedArray(), *tag.useCaseList.toTypedArray())
                val done = McpToolCallResult.Success(content = buildJsonObject { put("done", true) })

                toolSet.tool(name = "memo_primary_tag_set").call(arguments = JsonObject(mapOf("memoId" to JsonPrimitive(memo.id.toString()), "tagId" to JsonPrimitive(tag.id.toString())))) shouldBe done
                toolSet.tool(name = "memo_primary_tag_unset").call(arguments = JsonObject(mapOf("memoId" to JsonPrimitive(memo.id.toString())))) shouldBe done

                coVerify(exactly = 1) { setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memo.id, tagId = tag.id)) }
                coVerify(exactly = 1) { unsetMemoPrimaryTagUseCase(parameter = memo.id) }
            }
        }
    })

// 되돌리기 외의 앱 기능은 이 테스트에서 쓰지 않으므로 mock으로 채운다.
private fun createMemoToolSet(restoreMemoUseCase: RestoreMemoUseCase): MemoMcpToolSet = toolSetOf<MemoMcpToolSet>(restoreMemoUseCase)
