package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.mcp.tool.checklist.ChecklistMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.contact.ContactMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.dday.DDayMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.music.MusicMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.place.PlaceMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.qr.QrMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.StubbedFind
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagItemMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.TagMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundChecklist
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundContact
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundDDay
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundMemo
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundMusic
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundPlace
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundQr
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundTag
import io.github.taetae98coding.diary.work.mcp.tool.tag.stubFoundWeb
import io.github.taetae98coding.diary.work.mcp.tool.web.WebMcpToolSet
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.Uuid

class McpDeletedTargetTest :
    FunSpec({
        test("TC-62354a1e-7c25-4d54-81d6-c705399ad890 삭제된 항목을 바꾸는 요청은 없는 항목과 같은 결과를 받는다") {
            runTest {
                changeCaseList().forEach { case ->
                    withClue(case.toolName) {
                        val deletedResult = case.call(isDeleted = true)

                        deletedResult shouldBe case.call(isDeleted = false)
                        deletedResult shouldBe case.expected
                    }
                }
            }
        }

        test("TC-a422baa4-340c-4bb5-9453-15232dc401dc 없거나 삭제된 항목의 연결과 해제는 바뀐 것이 없다는 결과를 받는다") {
            runTest {
                linkCaseList().forEach { case ->
                    withClue(case.toolName) {
                        val deletedResult = case.call(isDeleted = true)

                        deletedResult shouldBe case.call(isDeleted = false)
                        deletedResult shouldBe case.expected
                    }
                }
            }
        }

        test("TC-937bb971-5a62-49cc-abb7-f1b6f76661df 없거나 삭제된 체크리스트의 빈 항목 추가는 추가한 수 0을 받는다") {
            runTest {
                val case =
                    TargetCase(
                        toolName = "checklist_item_add",
                        idName = "checklistId",
                        toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) },
                        target = ::checklistTarget,
                        extraArguments = { mapOf("content" to JsonPrimitive("")) },
                    )

                case.call(isDeleted = true) shouldBe unchanged
                case.call(isDeleted = false) shouldBe unchanged
            }
        }

        test("TC-aa3b2187-2072-4bf3-b79d-5c1cd17e889d 삭제된 항목에 딸린 목록을 요청하면 없는 항목과 같은 결과를 받는다") {
            runTest {
                listCaseList().forEach { case ->
                    withClue(case.toolName) {
                        val deletedResult = case.call(isDeleted = true)

                        deletedResult shouldBe case.call(isDeleted = false)
                        deletedResult shouldBe McpToolCallResult.Success(content = buildJsonObject { put("items", JsonArray(emptyList())) })
                    }
                }
            }
        }
    })

private val unchanged: McpToolCallResult = McpToolCallResult.Success(content = buildJsonObject { put("changedCount", 0) })

private val notDone: McpToolCallResult = McpToolCallResult.Success(content = buildJsonObject { put("done", false) })

private class TargetCase(
    val toolName: String,
    val idName: String,
    val toolSet: (List<Any>) -> McpToolSet,
    val target: (isDeleted: Boolean) -> Target,
    val expected: McpToolCallResult = unchanged,
    val extraArguments: () -> Map<String, JsonElement> = { emptyMap() },
    val saved: (Target) -> Saved = { Saved(arguments = emptyMap(), useCaseList = emptyList()) },
) {
    suspend fun call(isDeleted: Boolean): McpToolCallResult {
        val target = target(isDeleted)
        val saved = saved(target)
        val arguments = JsonObject(mapOf(idName to JsonPrimitive(target.id.toString())) + extraArguments() + saved.arguments)

        return toolSet(listOf(target.findUseCase) + saved.useCaseList).tool(name = toolName).call(arguments = arguments)
    }
}

// 요청이 대상과 함께 지정하는, 삭제되지 않은 채 저장된 항목이다.
private class Saved(
    val arguments: Map<String, JsonElement>,
    val useCaseList: List<Any>,
)

private fun StubbedFind.toSaved(idName: String): Saved = Saved(arguments = mapOf(idName to JsonPrimitive(id.toString())), useCaseList = useCaseList)

private class Target(
    val id: Uuid,
    val findUseCase: Any,
)

// 조회 기능만 답하게 하고 나머지는 답이 없는 mock으로 두어, 삭제된 항목에 바꾸기나 목록 기능이 실행되면 요청이 실패하게 한다.
// 삭제된 항목이 아니면 저장되어 있지 않은 항목으로 답한다.
private inline fun <reified T : FlowUseCase<Uuid, V?>, V : Any> target(
    isDeleted: Boolean,
    id: Uuid,
    deleted: V,
): Target {
    val findUseCase = mockk<T>()
    every { findUseCase(parameter = id) } returns flowOf(Result.success(deleted.takeIf { isDeleted }))

    return Target(id = id, findUseCase = findUseCase)
}

