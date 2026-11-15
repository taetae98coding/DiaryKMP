package io.github.taetae98coding.diary.work.mcp.tool.tag

import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineSummaryUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.github.taetae98coding.diary.work.appaction.core.AppActionRandomColor
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.checklist.ChecklistMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.contact.ContactMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.dday.DDayMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoLinkMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.memo.MemoMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.music.MusicMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.place.PlaceMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.qr.QrMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.routine.RoutineMcpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.github.taetae98coding.diary.work.mcp.tool.web.WebMcpToolSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.uuid.Uuid

class EntityTagMcpToolTest :
    FunSpec({
        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                    val addedId = Arb.kotlinUuid().next()
                    val added = case.stubAdd(addedId)
                    val arguments = JsonObject(case.addArguments() + ("tagIds" to JsonArray(tagIdSet.map { tagId -> JsonPrimitive(tagId.toString()) })))

                    val result = case.toolSet(listOf(added.useCase)).tool(name = "${case.entity}_add").call(arguments = arguments)

                    result shouldBe McpToolCallResult.Success(content = buildJsonObject { put("id", addedId.toString()) })
                    added.tagIdSet() shouldBe tagIdSet
                }
            }
        }

        test("추가 요청에 태그를 담지 않으면 연결 없이 추가한다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val added = case.stubAdd(Arb.kotlinUuid().next())

                    case.toolSet(listOf(added.useCase)).tool(name = "${case.entity}_add").call(arguments = JsonObject(case.addArguments()))

                    added.tagIdSet() shouldBe emptySet()
                }
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 저장된 항목에 태그를 하나씩 연결하고 해제한다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val entity = case.stubFound(false)
                    val tag = stubFoundTag(isDeleted = false)
                    val entityId = entity.id
                    val tagId = tag.id
                    val link = case.stubLink(Result.success(Unit))
                    val toolSet = case.linkToolSet(link.useCaseList + entity.useCaseList + tag.useCaseList)
                    val arguments = JsonObject(mapOf(case.idName to JsonPrimitive(entityId.toString()), "tagId" to JsonPrimitive(tagId.toString())))
                    val done = McpToolCallResult.Success(content = buildJsonObject { put("done", true) })

                    toolSet.tool(name = "${case.entity}_tag_add").call(arguments = arguments) shouldBe done
                    toolSet.tool(name = "${case.entity}_tag_remove").call(arguments = arguments) shouldBe done

                    link.linkedList() shouldBe listOf(link.expectedLinked(entityId, tagId))
                    link.unlinkedList() shouldBe listOf(link.expectedUnlinked(entityId, tagId))
                }
            }
        }

        test("항목이나 태그를 지정하지 않거나 id 형식이 아니면 연결하지 않고 실패를 알린다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val link = case.stubLink(Result.success(Unit))
                    val toolSet = case.linkToolSet(link.useCaseList)
                    val validId = JsonPrimitive(Arb.kotlinUuid().next().toString())

                    toolSet.tool(name = "${case.entity}_tag_add").call(arguments = JsonObject(mapOf("tagId" to validId))) shouldBe
                        McpToolCallResult.Failure(message = "'${case.idName}' is required.")
                    toolSet.tool(name = "${case.entity}_tag_remove").call(arguments = JsonObject(mapOf(case.idName to validId, "tagId" to JsonPrimitive("tag")))) shouldBe
                        McpToolCallResult.Failure(message = "'tagId' must be an id.")
                    link.linkedList() shouldBe emptyList()
                    link.unlinkedList() shouldBe emptyList()
                }
            }
        }

        test("TC-78e9deb8-0990-4954-9903-4337415a5943 태그를 연결하는 앱 기능이 실패하면 실패를 알린다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val entity = case.stubFound(false)
                    val tag = stubFoundTag(isDeleted = false)
                    val link = case.stubLink(Result.failure(IllegalStateException(Arb.nonBlankString().next())))
                    val arguments = JsonObject(mapOf(case.idName to JsonPrimitive(entity.id.toString()), "tagId" to JsonPrimitive(tag.id.toString())))

                    case
                        .linkToolSet(link.useCaseList + entity.useCaseList + tag.useCaseList)
                        .tool(name = "${case.entity}_tag_add")
                        .call(arguments = arguments)
                        .shouldBeInstanceOf<McpToolCallResult.Failure>()
                }
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 상세 결과에 연결된 태그를 함께 담는다") {
            runTest {
                entityTagCaseList().forEach { case ->
                    val tagList = Arb.list(Arb.tag(), 1..3).next()
                    val found = case.stubFound(false)
                    val arguments = JsonObject(mapOf(case.idName to JsonPrimitive(found.id.toString())))

                    val result = case.toolSet(found.useCaseList + case.stubTag(found.id, tagList)).tool(name = "${case.entity}_get").call(arguments = arguments)

                    val content = result.shouldBeInstanceOf<McpToolCallResult.Success>().content.jsonObject
                    content.getValue(case.entity).jsonObject["id"] shouldBe JsonPrimitive(found.id.toString())
                    content["tags"] shouldBe JsonArray(tagList.map { tag -> tag.toJson() })
                }
            }
        }
    })

