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
import org.koin.core.annotation.Factory
import kotlin.reflect.KClass

internal val mcpToolSetOrderList: List<KClass<out McpToolSet>> =
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

@Factory
internal class McpToolRegistry(
    toolSetList: List<McpToolSet>,
) {
    val toolList: List<McpTool> = toolSetList.sortedBy { toolSet -> toolSet.order() }.flatMap { toolSet -> toolSet.toolList }
}

private fun McpToolSet.order(): Int {
    val order = mcpToolSetOrderList.indexOf(this::class)
    check(order >= 0) { "${this::class.simpleName} is missing from mcpToolSetOrderList." }

    return order
}