private fun memoTarget(isDeleted: Boolean): Target =
    Arb
        .memo()
        .next()
        .copy(isDeleted = true)
        .let { memo -> target<FindMemoUseCase, _>(isDeleted = isDeleted, id = memo.id, deleted = memo) }

private fun tagTarget(isDeleted: Boolean): Target =
    Arb
        .tag()
        .next()
        .copy(isDeleted = true)
        .let { tag -> target<FindTagUseCase, _>(isDeleted = isDeleted, id = tag.id, deleted = tag) }

private fun placeTarget(isDeleted: Boolean): Target =
    Arb
        .place()
        .next()
        .copy(isDeleted = true)
        .let { place -> target<FindPlaceUseCase, _>(isDeleted = isDeleted, id = place.id, deleted = place) }

private fun webTarget(isDeleted: Boolean): Target =
    Arb
        .web()
        .next()
        .copy(isDeleted = true)
        .let { web -> target<FindWebUseCase, _>(isDeleted = isDeleted, id = web.id, deleted = web) }

private fun contactTarget(isDeleted: Boolean): Target =
    Arb
        .contact()
        .next()
        .copy(isDeleted = true)
        .let { contact -> target<FindContactUseCase, _>(isDeleted = isDeleted, id = contact.id, deleted = contact) }

private fun checklistTarget(isDeleted: Boolean): Target =
    Arb
        .checklist()
        .next()
        .copy(isDeleted = true)
        .let { checklist -> target<FindChecklistUseCase, _>(isDeleted = isDeleted, id = checklist.id, deleted = checklist) }

private fun qrTarget(isDeleted: Boolean): Target =
    Arb
        .qr()
        .next()
        .copy(isDeleted = true)
        .let { qr -> target<FindQrUseCase, _>(isDeleted = isDeleted, id = qr.id, deleted = qr) }

private fun musicTarget(isDeleted: Boolean): Target =
    Arb
        .music()
        .next()
        .copy(isDeleted = true)
        .let { music -> target<FindMusicUseCase, _>(isDeleted = isDeleted, id = music.id, deleted = music) }

private fun ddayTarget(isDeleted: Boolean): Target =
    Arb
        .dday()
        .next()
        .copy(isDeleted = true)
        .let { dday -> target<FindDDayUseCase, _>(isDeleted = isDeleted, id = dday.id, deleted = dday) }

