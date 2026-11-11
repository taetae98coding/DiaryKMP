package io.github.taetae98coding.diary.work.appaction.checklist

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.core.testing.checklist.checklistSummary
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.CheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.GetChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.ResetChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.RestoreChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UncheckChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.UpdateChecklistUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.expected
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class ChecklistAppActionTest :
    FunSpec({
        test("목록은 체크한 항목 수와 함께 MCP와 같은 항목을 돌려준다") {
            runTest {
                val summaryList =
                    Arb.list(Arb.checklistSummary(), 1..3).next()
                val pageChecklistUseCase = mockk<PageChecklistUseCase>()
                every { pageChecklistUseCase() } returns flowOf(Result.success(PagingData.from(summaryList)))
                val action = checklistAppActionOf(pageChecklistUseCase)

                action.list(cursor = null) shouldBe AppActionPage(items = summaryList.map { summary -> summary.expected() }, nextCursor = null)
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val summaryList = Arb.list(Arb.checklistSummary(), 101..150).next()
                val pageChecklistUseCase = mockk<PageChecklistUseCase>()
                every { pageChecklistUseCase() } answers { flowOf(Result.success(PagingData.from(summaryList))) }
                val action = checklistAppActionOf(pageChecklistUseCase)

                val firstPage = action.list(cursor = null)
                val secondPage = action.list(cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = summaryList.take(100).map { summary -> summary.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = summaryList.drop(100).map { summary -> summary.expected() }, nextCursor = null)
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 체크리스트 하나를 고른 정렬의 항목, 연결된 태그와 함께 돌려준다") {
            runTest {
                val checklist = Arb.checklist().next().copy(isDeleted = false)
                val itemList = Arb.list(Arb.checklistItem(), 0..3).next()
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                val getChecklistItemUseCase = mockk<GetChecklistItemUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
                every { getChecklistItemUseCase(parameter = any()) } returns flowOf(Result.success(itemList))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CHECKLIST, entityId = checklist.id)) } returns flowOf(Result.success(tagList))
                val action = checklistAppActionOf(findChecklistUseCase, getChecklistItemUseCase, getEntityTagUseCase)
                val expected =
                    AppActionChecklistDetail(
                        checklist =
                            AppActionChecklist(
                                id = checklist.id.toString(),
                                title = checklist.detail.title,
                                description = checklist.detail.description,
                                createdAt = checklist.createdAt.toString(),
                                updatedAt = checklist.updatedAt.toString(),
                            ),
                        items = itemList.map { item -> item.expected() },
                        tags = tagList.map { tag -> tag.expected() },
                    )

                action.get(checklistId = checklist.id.toString(), itemSort = null) shouldBe expected
                action.get(checklistId = checklist.id.toString(), itemSort = "unchecked_first") shouldBe expected

                verify(exactly = 1) { getChecklistItemUseCase(parameter = GetChecklistItemUseCase.Parameter(checklistId = checklist.id, sort = ChecklistItemSort.ADDED)) }
                verify(exactly = 1) { getChecklistItemUseCase(parameter = GetChecklistItemUseCase.Parameter(checklistId = checklist.id, sort = ChecklistItemSort.UNCHECKED_FIRST)) }
            }
        }

        test("항목 정렬이 고를 수 있는 값이 아니면 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val checklist = Arb.checklist().next().copy(isDeleted = false)
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                val getChecklistItemUseCase = mockk<GetChecklistItemUseCase>()
                every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
                val action = checklistAppActionOf(findChecklistUseCase, getChecklistItemUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.get(checklistId = checklist.id.toString(), itemSort = "title") }.message shouldBe
                    "'itemSort' must be one of added, unchecked_first."
                verify(exactly = 0) { getChecklistItemUseCase(parameter = any()) }
            }
        }

        test("없는 체크리스트를 조회하면 null을, id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val checklistId = Arb.kotlinUuid().next()
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                every { findChecklistUseCase(parameter = checklistId) } returns flowOf(Result.success(null))
                val action = checklistAppActionOf(findChecklistUseCase)

                action.get(checklistId = checklistId.toString(), itemSort = null) shouldBe null
                shouldThrow<AppActionException.InvalidArgument> { action.get(checklistId = "not-id", itemSort = null) }.message shouldBe "'checklistId' must be an id."
                verify(exactly = 1) { findChecklistUseCase(parameter = any()) }
            }
        }

        test("체크리스트를 추가하면 받은 제목, 설명, 항목으로 추가하고 id를 돌려준다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val description = Arb.string().next()
                val itemList = Arb.list(Arb.string(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddChecklistUseCase.Parameter>()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = checklistAppActionOf(addChecklistUseCase)

                action.add(title = title, description = description, items = itemList, tagIds = emptyList()) shouldBe AppActionId(id = addedId.toString())
                action.add(title = title, description = null, items = emptyList(), tagIds = emptyList())

                coVerify(exactly = 1) {
                    addChecklistUseCase(
                        parameter = AddChecklistUseCase.Parameter(detail = ChecklistDetail(title = title, description = description), itemContentList = itemList, tagIdSet = emptySet()),
                    )
                }
                parameter.captured shouldBe AddChecklistUseCase.Parameter(detail = ChecklistDetail(title = title, description = ""), itemContentList = emptyList(), tagIdSet = emptySet())
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고, 없는 체크리스트는 고친 수 0을 돌려준다") {
            runTest {
                val checklist = Arb.checklist().next().copy(isDeleted = false)
                val missingId = Arb.kotlinUuid().next()
                val description = Arb.string().next()
                val changedCount = Arb.int(0..1).next()
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                val updateChecklistUseCase = mockk<UpdateChecklistUseCase>()
                every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
                every { findChecklistUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                coEvery { updateChecklistUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = checklistAppActionOf(findChecklistUseCase, updateChecklistUseCase)

                action.update(checklistId = checklist.id.toString(), title = null, description = description) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.update(checklistId = missingId.toString(), title = Arb.nonBlankString().next(), description = null) shouldBe AppActionChangedCount(changedCount = 0)

                coVerify(exactly = 1) { updateChecklistUseCase(parameter = any()) }
                coVerify(exactly = 1) { updateChecklistUseCase(parameter = UpdateChecklistUseCase.Parameter(id = checklist.id, detail = checklist.detail.copy(description = description))) }
            }
        }

        test("리셋·삭제·되돌리기는 체크리스트 id로 실행해 바뀐 수를 돌려준다") {
            runTest {
                val checklistId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..10).next()
                val resetChecklistUseCase = mockk<ResetChecklistUseCase>()
                val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
                val restoreChecklistUseCase = mockk<RestoreChecklistUseCase>()
                coEvery { resetChecklistUseCase(parameter = checklistId) } returns Result.success(changedCount)
                coEvery { deleteChecklistUseCase(parameter = checklistId) } returns Result.success(changedCount)
                coEvery { restoreChecklistUseCase(parameter = checklistId) } returns Result.success(changedCount)
                val action = checklistAppActionOf(resetChecklistUseCase, deleteChecklistUseCase, restoreChecklistUseCase)
                val expected = AppActionChangedCount(changedCount = changedCount)

                action.reset(checklistId = checklistId.toString()) shouldBe expected
                action.delete(checklistId = checklistId.toString()) shouldBe expected
                action.restore(checklistId = checklistId.toString()) shouldBe expected
            }
        }

        test("항목 추가는 체크리스트와 내용으로 추가한 수를, 체크·체크 해제·삭제는 항목 id로 바뀐 수를 돌려준다") {
            runTest {
                val checklistId = Arb.kotlinUuid().next()
                val itemId = Arb.kotlinUuid().next()
                val content = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                val checkChecklistItemUseCase = mockk<CheckChecklistItemUseCase>()
                val uncheckChecklistItemUseCase = mockk<UncheckChecklistItemUseCase>()
                val deleteChecklistItemUseCase = mockk<DeleteChecklistItemUseCase>()
                coEvery { addChecklistItemUseCase(parameter = AddChecklistItemUseCase.Parameter(checklistId = checklistId, content = content)) } returns Result.success(changedCount)
                coEvery { checkChecklistItemUseCase(parameter = itemId) } returns Result.success(changedCount)
                coEvery { uncheckChecklistItemUseCase(parameter = itemId) } returns Result.success(changedCount)
                coEvery { deleteChecklistItemUseCase(parameter = itemId) } returns Result.success(changedCount)
                val action = checklistItemAppActionOf(addChecklistItemUseCase, checkChecklistItemUseCase, uncheckChecklistItemUseCase, deleteChecklistItemUseCase)
                val expected = AppActionChangedCount(changedCount = changedCount)

                action.add(checklistId = checklistId.toString(), content = content) shouldBe expected
                action.check(itemId = itemId.toString()) shouldBe expected
                action.uncheck(itemId = itemId.toString()) shouldBe expected
                action.delete(itemId = itemId.toString()) shouldBe expected
            }
        }

        test("항목 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val checkChecklistItemUseCase = mockk<CheckChecklistItemUseCase>()
                val addChecklistItemUseCase = mockk<AddChecklistItemUseCase>()
                val action = checklistItemAppActionOf(checkChecklistItemUseCase, addChecklistItemUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.check(itemId = "not-id") }.message shouldBe "'itemId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(checklistId = "not-id", content = "content") }.message shouldBe "'checklistId' must be an id."
                coVerify(exactly = 0) { checkChecklistItemUseCase(parameter = any()) }
                coVerify(exactly = 0) { addChecklistItemUseCase(parameter = any()) }
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = checklistAppActionOf(addChecklistUseCase)

                shouldThrow<AppActionException.Failure> { action.add(title = " ", description = null, items = emptyList(), tagIds = emptyList()) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 체크리스트의 상세는 없는 체크리스트와 같이 null을 돌려준다") {
            runTest {
                val checklist = Arb.checklist().next().copy(isDeleted = true)
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
                val action = checklistAppActionOf(findChecklistUseCase, getEntityTagUseCase)

                action.get(checklistId = checklist.id.toString(), itemSort = null) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddChecklistUseCase.Parameter>()
                val addUseCase = mockk<AddChecklistUseCase>()
                coEvery { addUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = checklistAppActionOf(addUseCase)

                action.add(title = Arb.nonBlankString().next(), description = null, items = emptyList(), tagIds = tagIdSet.map { tagId -> tagId.toString() }) shouldBe AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("추가 요청의 태그 id가 id 형식이 아니면 추가하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addUseCase = mockk<AddChecklistUseCase>()
                val action = checklistAppActionOf(addUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(title = Arb.nonBlankString().next(), description = null, items = emptyList(), tagIds = listOf("tag")) }.message shouldBe "'tagIds' must be an id."
                coVerify(exactly = 0) { addUseCase(parameter = any()) }
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 체크리스트 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val checklistId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId)) }
            }
        }

        test("태그 연결의 id가 id 형식이 아니면 연결하지 않고, 연결이 실패하면 MCP와 같은 메시지로 실패한다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = "not-id", tagId = Arb.kotlinUuid().next().toString()) }.message shouldBe "'checklistId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = Arb.kotlinUuid().next().toString(), tagId = "tag") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.Failure> {
                    action.remove(kind = AppActionEntityTagKind.CHECKLIST, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
                coVerify(exactly = 0) { addEntityTagUseCase(parameter = any()) }
            }
        }

        test("삭제된 체크리스트도 포함하는 조회는 삭제된 체크리스트를 돌려준다") {
            runTest {
                val checklist = Arb.checklist().next().copy(isDeleted = true)
                val findChecklistUseCase = mockk<FindChecklistUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findChecklistUseCase(parameter = checklist.id) } returns flowOf(Result.success(checklist))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CHECKLIST, entityId = checklist.id)) } returns flowOf(Result.success(emptyList()))
                val getChecklistItemUseCase = mockk<GetChecklistItemUseCase>()
                every { getChecklistItemUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                val action = checklistAppActionOf(findChecklistUseCase, getEntityTagUseCase, getChecklistItemUseCase)

                action.getIncludingDeleted(checklistId = checklist.id.toString(), itemSort = null)?.checklist?.id shouldBe checklist.id.toString()
            }
        }
    })

private fun ChecklistSummary.expected(): AppActionChecklistSummary =
    AppActionChecklistSummary(
        id = checklist.id.toString(),
        title = checklist.detail.title,
        description = checklist.detail.description,
        checkedCount = checkedCount,
        totalCount = totalCount,
        createdAt = checklist.createdAt.toString(),
        updatedAt = checklist.updatedAt.toString(),
    )

private fun ChecklistItem.expected(): AppActionChecklistItem =
    AppActionChecklistItem(
        id = id.toString(),
        content = content,
        isChecked = isChecked,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
