package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.work.mcp.tool.calendar.CalendarMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.calendar.LunarMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.checklist.ChecklistMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.contact.ContactMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.dday.DDayMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.music.MusicMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.place.PlaceMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.qr.QrMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.routine.RoutineMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.search.SearchMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagItemMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.web.WebMcpToolSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainDuplicates
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.mockkClass
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class McpToolRegistryTest :
    FunSpec({
        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 기능 이름은 겹치지 않고 영문, 숫자, 밑줄만 쓴다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            nameList.shouldNotContainDuplicates()
            nameList.forEach { name -> name shouldMatch Regex("[A-Za-z0-9_]+") }
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 대상마다 조회 기능과 추가, 수정, 삭제, 되돌리기 기능이 있다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            listOf("memo", "tag", "place", "web", "contact", "checklist", "qr", "music", "dday", "routine").forEach { entity ->
                nameList shouldContainAll listOf("list", "get", "add", "update", "delete", "restore").map { action -> "${entity}_$action" }
            }
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 태그를 연결하는 대상마다 태그 연결과 해제 기능이 있다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            listOf("memo", "place", "web", "contact", "checklist", "qr", "music", "dday", "routine").forEach { entity ->
                nameList shouldContainAll listOf("${entity}_tag_add", "${entity}_tag_remove")
            }
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 태그 상세의 탭마다 태그에 연결된 항목 목록 기능이 있다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            nameList shouldContainAll listOf("memo", "web", "place", "contact", "checklist", "music", "qr", "dday", "routine").map { item -> "tag_${item}_list" } + listOf("tag_memo_finished_list", "tag_routine_finished_list")
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 검색, 캘린더, 공휴일, 음력 조회 기능이 있다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            nameList shouldContainAll
                listOf(
                    "search_memo",
                    "search_tag",
                    "search_place",
                    "search_web",
                    "search_dday",
                    "search_routine",
                    "calendar_memo_list",
                    "calendar_birthday_list",
                    "calendar_dday_list",
                    "holiday_list",
                    "lunar_list",
                )
        }

        test("기능 묶음이 어떤 순서로 주입되어도 정한 묶음 순서대로 기능을 알린다") {
            val expected = mcpToolSetOrderList.flatMap { toolSetClass -> toolSetClass.createWithMocks().toolList.map { tool -> tool.name } }

            createRegistry().toolList.map { tool -> tool.name } shouldBe expected
        }

        test("기능 묶음 순서 목록은 메모부터 음력까지 정한 순서다") {
            mcpToolSetOrderList shouldBe
                listOf(
                    MemoMcpToolSet::class,
                    MemoLinkMcpToolSet::class,
                    TagMcpToolSet::class,
                    TagLinkMcpToolSet::class,
                    TagItemMcpToolSet::class,
                    PlaceMcpToolSet::class,
                    WebMcpToolSet::class,
                    ContactMcpToolSet::class,
                    ChecklistMcpToolSet::class,
                    QrMcpToolSet::class,
                    MusicMcpToolSet::class,
                    DDayMcpToolSet::class,
                    RoutineMcpToolSet::class,
                    SearchMcpToolSet::class,
                    CalendarMcpToolSet::class,
                    LunarMcpToolSet::class,
                )
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 제공하지 않는 기능은 목록에 없다") {
            val excludedWordList =
                listOf(
                    "filter",
                    "setting",
                    "map",
                    "gemini",
                    "country",
                    "visibility",
                    "login",
                    "logout",
                    "account",
                    "withdraw",
                    "profile",
                    "file",
                    "download",
                    "upload",
                    "fetch",
                    "weather",
                    "scan",
                    "location",
                    "open",
                    "launch",
                    "mcp",
                )

            createRegistry()
                .toolList
                .map { tool -> tool.name }
                .filter { name -> excludedWordList.any { word -> word in name.split('_') } }
                .shouldBeEmpty()
        }
    })

// 각 기능 묶음이 받는 앱 기능은 목록을 만드는 데 쓰이지 않으므로 모두 mock으로 채운다.
private fun createRegistry(): McpToolRegistry = McpToolRegistry(toolSetList = mcpToolSetOrderList.map { toolSetClass -> toolSetClass.createWithMocks() }.shuffled())

private fun <T : Any> KClass<T>.createWithMocks(): T {
    val constructor = primaryConstructor!!
    val argumentList = constructor.parameters.map { parameter -> mockkClass(parameter.type.classifier as KClass<*>) }

    return constructor.call(*argumentList.toTypedArray())
}