private fun changeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(toolName = "memo_update", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::memoTarget),
        TargetCase(toolName = "memo_copy", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::memoTarget, expected = McpToolCallResult.Failure(message = "IllegalArgumentException: Required value was null.")),
        TargetCase(toolName = "memo_finish", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::memoTarget),
        TargetCase(toolName = "memo_restart", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::memoTarget),
        TargetCase(toolName = "memo_delete", idName = "memoId", toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::memoTarget),
        TargetCase(toolName = "tag_update", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_finish", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_restart", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_delete", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "place_update", idName = "placeId", toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::placeTarget),
        TargetCase(toolName = "place_delete", idName = "placeId", toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::placeTarget),
        TargetCase(toolName = "web_update", idName = "webId", toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::webTarget),
        TargetCase(toolName = "web_delete", idName = "webId", toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::webTarget),
        TargetCase(toolName = "contact_update", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
        TargetCase(toolName = "contact_favorite", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
        TargetCase(toolName = "contact_unfavorite", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
        TargetCase(toolName = "contact_delete", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
        TargetCase(toolName = "checklist_update", idName = "checklistId", toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::checklistTarget),
        TargetCase(toolName = "checklist_item_add", idName = "checklistId", toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::checklistTarget, extraArguments = { mapOf("content" to JsonPrimitive(Arb.nonBlankString().next())) }),
        checklistItemCase(toolName = "checklist_item_check"),
        checklistItemCase(toolName = "checklist_item_uncheck"),
        checklistItemCase(toolName = "checklist_item_delete"),
        deletedChecklistItemCase(toolName = "checklist_item_check"),
        deletedChecklistItemCase(toolName = "checklist_item_uncheck"),
        deletedChecklistItemCase(toolName = "checklist_item_delete"),
        TargetCase(toolName = "checklist_reset", idName = "checklistId", toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::checklistTarget),
        TargetCase(toolName = "checklist_delete", idName = "checklistId", toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::checklistTarget),
        TargetCase(toolName = "qr_update", idName = "qrId", toolSet = { useCaseList -> toolSetOf<QrMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::qrTarget),
        TargetCase(toolName = "qr_delete", idName = "qrId", toolSet = { useCaseList -> toolSetOf<QrMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::qrTarget),
        TargetCase(toolName = "music_update", idName = "musicId", toolSet = { useCaseList -> toolSetOf<MusicMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::musicTarget),
        TargetCase(toolName = "music_delete", idName = "musicId", toolSet = { useCaseList -> toolSetOf<MusicMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::musicTarget),
        TargetCase(toolName = "dday_update", idName = "ddayId", toolSet = { useCaseList -> toolSetOf<DDayMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::ddayTarget),
        TargetCase(toolName = "dday_delete", idName = "ddayId", toolSet = { useCaseList -> toolSetOf<DDayMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::ddayTarget),
    )

// 항목은 삭제되지 않은 채 저장되어 있고, 그 항목이 속한 체크리스트만 삭제되었거나 없다.
private fun checklistItemCase(toolName: String): TargetCase =
    TargetCase(
        toolName = toolName,
        idName = "checklistId",
        toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) },
        target = ::checklistTarget,
        saved = { checklist ->
            val item = Arb.checklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id).next()
            val findChecklistItemUseCase = mockk<FindChecklistItemUseCase>()
            every { findChecklistItemUseCase(parameter = item.id) } returns flowOf(Result.success(item))

            Saved(arguments = mapOf("itemId" to JsonPrimitive(item.id.toString())), useCaseList = listOf(findChecklistItemUseCase))
        },
    )

// 체크리스트는 삭제되지 않은 채 저장되어 있고, 항목만 삭제되었거나 없다.
private fun deletedChecklistItemCase(toolName: String): TargetCase =
    TargetCase(
        toolName = toolName,
        idName = "itemId",
        toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) },
        target = { isDeleted ->
            val item = Arb.checklistItem(isChecked = false, isDeleted = true).next()

            target<FindChecklistItemUseCase, _>(isDeleted = isDeleted, id = item.id, deleted = item)
        },
        saved = { stubFoundChecklistOfAnyId() },
    )

private fun stubFoundChecklistOfAnyId(): Saved {
    val findChecklistUseCase = mockk<FindChecklistUseCase>()
    every { findChecklistUseCase(parameter = any()) } answers { flowOf(Result.success(Arb.checklist().next().copy(isDeleted = false))) }

    return Saved(arguments = emptyMap(), useCaseList = listOf(findChecklistUseCase))
}

private fun linkCaseList(): List<TargetCase> = memoLinkCaseList() + entityTagLinkCaseList() + tagLinkCaseList()

// 지정한 두 항목 가운데 한쪽만 삭제되었거나 없고, 다른 쪽은 삭제되지 않은 채 저장되어 있다.
private fun memoLinkCaseList(): List<TargetCase> =
    listOf("memo_tag_add", "memo_tag_remove", "memo_primary_tag_set").flatMap { toolName -> memoLinkCase(toolName = toolName, idName = "tagId", target = ::tagTarget) { stubFoundTag(isDeleted = false) } } +
        listOf("memo_place_add", "memo_place_remove").flatMap { toolName -> memoLinkCase(toolName = toolName, idName = "placeId", target = ::placeTarget) { stubFoundPlace(isDeleted = false) } } +
        listOf("memo_web_add", "memo_web_remove").flatMap { toolName -> memoLinkCase(toolName = toolName, idName = "webId", target = ::webTarget) { stubFoundWeb(isDeleted = false) } } +
        listOf("memo_contact_add", "memo_contact_remove").flatMap { toolName -> memoLinkCase(toolName = toolName, idName = "contactId", target = ::contactTarget) { stubFoundContact(isDeleted = false) } } +
        linkCase<MemoLinkMcpToolSet>(toolName = "memo_primary_tag_unset", idName = "memoId", target = ::memoTarget)

private fun memoLinkCase(
    toolName: String,
    idName: String,
    target: (isDeleted: Boolean) -> Target,
    savedTarget: () -> StubbedFind,
): List<TargetCase> =
    listOf(
        linkCase<MemoLinkMcpToolSet>(toolName = toolName, idName = "memoId", target = ::memoTarget) { savedTarget().toSaved(idName = idName) },
        linkCase<MemoLinkMcpToolSet>(toolName = toolName, idName = idName, target = target) { stubFoundMemo(isDeleted = false).toSaved(idName = "memoId") },
    )

private fun entityTagLinkCaseList(): List<TargetCase> =
    entityTagLinkCase<PlaceMcpToolSet>(entity = "place", idName = "placeId", target = ::placeTarget) { stubFoundPlace(isDeleted = false) } +
        entityTagLinkCase<WebMcpToolSet>(entity = "web", idName = "webId", target = ::webTarget) { stubFoundWeb(isDeleted = false) } +
        entityTagLinkCase<ContactMcpToolSet>(entity = "contact", idName = "contactId", target = ::contactTarget) { stubFoundContact(isDeleted = false) } +
        entityTagLinkCase<ChecklistMcpToolSet>(entity = "checklist", idName = "checklistId", target = ::checklistTarget) { stubFoundChecklist(isDeleted = false) } +
        entityTagLinkCase<QrMcpToolSet>(entity = "qr", idName = "qrId", target = ::qrTarget) { stubFoundQr(isDeleted = false) } +
        entityTagLinkCase<MusicMcpToolSet>(entity = "music", idName = "musicId", target = ::musicTarget) { stubFoundMusic(isDeleted = false) } +
        entityTagLinkCase<DDayMcpToolSet>(entity = "dday", idName = "ddayId", target = ::ddayTarget) { stubFoundDDay(isDeleted = false) }

private inline fun <reified T : McpToolSet> entityTagLinkCase(
    entity: String,
    idName: String,
    noinline target: (isDeleted: Boolean) -> Target,
    noinline savedEntity: () -> StubbedFind,
): List<TargetCase> =
    listOf("${entity}_tag_add", "${entity}_tag_remove").flatMap { toolName ->
        listOf(
            linkCase<T>(toolName = toolName, idName = idName, target = target) { stubFoundTag(isDeleted = false).toSaved(idName = "tagId") },
            linkCase<T>(toolName = toolName, idName = "tagId", target = ::tagTarget) { savedEntity().toSaved(idName = idName) },
        )
    }

private fun tagLinkCaseList(): List<TargetCase> =
    listOf("tag_link_add", "tag_link_remove").flatMap { toolName ->
        listOf(
            linkCase<TagLinkMcpToolSet>(toolName = toolName, idName = "tagId", target = ::tagTarget) { tag -> tag.savedTag(idName = "linkedTagId") },
            linkCase<TagLinkMcpToolSet>(toolName = toolName, idName = "linkedTagId", target = ::tagTarget) { tag -> tag.savedTag(idName = "tagId") },
        )
    }

// 두 태그를 같은 조회 기능으로 찾으므로, 대상 태그의 조회 기능이 저장된 태그도 답하게 한다.
private fun Target.savedTag(idName: String): Saved {
    val saved = Arb.tag().next().copy(isDeleted = false)
    every { (findUseCase as FindTagUseCase)(parameter = saved.id) } returns flowOf(Result.success(saved))

    return Saved(arguments = mapOf(idName to JsonPrimitive(saved.id.toString())), useCaseList = emptyList())
}

private inline fun <reified T : McpToolSet> linkCase(
    toolName: String,
    idName: String,
    noinline target: (isDeleted: Boolean) -> Target,
    noinline saved: (Target) -> Saved = { Saved(arguments = emptyMap(), useCaseList = emptyList()) },
): TargetCase =
    TargetCase(
        toolName = toolName,
        idName = idName,
        toolSet = { useCaseList -> toolSetOf<T>(*useCaseList.toTypedArray()) },
        target = target,
        expected = notDone,
        saved = saved,
    )

private fun listCaseList(): List<TargetCase> =
    listOf(
        TargetCase(toolName = "tag_memo_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_memo_finished_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_web_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_place_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_contact_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_checklist_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_music_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_qr_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "tag_dday_list", idName = "tagId", toolSet = { useCaseList -> toolSetOf<TagItemMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::tagTarget),
        TargetCase(toolName = "place_memo_list", idName = "placeId", toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::placeTarget),
        TargetCase(toolName = "place_memo_finished_list", idName = "placeId", toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::placeTarget),
        TargetCase(toolName = "web_memo_list", idName = "webId", toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::webTarget),
        TargetCase(toolName = "web_memo_finished_list", idName = "webId", toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::webTarget),
        TargetCase(toolName = "contact_memo_list", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
        TargetCase(toolName = "contact_memo_finished_list", idName = "contactId", toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) }, target = ::contactTarget),
    )
