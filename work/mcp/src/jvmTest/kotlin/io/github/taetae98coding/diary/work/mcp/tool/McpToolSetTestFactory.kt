package io.github.taetae98coding.diary.work.mcp.tool

import io.mockk.mockkClass
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

// 도구 묶음은 화면 기능을 여럿 주입받으므로, 테스트가 쓰지 않는 기능은 mock으로 채운다.
internal inline fun <reified T : McpToolSet> toolSetOf(vararg useCaseList: Any): T {
    val constructor = T::class.primaryConstructor!!
    val argumentList =
        constructor.parameters.map { parameter ->
            val type = parameter.type.classifier as KClass<*>
            useCaseList.firstOrNull { useCase -> type.isInstance(useCase) } ?: mockkClass(type)
        }

    return constructor.call(*argumentList.toTypedArray())
}

internal fun McpToolSet.tool(name: String): McpTool = toolList.single { tool -> tool.name == name }
