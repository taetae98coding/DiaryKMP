package io.github.taetae98coding.diary.work.mcp.tool.search

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class SearchMcpToolSetTest :
    FunSpec({
        test("TC-f61abd56-16fd-4232-8939-3191cee6d724 빈 검색어로 검색하면 SearchHome과 같은 결과를 받는다") {
            runTest {
                val searchMemoUseCase = mockk<SearchMemoUseCase>()
                every { searchMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
                val tool = toolSetOf<SearchMcpToolSet>(searchMemoUseCase).tool(name = "search_memo")

                tool.call(arguments = JsonObject(mapOf("query" to JsonPrimitive("")))) shouldBe
                    McpToolCallResult.Success(content = buildJsonObject { put("items", JsonArray(emptyList())) })
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = "", sort = ListSort.TITLE)) }
            }
        }
    })
