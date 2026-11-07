package io.github.taetae98coding.diary.work.mcp.tool

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainDuplicates
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

            listOf("memo", "tag", "place", "web", "contact", "checklist", "qr", "music").forEach { entity ->
                nameList shouldContainAll listOf("list", "get", "add", "update", "delete", "restore").map { action -> "${entity}_$action" }
            }
        }

        test("TC-c3ac3b36-0621-4123-838a-1cb57a17ccf8 검색, 캘린더, 공휴일, 음력 조회 기능이 있다") {
            val nameList = createRegistry().toolList.map { tool -> tool.name }

            nameList shouldContainAll
                listOf(
                    "search_memo",
                    "search_tag",
                    "search_place",
                    "search_web",
                    "calendar_memo_list",
                    "calendar_birthday_list",
                    "holiday_list",
                    "lunar_list",
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

private val toolSetClassList: List<KClass<out McpToolSet>>
    get() =
        McpToolRegistry::class.primaryConstructor!!.parameters.map { parameter ->
            @Suppress("UNCHECKED_CAST")
            parameter.type.classifier as KClass<out McpToolSet>
        }

// 각 기능 묶음이 받는 앱 기능은 목록을 만드는 데 쓰이지 않으므로 모두 mock으로 채운다.
private fun createRegistry(): McpToolRegistry {
    val toolSetList = toolSetClassList.map { toolSetClass -> toolSetClass.createWithMocks() }

    return McpToolRegistry::class.primaryConstructor!!.call(*toolSetList.toTypedArray())
}

private fun <T : Any> KClass<T>.createWithMocks(): T {
    val constructor = primaryConstructor!!
    val argumentList = constructor.parameters.map { parameter -> mockkClass(parameter.type.classifier as KClass<*>) }

    return constructor.call(*argumentList.toTypedArray())
}
