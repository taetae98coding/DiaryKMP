package io.github.taetae98coding.diary.work.mcp.tool.paging

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class McpPageTest :
    FunSpec({
        test("TC-2512e6a0-0601-47e5-a441-a95b9c00a65f 첫 페이지는 최대 100개와 남은 항목이 있을 때만 다음 위치를 준다") {
            runTest {
                listOf(
                    0 to (0 to null),
                    100 to (100 to null),
                    101 to (100 to "100"),
                ).forEach { (count, expected) ->
                    val (expectedSize, expectedCursor) = expected
                    val itemList = itemList(count = count)
                    val useCase = pagingUseCase(itemList = itemList, enablePlaceholders = true)

                    val page = useCase.readPage(parameter = Unit, offset = McpToolArguments(JsonObject(emptyMap())).pageOffset()).toJson { item -> JsonPrimitive(item) }

                    page.items() shouldBe itemList.take(expectedSize)
                    page.nextCursor() shouldBe expectedCursor
                }
            }
        }

        test("TC-5e76676b-ba84-45cd-9f2c-2d77bc71e555 다음 위치로 이어 받으면 250개를 순서대로 100개씩 받는다") {
            runTest {
                val itemList = itemList(count = 250)
                val useCase = pagingUseCase(itemList = itemList, enablePlaceholders = true)

                val pageList = readAllPages(useCase)

                pageList.map { page -> page.items().size } shouldBe listOf(100, 100, 50)
                pageList[1].items() shouldBe itemList.subList(100, 200)
                pageList[2].items() shouldBe itemList.subList(200, 250)
                pageList.map { page -> page.nextCursor() } shouldBe listOf("100", "200", null)
                pageList.flatMap { page -> page.items() } shouldBe itemList
            }
        }

        test("자리 표시 없이 이어 붙이는 목록도 같은 페이지로 나눈다") {
            runTest {
                listOf(0, 100, 101).forEach { count ->
                    val itemList = itemList(count = count)
                    val page = pagingUseCase(itemList = itemList, enablePlaceholders = false).readPage(parameter = Unit, offset = 0)

                    page.itemList shouldBe itemList.take(100)
                    page.nextOffset shouldBe 100.takeIf { count > 100 }
                }

                val itemList = itemList(count = 250)
                val pageList = readAllPages(pagingUseCase(itemList = itemList, enablePlaceholders = false))

                pageList.map { page -> page.nextCursor() } shouldBe listOf("100", "200", null)
                pageList.flatMap { page -> page.items() } shouldBe itemList
            }
        }

        test("목록 끝을 넘는 위치는 빈 페이지를 준다") {
            runTest {
                val itemList = itemList(count = Arb.int(0..50).next())
                val page = pagingUseCase(itemList = itemList, enablePlaceholders = true).readPage(parameter = Unit, offset = 100)

                page.itemList.shouldBeEmpty()
                page.nextOffset shouldBe null
            }
        }

        test("이전 페이지가 준 위치가 아니면 실패한다") {
            listOf("abc", "-1", "1.5", "").forEach { cursor ->
                shouldThrowExactly<McpToolArgumentException> {
                    McpToolArguments(JsonObject(mapOf("cursor" to JsonPrimitive(cursor)))).pageOffset()
                }.message shouldBe "'cursor' must be a nextCursor value from a previous page."
            }
        }

        test("불러오기 상태 없이 고정된 목록도 페이지로 나눈다") {
            runTest {
                val itemList = itemList(count = 3)

                PagingData.from(itemList).readPage(offset = 0, size = MCP_PAGE_SIZE) shouldBe McpPage(itemList = itemList, nextOffset = null)
                PagingData.empty<String>().readPage(offset = 0, size = MCP_PAGE_SIZE) shouldBe McpPage(itemList = emptyList(), nextOffset = null)
            }
        }

        test("처음 불러오기가 실패하면 그 실패를 그대로 알린다") {
            runTest {
                val failure = IllegalStateException(Arb.string().next())
                val pagingData =
                    PagingData.from(
                        data = emptyList<String>(),
                        sourceLoadStates = LoadStates(refresh = LoadState.Error(failure), prepend = LoadState.NotLoading(false), append = LoadState.NotLoading(false)),
                    )

                shouldThrowExactly<IllegalStateException> { pagingData.readPage(offset = 0, size = MCP_PAGE_SIZE) }.message shouldBe failure.message
            }
        }

        test("다음 페이지를 불러오다 실패하면 그 실패를 그대로 알린다") {
            runTest {
                val failure = IllegalStateException(Arb.string().next())
                val firstPage = itemList(count = 30)
                val pagingSource = mockk<PagingSource<Int, String>>(relaxed = true)
                coEvery { pagingSource.load(any()) } answers {
                    if (firstArg<PagingSource.LoadParams<Int>>() is PagingSource.LoadParams.Refresh) {
                        PagingSource.LoadResult.Page(data = firstPage, prevKey = null, nextKey = firstPage.size)
                    } else {
                        PagingSource.LoadResult.Error(failure)
                    }
                }
                val pagingData = Pager(config = PagingConfig(pageSize = 30, enablePlaceholders = false), pagingSourceFactory = { pagingSource }).flow.first()

                shouldThrowExactly<IllegalStateException> { pagingData.readPage(offset = 0, size = MCP_PAGE_SIZE) }.message shouldBe failure.message
            }
        }
    })

private fun itemList(count: Int): List<String> = List(count) { index -> "$index-${Arb.list(Arb.int(), 0..2).next()}" }

private fun pagingUseCase(
    itemList: List<String>,
    enablePlaceholders: Boolean,
): FlowUseCase<Unit, PagingData<String>> {
    val pagingSourceFactory = itemList.asPagingSourceFactory()

    return mockk<FlowUseCase<Unit, PagingData<String>>>().also { useCase ->
        every { useCase(parameter = Unit) } answers {
            Pager(
                config = PagingConfig(pageSize = 30, enablePlaceholders = enablePlaceholders),
                pagingSourceFactory = pagingSourceFactory,
            ).flow.map { pagingData -> Result.success(pagingData) }
        }
    }
}

private suspend fun readAllPages(useCase: FlowUseCase<Unit, PagingData<String>>): List<JsonObject> {
    val pageList = mutableListOf<JsonObject>()
    var arguments = McpToolArguments(JsonObject(emptyMap()))

    while (true) {
        val page = useCase.readPage(parameter = Unit, offset = arguments.pageOffset()).toJson { item -> JsonPrimitive(item) }
        pageList += page
        val nextCursor = page.nextCursor() ?: return pageList
        arguments = McpToolArguments(JsonObject(mapOf("cursor" to JsonPrimitive(nextCursor))))
    }
}

private fun JsonObject.items(): List<String> = (getValue("items") as kotlinx.serialization.json.JsonArray).map { item -> (item as JsonPrimitive).content }

private fun JsonObject.nextCursor(): String? = (get("nextCursor") as? JsonPrimitive)?.content