private class EntityTagCase(
    val entity: String,
    val idName: String,
    val toolSet: (List<Any>) -> McpToolSet,
    val addArguments: () -> Map<String, JsonElement>,
    val stubAdd: (addedId: Uuid) -> StubbedAdd,
    val stubFound: (isDeleted: Boolean) -> StubbedFind,
    val stubLink: (result: Result<Unit>) -> StubbedLink,
    val stubTag: (entityId: Uuid, tagList: List<Tag>) -> Any,
    // 태그를 연결하고 해제하는 도구가 다른 묶음에 있으면 그 묶음을 만든다.
    val linkToolSet: (List<Any>) -> McpToolSet = toolSet,
)

private class StubbedAdd(
    val useCase: Any,
    val tagIdSet: () -> Set<Uuid>,
)

internal class StubbedFind(
    val id: Uuid,
    val useCaseList: List<Any>,
)

// 연결과 해제 요청이 앱 기능에 넘긴 값과, 그 항목과 태그로 넘겨야 하는 값이다.
private class StubbedLink(
    val useCaseList: List<Any>,
    val linkedList: () -> List<Any>,
    val unlinkedList: () -> List<Any>,
    val expectedLinked: (entityId: Uuid, tagId: Uuid) -> Any,
    val expectedUnlinked: (entityId: Uuid, tagId: Uuid) -> Any,
)

private fun stubEntityLink(
    kind: EntityTagKind,
    result: Result<Unit>,
): StubbedLink {
    val linkedList = mutableListOf<AddEntityTagUseCase.Parameter>()
    val unlinkedList = mutableListOf<RemoveEntityTagUseCase.Parameter>()
    val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
    val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
    coEvery { addEntityTagUseCase(parameter = capture(linkedList)) } returns result
    coEvery { removeEntityTagUseCase(parameter = capture(unlinkedList)) } returns result

    return StubbedLink(
        useCaseList = listOf(addEntityTagUseCase, removeEntityTagUseCase),
        linkedList = { linkedList },
        unlinkedList = { unlinkedList },
        expectedLinked = { entityId, tagId -> AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId) },
        expectedUnlinked = { entityId, tagId -> RemoveEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId) },
    )
}

private fun stubMemoLink(result: Result<Unit>): StubbedLink {
    val linkedList = mutableListOf<AddMemoTagUseCase.Parameter>()
    val unlinkedList = mutableListOf<RemoveMemoTagUseCase.Parameter>()
    val addMemoTagUseCase = mockk<AddMemoTagUseCase>()
    val removeMemoTagUseCase = mockk<RemoveMemoTagUseCase>()
    coEvery { addMemoTagUseCase(parameter = capture(linkedList)) } returns result
    coEvery { removeMemoTagUseCase(parameter = capture(unlinkedList)) } returns result

    return StubbedLink(
        useCaseList = listOf(addMemoTagUseCase, removeMemoTagUseCase),
        linkedList = { linkedList },
        unlinkedList = { unlinkedList },
        expectedLinked = { memoId, tagId -> AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId) },
        expectedUnlinked = { memoId, tagId -> RemoveMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId) },
    )
}

