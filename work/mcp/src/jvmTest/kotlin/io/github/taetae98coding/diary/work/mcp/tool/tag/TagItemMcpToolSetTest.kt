package io.github.taetae98coding.diary.work.mcp.tool.tag

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.checklist.checklistSummary
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageTagContactUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageTagMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageTagQrUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageTagRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.github.taetae98coding.diary.work.mcp.tool.McpToolCallResult
import io.github.taetae98coding.diary.work.mcp.tool.call
import io.github.taetae98coding.diary.work.mcp.tool.checklist.toJson
import io.github.taetae98coding.diary.work.mcp.tool.dday.toJson
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.music.toJson
import io.github.taetae98coding.diary.work.mcp.tool.qr.toJson
import io.github.taetae98coding.diary.work.mcp.tool.routine.toJson
import io.github.taetae98coding.diary.work.mcp.tool.tool
import io.github.taetae98coding.diary.work.mcp.tool.toolSetOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Clock
import kotlin.uuid.Uuid

class TagItemMcpToolSetTest :
    FunSpec({
        test("태그의 연락처 목록은 범위와 정렬을 담지 않으면 이 태그만, 이름순으로 조회한다") {
            runTest {
                val tag = storedTag()
                val itemList = Arb.list(Arb.contact(), 1..3).next().map { contact -> ContactListItem(contact = contact, nameQualifier = Arb.string().next()) }
                val pageTagContactUseCase = mockk<PageTagContactUseCase>()
                every { pageTagContactUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(itemList)))

                val result = toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagContactUseCase).tool(name = "tag_contact_list").call(arguments = tag.arguments())

                result shouldBe pageOf(itemList.map { item -> item.toJson() })
                verify(exactly = 1) { pageTagContactUseCase(parameter = PageTagContactUseCase.Parameter(tagId = tag.id, scope = TagScope.SELF, sort = ListSort.NAME)) }
            }
        }

        test("태그의 체크리스트 목록은 받은 범위로 조회한다") {
            runTest {
                val tag = storedTag()
                val summaryList =
                    Arb.list(Arb.checklistSummary(), 1..3).next()
                val pageTagChecklistUseCase = mockk<PageTagChecklistUseCase>()
                every { pageTagChecklistUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(summaryList)))

                val result =
                    toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagChecklistUseCase)
                        .tool(name = "tag_checklist_list")
                        .call(arguments = tag.arguments("scope" to JsonPrimitive("child")))

                result shouldBe pageOf(summaryList.map { summary -> summary.toJson() })
                verify(exactly = 1) { pageTagChecklistUseCase(parameter = PageTagChecklistUseCase.Parameter(tagId = tag.id, scope = TagScope.CHILD)) }
            }
        }

        test("태그의 곡 목록은 받은 범위와 정렬로 조회한다") {
            runTest {
                val tag = storedTag()
                val musicList = Arb.list(Arb.music(), 1..3).next()
                val pageTagMusicUseCase = mockk<PageTagMusicUseCase>()
                every { pageTagMusicUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(musicList)))

                val result =
                    toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagMusicUseCase)
                        .tool(name = "tag_music_list")
                        .call(arguments = tag.arguments("scope" to JsonPrimitive("descendant"), "sort" to JsonPrimitive("recently_updated")))

                result shouldBe pageOf(musicList.map { music -> music.toJson() })
                verify(exactly = 1) {
                    pageTagMusicUseCase(parameter = PageTagMusicUseCase.Parameter(tagId = tag.id, scope = TagScope.DESCENDANT, sort = ListSort.RECENTLY_UPDATED))
                }
            }
        }

        test("태그의 QR 목록은 범위를 담지 않으면 이 태그만 조회한다") {
            runTest {
                val tag = storedTag()
                val qrList = Arb.list(Arb.qr(), 1..3).next()
                val pageTagQrUseCase = mockk<PageTagQrUseCase>()
                every { pageTagQrUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(qrList)))

                val result = toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagQrUseCase).tool(name = "tag_qr_list").call(arguments = tag.arguments())

                result shouldBe pageOf(qrList.map { qr -> qr.toJson() })
                verify(exactly = 1) { pageTagQrUseCase(parameter = PageTagQrUseCase.Parameter(tagId = tag.id, scope = TagScope.SELF)) }
            }
        }

        test("태그의 디데이 목록은 오늘로 계산한 값을 함께 담는다") {
            runTest {
                val tag = storedTag()
                val now = Arb.secondPrecisionInstant().next()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val ddayList = Arb.list(Arb.dday(), 1..3).next()
                val pageTagDDayUseCase = mockk<PageTagDDayUseCase>()
                val clock = mockk<Clock>()
                every { pageTagDDayUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(ddayList)))
                every { clock.now() } returns now

                val result = toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagDDayUseCase, clock).tool(name = "tag_dday_list").call(arguments = tag.arguments())

                result shouldBe pageOf(ddayList.map { dday -> dday.toJson(today = today) })
                verify(exactly = 1) { pageTagDDayUseCase(parameter = PageTagDDayUseCase.Parameter(tagId = tag.id, scope = TagScope.SELF)) }
            }
        }

        test("태그의 루틴 목록은 오늘을 넘겨 조회하고 오늘로 계산한 값을 함께 담는다") {
            runTest {
                val tag = storedTag()
                val now = Arb.secondPrecisionInstant().next()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val summaryList = Arb.list(Arb.routineSummary(), 1..3).next()
                val pageTagRoutineUseCase = mockk<PageTagRoutineUseCase>()
                val clock = mockk<Clock>()
                every { pageTagRoutineUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(summaryList)))
                every { clock.now() } returns now

                val result = toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagRoutineUseCase, clock).tool(name = "tag_routine_list").call(arguments = tag.arguments())

                result shouldBe pageOf(summaryList.map { summary -> summary.toJson(today = today) })
                verify(exactly = 1) { pageTagRoutineUseCase(parameter = PageTagRoutineUseCase.Parameter(tagId = tag.id, scope = TagScope.SELF, today = today)) }
            }
        }

        test("TC-e408a0ff-1873-49ab-8f51-d7ca57065fee 태그의 루틴은 완료되지 않은 목록과 완료된 목록을 따로 받는다") {
            runTest {
                val tag = storedTag()
                val now = Arb.secondPrecisionInstant().next()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val unfinished = Arb.routineSummary().next().let { summary -> summary.copy(routine = summary.routine.copy(isFinished = false, isDeleted = false)) }
                val finished = Arb.routineSummary().next().let { summary -> summary.copy(routine = summary.routine.copy(isFinished = true, isDeleted = false)) }
                val pageTagRoutineUseCase = mockk<PageTagRoutineUseCase>()
                val pageFinishedTagRoutineUseCase = mockk<PageFinishedTagRoutineUseCase>()
                val clock = mockk<Clock>()
                every { pageTagRoutineUseCase(parameter = PageTagRoutineUseCase.Parameter(tagId = tag.id, scope = TagScope.SELF, today = today)) } returns
                    flowOf(Result.success(PagingData.from(listOf(unfinished))))
                every { pageFinishedTagRoutineUseCase(parameter = PageFinishedTagRoutineUseCase.Parameter(tagId = tag.id, today = today)) } returns
                    flowOf(Result.success(PagingData.from(listOf(finished))))
                every { clock.now() } returns now
                val toolSet = toolSetOf<TagItemMcpToolSet>(tag.findUseCase, pageTagRoutineUseCase, pageFinishedTagRoutineUseCase, clock)

                toolSet.tool(name = "tag_routine_list").call(arguments = tag.arguments()) shouldBe pageOf(listOf(unfinished.toJson(today = today)))
                toolSet.tool(name = "tag_routine_finished_list").call(arguments = tag.arguments()) shouldBe pageOf(listOf(finished.toJson(today = today)))
                toolSet.tool(name = "tag_routine_finished_list").parameterList.map { parameter -> parameter.name } shouldBe listOf("tagId", "cursor")
            }
        }

        test("제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val tag = storedTag()
                val toolSet = toolSetOf<TagItemMcpToolSet>(tag.findUseCase)

                toolSet.tool(name = "tag_contact_list").call(arguments = tag.arguments("sort" to JsonPrimitive("title"))) shouldBe
                    McpToolCallResult.Failure(message = "'sort' must be one of name, recently_updated.")
                toolSet.tool(name = "tag_music_list").call(arguments = tag.arguments("sort" to JsonPrimitive("name"))) shouldBe
                    McpToolCallResult.Failure(message = "'sort' must be one of title, recently_updated.")
            }
        }
    })

private class StoredTag(
    val id: Uuid,
    val findUseCase: FindTagUseCase,
) {
    fun arguments(vararg extra: Pair<String, JsonElement>): JsonObject = JsonObject(mapOf("tagId" to JsonPrimitive(id.toString())) + extra)
}

private fun storedTag(): StoredTag {
    val tag = Arb.tag().next().copy(isDeleted = false)
    val findTagUseCase = mockk<FindTagUseCase>()
    every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))

    return StoredTag(id = tag.id, findUseCase = findTagUseCase)
}

private fun pageOf(itemList: List<JsonElement>): McpToolCallResult = McpToolCallResult.Success(content = buildJsonObject { put("items", JsonArray(itemList)) })
