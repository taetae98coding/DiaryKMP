package io.github.taetae98coding.diary.work.mcp.tool.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class MemoListMcpToolTest :
    FunSpec({
        test("TC-9f84b0bf-4663-41e0-b563-897790439aee 정렬을 담지 않으면 처음 정렬로, 담으면 그 정렬로 조회한다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val tool = toolSetOf<MemoMcpToolSet>(pageMemoHomeUseCase).tool(name = "memo_list")

                tool.call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = memoList.toPageJson())
                tool.call(arguments = JsonObject(mapOf("sort" to JsonPrimitive("title")))) shouldBe McpToolCallResult.Success(content = memoList.toPageJson())

                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) }
                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.TITLE) }
            }
        }

        test("TC-9f84b0bf-4663-41e0-b563-897790439aee 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                val tool = toolSetOf<MemoMcpToolSet>(pageMemoHomeUseCase).tool(name = "memo_list")

                tool.call(arguments = JsonObject(mapOf("sort" to JsonPrimitive("name")))) shouldBe
                    McpToolCallResult.Failure(message = "'sort' must be one of default, title, recently_updated.")
                verify(exactly = 0) { pageMemoHomeUseCase(parameter = any()) }
            }
        }

        test("TC-63d5cbe2-0359-4a08-8108-4f545e469af2 계정이 확정되기 전 요청은 확정된 뒤의 목록을 받는다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns
                    flow {
                        delay(ACCOUNT_CONFIRM_DELAY_MILLIS)
                        emit(Result.success(PagingData.from(memoList)))
                    }
                val tool = toolSetOf<MemoMcpToolSet>(pageMemoHomeUseCase).tool(name = "memo_list")

                tool.call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = memoList.toPageJson())
            }
        }

        test("같은 메모 목록 도구로 다시 요청하면 그때의 목록을 받는다") {
            runTest {
                val beforeList = Arb.list(Arb.memo(), 1..3).next()
                val afterList = Arb.list(Arb.memo(), 1..3).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returnsMany
                    listOf(
                        flowOf(Result.success(PagingData.from(beforeList))),
                        flowOf(Result.success(PagingData.from(afterList))),
                    )
                val tool = toolSetOf<MemoMcpToolSet>(pageMemoHomeUseCase).tool(name = "memo_list")

                tool.call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = beforeList.toPageJson())
                tool.call(arguments = JsonObject(emptyMap())) shouldBe McpToolCallResult.Success(content = afterList.toPageJson())
            }
        }
    })

private const val ACCOUNT_CONFIRM_DELAY_MILLIS = 1_000L

private fun List<Memo>.toPageJson(): JsonObject = buildJsonObject { put("items", JsonArray(map { memo -> memo.toJson() })) }