private fun stubEntityTag(
    kind: EntityTagKind,
    entityId: Uuid,
    tagList: List<Tag>,
): GetEntityTagUseCase {
    val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
    every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = kind, entityId = entityId)) } returns flowOf(Result.success(tagList))

    return getEntityTagUseCase
}

private fun entityTagCaseList(): List<EntityTagCase> = listOf(memoTagCase(), placeTagCase(), webTagCase(), contactTagCase(), checklistTagCase(), qrTagCase(), musicTagCase(), ddayTagCase(), routineTagCase())

private fun memoTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "memo",
        idName = "memoId",
        toolSet = { useCaseList -> toolSetOf<MemoMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddMemoUseCase.Parameter>()
            val addMemoUseCase = mockk<AddMemoUseCase>()
            coEvery { addMemoUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addMemoUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundMemo(isDeleted = isDeleted) },
        stubLink = { result -> stubMemoLink(result = result) },
        stubTag = { memoId, tagList ->
            val getMemoTagUseCase = mockk<GetMemoTagUseCase>()
            every { getMemoTagUseCase(parameter = memoId) } returns flowOf(Result.success(tagList))
            getMemoTagUseCase
        },
        linkToolSet = { useCaseList -> toolSetOf<MemoLinkMcpToolSet>(*useCaseList.toTypedArray()) },
    )

private fun placeTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "place",
        idName = "placeId",
        toolSet = { useCaseList -> toolSetOf<PlaceMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = {
            mapOf(
                "title" to JsonPrimitive(Arb.nonBlankString().next()),
                "latitude" to JsonPrimitive(Arb.int(-90..90).next().toString()),
                "longitude" to JsonPrimitive(Arb.int(-180..180).next().toString()),
            )
        },
        stubAdd = { addedId ->
            val parameter = slot<AddPlaceUseCase.Parameter>()
            val addPlaceUseCase = mockk<AddPlaceUseCase>()
            coEvery { addPlaceUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addPlaceUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundPlace(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.PLACE, result = result) },
        stubTag = { placeId, tagList -> stubEntityTag(kind = EntityTagKind.PLACE, entityId = placeId, tagList = tagList) },
    )

private fun webTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "web",
        idName = "webId",
        toolSet = { useCaseList -> toolSetOf<WebMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next()), "url" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddWebUseCase.Parameter>()
            val addWebUseCase = mockk<AddWebUseCase>()
            coEvery { addWebUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addWebUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundWeb(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.WEB, result = result) },
        stubTag = { webId, tagList -> stubEntityTag(kind = EntityTagKind.WEB, entityId = webId, tagList = tagList) },
    )

private fun contactTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "contact",
        idName = "contactId",
        toolSet = { useCaseList -> toolSetOf<ContactMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("name" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddContactUseCase.Parameter>()
            val addContactUseCase = mockk<AddContactUseCase>()
            coEvery { addContactUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addContactUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundContact(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.CONTACT, result = result) },
        stubTag = { contactId, tagList -> stubEntityTag(kind = EntityTagKind.CONTACT, entityId = contactId, tagList = tagList) },
    )

private fun checklistTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "checklist",
        idName = "checklistId",
        toolSet = { useCaseList -> toolSetOf<ChecklistMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddChecklistUseCase.Parameter>()
            val addChecklistUseCase = mockk<AddChecklistUseCase>()
            coEvery { addChecklistUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addChecklistUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundChecklist(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.CHECKLIST, result = result) },
        stubTag = { checklistId, tagList -> stubEntityTag(kind = EntityTagKind.CHECKLIST, entityId = checklistId, tagList = tagList) },
    )

private fun qrTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "qr",
        idName = "qrId",
        toolSet = { useCaseList -> toolSetOf<QrMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next()), "value" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddQrUseCase.Parameter>()
            val addQrUseCase = mockk<AddQrUseCase>()
            coEvery { addQrUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addQrUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundQr(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.QR, result = result) },
        stubTag = { qrId, tagList -> stubEntityTag(kind = EntityTagKind.QR, entityId = qrId, tagList = tagList) },
    )

