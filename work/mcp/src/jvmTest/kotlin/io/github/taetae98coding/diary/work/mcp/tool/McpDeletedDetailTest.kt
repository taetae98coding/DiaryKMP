package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.work.mcp.tool.checklist.ChecklistMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.contact.ContactMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.dday.DDayMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.music.MusicMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.place.PlaceMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.qr.QrMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.StubbedFind
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundChecklist
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundContact
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundDDay
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundMemo
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundMusic
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundPlace
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundQr
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundWeb
import io.github.taetae98coding.diary.work.mcp.tool.web.WebMcpToolSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class McpDeletedDetailTest :
    FunSpec({
        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 항목의 상세를 요청하면 없는 항목과 같은 결과를 받는다") {
            runTest {
                detailCaseList().forEach { case ->
                    val found = case.stubFound()
                    val arguments = JsonObject(mapOf(case.idName to JsonPrimitive(found.id.toString())))

                    case.toolSet(found.useCaseList).tool(name = case.toolName).call(arguments = arguments) shouldBe McpToolCallResult.Success(content = JsonNull)
                }
            }
        }
    })

private class DetailCase(
    val toolName: String,
    val idName: String,
    val toolSet: (List<Any>) -> McpToolSet,
    val stubFound: () -> StubbedFind,
)

private fun detailCaseList(): List<DetailCase> =
    listOf(
        DetailCase(toolName = "memo_get", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundMemo(isDeleted = true) }),
        DetailCase(
            toolName = "tag_get",
            idName = "tagId",
            toolSet = { useCaseList -> toolSetOf<TagMcpToolSet>(*useCaseList.toTypedArray()) },
            stubFound = {
                val tag = Arb.tag().next().copy(isDeleted = true)
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                StubbedFind(id = tag.id, useCaseList = listOf(findTagUseCase))
            },
        ),
        DetailCase(toolName = "place_get", idName = "placeId", toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundPlace(isDeleted = true) }),
        DetailCase(toolName = "web_get", idName = "webId", toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundWeb(isDeleted = true) }),
        DetailCase(toolName = "contact_get", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundContact(isDeleted = true) }),
        DetailCase(toolName = "checklist_get", idName = "checklistId", toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundChecklist(isDeleted = true) }),
        DetailCase(toolName = "qr_get", idName = "qrId", toolSet = { useCaseList -> toolSetOf<QrMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundQr(isDeleted = true) }),
        DetailCase(toolName = "music_get", idName = "musicId", toolSet = { useCaseList -> toolSetOf<MusicMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundMusic(isDeleted = true) }),
        DetailCase(toolName = "dday_get", idName = "ddayId", toolSet = { useCaseList -> toolSetOf<DDayMcpToolSet>(*useCaseList.toTypedArray()) }, stubFound = { stubFoundDDay(isDeleted = true) }),
    )
