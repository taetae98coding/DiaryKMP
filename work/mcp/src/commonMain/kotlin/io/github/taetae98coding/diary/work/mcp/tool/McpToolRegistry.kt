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
import io.github.taetae98coding.diary.work.mcp.tool.search.SearchMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagItemMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.web.WebMcpToolSet
import org.koin.core.annotation.Factory

@Factory
internal class McpToolRegistry(
    memoMcpToolSet: MemoMcpToolSet,
    memoLinkMcpToolSet: MemoLinkMcpToolSet,
    tagMcpToolSet: TagMcpToolSet,
    tagLinkMcpToolSet: TagLinkMcpToolSet,
    tagItemMcpToolSet: TagItemMcpToolSet,
    placeMcpToolSet: PlaceMcpToolSet,
    webMcpToolSet: WebMcpToolSet,
    contactMcpToolSet: ContactMcpToolSet,
    checklistMcpToolSet: ChecklistMcpToolSet,
    qrMcpToolSet: QrMcpToolSet,
    musicMcpToolSet: MusicMcpToolSet,
    ddayMcpToolSet: DDayMcpToolSet,
    searchMcpToolSet: SearchMcpToolSet,
    calendarMcpToolSet: CalendarMcpToolSet,
    lunarMcpToolSet: LunarMcpToolSet,
) {
    val toolList: List<McpTool> =
        listOf(
            memoMcpToolSet,
            memoLinkMcpToolSet,
            tagMcpToolSet,
            tagLinkMcpToolSet,
            tagItemMcpToolSet,
            placeMcpToolSet,
            webMcpToolSet,
            contactMcpToolSet,
            checklistMcpToolSet,
            qrMcpToolSet,
            musicMcpToolSet,
            ddayMcpToolSet,
            searchMcpToolSet,
            calendarMcpToolSet,
            lunarMcpToolSet,
        ).flatMap { toolSet -> toolSet.toolList }
}