private fun musicTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "music",
        idName = "musicId",
        toolSet = { useCaseList -> toolSetOf<MusicMcpToolSet>(*useCaseList.toTypedArray()) },
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next())) },
        stubAdd = { addedId ->
            val parameter = slot<AddMusicUseCase.Parameter>()
            val addMusicUseCase = mockk<AddMusicUseCase>()
            coEvery { addMusicUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addMusicUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundMusic(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.MUSIC, result = result) },
        stubTag = { musicId, tagList -> stubEntityTag(kind = EntityTagKind.MUSIC, entityId = musicId, tagList = tagList) },
    )

private fun ddayTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "dday",
        idName = "ddayId",
        toolSet = { useCaseList -> toolSetOf<DDayMcpToolSet>(*useCaseList.toTypedArray()) },
        // 기준일을 담지 않으면 오늘을 쓰므로, 시계 없이 추가할 수 있게 기준일을 담는다.
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next()), "baseDate" to JsonPrimitive(Arb.kotlinxLocalDate().next().toString())) },
        stubAdd = { addedId ->
            val parameter = slot<AddDDayUseCase.Parameter>()
            val addDDayUseCase = mockk<AddDDayUseCase>()
            coEvery { addDDayUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addDDayUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundDDay(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.DDAY, result = result) },
        stubTag = { ddayId, tagList -> stubEntityTag(kind = EntityTagKind.DDAY, entityId = ddayId, tagList = tagList) },
    )

private fun routineTagCase(): EntityTagCase =
    EntityTagCase(
        entity = "routine",
        idName = "routineId",
        toolSet = { useCaseList -> toolSetOf<RoutineMcpToolSet>(*useCaseList.toTypedArray(), AppActionRandomColor()) },
        // 시작 날짜를 담지 않으면 오늘을 쓰므로, 시계 없이 추가할 수 있게 시작 날짜를 담는다.
        addArguments = { mapOf("title" to JsonPrimitive(Arb.nonBlankString().next()), "startDate" to JsonPrimitive(Arb.kotlinxLocalDate().next().toString())) },
        stubAdd = { addedId ->
            val parameter = slot<AddRoutineUseCase.Parameter>()
            val addRoutineUseCase = mockk<AddRoutineUseCase>()
            coEvery { addRoutineUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
            StubbedAdd(useCase = addRoutineUseCase) { parameter.captured.tagIdSet }
        },
        stubFound = { isDeleted -> stubFoundRoutine(isDeleted = isDeleted) },
        stubLink = { result -> stubEntityLink(kind = EntityTagKind.ROUTINE, result = result) },
        stubTag = { routineId, tagList -> stubEntityTag(kind = EntityTagKind.ROUTINE, entityId = routineId, tagList = tagList) },
    )

internal fun stubFoundMemo(isDeleted: Boolean): StubbedFind {
    val memo = Arb.memo().next().copy(isDeleted = isDeleted)
    val findMemoUseCase = mockk<FindMemoUseCase>()
    val getMemoPlaceUseCase = mockk<GetMemoPlaceUseCase>()
    val getMemoWebUseCase = mockk<GetMemoWebUseCase>()
    val getMemoContactUseCase = mockk<GetMemoContactUseCase>()
    every { findMemoUseCase(parameter = memo.id) } returns flowOf(Result.success(memo))
    every { getMemoPlaceUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))
    every { getMemoWebUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))
    every { getMemoContactUseCase(parameter = memo.id) } returns flowOf(Result.success(emptyList()))

    return StubbedFind(id = memo.id, useCaseList = listOf(findMemoUseCase, getMemoPlaceUseCase, getMemoWebUseCase, getMemoContactUseCase))
}

