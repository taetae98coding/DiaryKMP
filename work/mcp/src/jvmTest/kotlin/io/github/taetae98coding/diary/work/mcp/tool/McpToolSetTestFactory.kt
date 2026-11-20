package io.github.taetae98coding.diary.work.mcp.tool

import io.mockk.mockkClass
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

private const val APP_ACTION_PACKAGE = "io.github.taetae98coding.diary.work.appaction."

// 도구 묶음은 화면 기능을 여럿 주입받으므로, 테스트가 쓰지 않는 기능은 mock으로 채운다.
internal inline fun <reified T : McpToolSet> toolSetOf(vararg useCaseList: Any): T = T::class.createWith(useCaseList = useCaseList.toList())

// 도구가 공통 동작(XxxAppAction)을 거쳐 화면 기능을 부르므로, 공통 동작은 mock으로 채우지 않고 같은 기능 목록으로 실제로 만든다.
internal fun <T : Any> KClass<T>.createWith(useCaseList: List<Any>): T {
    val constructor = primaryConstructor!!
    val argumentList =
        constructor.parameters.map { parameter ->
            val type = parameter.type.classifier as KClass<*>

            useCaseList.firstOrNull { useCase -> type.isInstance(useCase) }
                ?: if (type.isAppAction) type.createWith(useCaseList = useCaseList) else mockkClass(type)
        }

    return constructor.call(*argumentList.toTypedArray())
}

private val KClass<*>.isAppAction: Boolean
    get() = qualifiedName.orEmpty().startsWith(APP_ACTION_PACKAGE) && simpleName.orEmpty().endsWith("AppAction")

internal fun McpToolSet.tool(name: String): McpTool = toolList.single { tool -> tool.name == name }