internal fun stubFoundTag(isDeleted: Boolean): StubbedFind {
    val tag = Arb.tag().next().copy(isDeleted = isDeleted)
    val findTagUseCase = mockk<FindTagUseCase>()
    every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))

    return StubbedFind(id = tag.id, useCaseList = listOf(findTagUseCase))
}

internal fun stubFoundPlace(isDeleted: Boolean): StubbedFind {
    val place = Arb.place().next().copy(isDeleted = isDeleted)
    val findPlaceUseCase = mockk<FindPlaceUseCase>()
    every { findPlaceUseCase(parameter = place.id) } returns flowOf(Result.success(place))

    return StubbedFind(id = place.id, useCaseList = listOf(findPlaceUseCase))
}

internal fun stubFoundWeb(isDeleted: Boolean): StubbedFind {
    val web = Arb.web().next().copy(isDeleted = isDeleted)
    val findWebUseCase = mockk<FindWebUseCase>()
    every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))

    return StubbedFind(id = web.id, useCaseList = listOf(findWebUseCase))
}

internal fun stubFoundContact(isDeleted: Boolean): StubbedFind {
    val contact = Arb.contact().next().copy(isDeleted = isDeleted)
    val findContactUseCase = mockk<FindContactUseCase>()
    every { findContactUseCase(parameter = contact.id) } returns flowOf(Result.success(contact))

    return StubbedFind(id = contact.id, useCaseList = listOf(findContactUseCase))
}

internal fun stubFoundChecklist(isDeleted: Boolean): StubbedFind {
    val checklist = Arb.checklist().next().copy(isDeleted = isDeleted)
    val findChecklistUseCase = mockk<FindChecklistUseCase>()
    val getChecklistItemUseCase = mockk<GetChecklistItemUseCase>()
    every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
    every { getChecklistItemUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

    return StubbedFind(id = checklist.id, useCaseList = listOf(findChecklistUseCase, getChecklistItemUseCase))
}

internal fun stubFoundQr(isDeleted: Boolean): StubbedFind {
    val qr = Arb.qr().next().copy(isDeleted = isDeleted)
    val findQrUseCase = mockk<FindQrUseCase>()
    every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))

    return StubbedFind(id = qr.id, useCaseList = listOf(findQrUseCase))
}

internal fun stubFoundMusic(isDeleted: Boolean): StubbedFind {
    val music = Arb.music().next().copy(isDeleted = isDeleted)
    val findMusicUseCase = mockk<FindMusicUseCase>()
    every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))

    return StubbedFind(id = music.id, useCaseList = listOf(findMusicUseCase))
}

// 디데이 상세는 오늘로 계산한 값을 함께 담으므로 시계를 함께 준다.
internal fun stubFoundDDay(isDeleted: Boolean): StubbedFind {
    val dday = Arb.dday().next().copy(isDeleted = isDeleted)
    val findDDayUseCase = mockk<FindDDayUseCase>()
    val clock = mockk<Clock>()
    every { findDDayUseCase(parameter = dday.id) } returns flowOf(Result.success(dday))
    every { clock.now() } returns Arb.secondPrecisionInstant().next()

    return StubbedFind(id = dday.id, useCaseList = listOf(findDDayUseCase, clock))
}

// 루틴 상세는 이번 주기의 합계와 오늘로 계산한 값을 함께 담으므로 합계 조회와 시계를 함께 준다.
internal fun stubFoundRoutine(isDeleted: Boolean): StubbedFind {
    val routine = Arb.routine().next().copy(isDeleted = isDeleted)
    val findRoutineUseCase = mockk<FindRoutineUseCase>()
    val findRoutineSummaryUseCase = mockk<FindRoutineSummaryUseCase>()
    val clock = mockk<Clock>()
    every { findRoutineUseCase(parameter = routine.id) } returns flowOf(Result.success(routine))
    every { findRoutineSummaryUseCase(parameter = any()) } returns flowOf(Result.success(RoutineSummary(routine = routine, cycleAmount = 0)))
    every { clock.now() } returns Arb.secondPrecisionInstant().next()

    return StubbedFind(id = routine.id, useCaseList = listOf(findRoutineUseCase, findRoutineSummaryUseCase, clock))
}
