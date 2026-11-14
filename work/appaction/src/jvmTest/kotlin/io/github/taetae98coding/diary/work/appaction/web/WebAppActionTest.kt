package io.github.taetae98coding.diary.work.appaction.web

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.domain.web.exception.WebUrlBlankException
import io.github.taetae98coding.diary.domain.web.usecase.AddWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.UpdateWebUseCase
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.memoListAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class WebAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 제목순으로, 담으면 그 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val webList = Arb.list(Arb.web(), 1..3).next()
                val pageWebUseCase = mockk<PageWebUseCase>()
                every { pageWebUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(webList)))
                val action = webAppActionOf(pageWebUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = webList.map { web -> web.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe webList.map { web -> web.expected() }

                verify(exactly = 1) { pageWebUseCase(parameter = ListSort.TITLE) }
                verify(exactly = 1) { pageWebUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("목록이 제공하지 않는 정렬이나 잘못된 위치는 앱 기능을 실행하지 않고 실패한다") {
            runTest {
                val pageWebUseCase = mockk<PageWebUseCase>()
                val action = webAppActionOf(pageWebUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = "name", cursor = null) }.message shouldBe "'sort' must be one of title, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = null, cursor = "-1") }.message shouldBe "'cursor' must be a nextCursor value from a previous page."
                verify(exactly = 0) { pageWebUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알린다") {
            runTest {
                val webList = Arb.list(Arb.web(), 101..150).next()
                val pageWebUseCase = mockk<PageWebUseCase>()
                every { pageWebUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(webList))) }
                val action = webAppActionOf(pageWebUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = webList.take(100).map { web -> web.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = webList.drop(100).map { web -> web.expected() }, nextCursor = null)
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 웹 하나를 요청 헤더, 연결된 태그와 함께 돌려준다") {
            runTest {
                val web = Arb.web().next().copy(isDeleted = false)
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val findWebUseCase = mockk<FindWebUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = web.id)) } returns flowOf(Result.success(tagList))
                val action = webAppActionOf(findWebUseCase, getEntityTagUseCase)

                action.get(webId = web.id.toString()) shouldBe
                    AppActionWebDetail(
                        web =
                            AppActionWebWithHeaders(
                                id = web.id.toString(),
                                title = web.detail.title,
                                description = web.detail.description,
                                url = web.detail.url,
                                createdAt = web.createdAt.toString(),
                                updatedAt = web.updatedAt.toString(),
                                headers = web.detail.headerList.map { header -> AppActionWebHeader(name = header.name, value = header.value) },
                            ),
                        tags = tagList.map { tag -> tag.expected() },
                    )
            }
        }

        test("없는 웹을 조회하면 null을 돌려준다") {
            runTest {
                val webId = Arb.kotlinUuid().next()
                val findWebUseCase = mockk<FindWebUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findWebUseCase(parameter = webId) } returns flowOf(Result.success(null))
                val action = webAppActionOf(findWebUseCase, getEntityTagUseCase)

                action.get(webId = webId.toString()) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("웹 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findWebUseCase = mockk<FindWebUseCase>()
                val restoreWebUseCase = mockk<RestoreWebUseCase>()
                val pageFinishedWebMemoUseCase = mockk<PageFinishedWebMemoUseCase>()
                val action = webAppActionOf(findWebUseCase, restoreWebUseCase)
                val memoListAction = memoListAppActionOf(pageFinishedWebMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.get(webId = "not-id") }.message shouldBe "'webId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.restore(webId = "not-id") }.message shouldBe "'webId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { memoListAction.webFinishedList(webId = "not-id", sort = null, cursor = null) }.message shouldBe "'webId' must be an id."
                verify(exactly = 0) { findWebUseCase(parameter = any()) }
                verify(exactly = 0) { pageFinishedWebMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { restoreWebUseCase(parameter = any()) }
            }
        }

        test("웹의 메모 목록과 완료 메모 목록은 웹과 메모 정렬로 조회한다") {
            runTest {
                val webId = Arb.kotlinUuid().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageWebMemoUseCase = mockk<PageWebMemoUseCase>()
                val pageFinishedWebMemoUseCase = mockk<PageFinishedWebMemoUseCase>()
                every { pageWebMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                every { pageFinishedWebMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = memoListAppActionOf(pageWebMemoUseCase, pageFinishedWebMemoUseCase)
                val expected = AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)

                action.webList(webId = webId.toString(), sort = "recently_updated", cursor = null) shouldBe expected
                action.webFinishedList(webId = webId.toString(), sort = null, cursor = null) shouldBe expected

                verify(exactly = 1) { pageWebMemoUseCase(parameter = PageWebMemoUseCase.Parameter(webId = webId, sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 1) { pageFinishedWebMemoUseCase(parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId, sort = ListSort.DEFAULT)) }
            }
        }

        test("웹을 추가하면 헤더를 첫 콜론에서 나눠 순서대로 추가하고 추가한 웹의 id를 돌려준다") {
            runTest {
                val detail = Arb.webDetail().next()
                val headerName = Arb.alphanumericString(prefix = "Header").next()
                val headerValue = Arb.alphanumericString(prefix = "Value").next()
                val tagIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddWebUseCase.Parameter>()
                val addWebUseCase = mockk<AddWebUseCase>()
                coEvery { addWebUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = webAppActionOf(addWebUseCase)

                val result =
                    action.add(
                        title = detail.title,
                        url = detail.url,
                        description = detail.description,
                        headers = listOf("$headerName: $headerValue", "$headerName:a:b", "Empty:"),
                        tagIds = tagIdList.map(Uuid::toString),
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured shouldBe
                    AddWebUseCase.Parameter(
                        detail =
                            detail.copy(
                                headerList =
                                    listOf(
                                        WebHeader(name = headerName, value = headerValue),
                                        WebHeader(name = headerName, value = "a:b"),
                                        WebHeader(name = "Empty", value = ""),
                                    ),
                            ),
                        tagIdSet = tagIdList.toSet(),
                    )
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddWebUseCase.Parameter>()
                val addWebUseCase = mockk<AddWebUseCase>()
                coEvery { addWebUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = webAppActionOf(addWebUseCase)

                val result =
                    action.add(
                        title = Arb.nonBlankString().next(),
                        url = Arb.nonBlankString().next(),
                        description = null,
                        headers = emptyList(),
                        tagIds = tagIdSet.map { tagId -> tagId.toString() },
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("설명과 헤더를 담지 않으면 빈 설명과 헤더 없이 추가한다") {
            runTest {
                val parameter = slot<AddWebUseCase.Parameter>()
                val addWebUseCase = mockk<AddWebUseCase>()
                coEvery { addWebUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val action = webAppActionOf(addWebUseCase)

                action.add(title = Arb.nonBlankString().next(), url = Arb.nonBlankString().next(), description = null, headers = emptyList(), tagIds = emptyList())

                parameter.captured.detail.description shouldBe ""
                parameter.captured.detail.headerList shouldBe emptyList()
            }
        }

        test("콜론이 없는 헤더나 잘못된 태그 id는 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addWebUseCase = mockk<AddWebUseCase>()
                val action = webAppActionOf(addWebUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), url = Arb.nonBlankString().next(), description = null, headers = listOf("NoColon"), tagIds = emptyList())
                }.message shouldBe "Each 'headers' item must be \"Name: value\"."
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), url = Arb.nonBlankString().next(), description = null, headers = emptyList(), tagIds = listOf("tag"))
                }.message shouldBe "'tagIds' must be an id."
                coVerify(exactly = 0) { addWebUseCase(parameter = any()) }
            }
        }

        test("앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addWebUseCase = mockk<AddWebUseCase>()
                coEvery { addWebUseCase(parameter = any()) } returns Result.failure(WebUrlBlankException())
                val action = webAppActionOf(addWebUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.add(title = Arb.nonBlankString().next(), url = " ", description = null, headers = emptyList(), tagIds = emptyList())
                }.message shouldBe "WebUrlBlankException"
            }
        }

        test("고칠 값을 담지 않은 항목과 헤더는 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val web = Arb.web().next().copy(isDeleted = false)
                val title = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findWebUseCase = mockk<FindWebUseCase>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))
                coEvery { updateWebUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = webAppActionOf(findWebUseCase, updateWebUseCase)

                action.update(webId = web.id.toString(), title = title, description = null, url = null, headers = null) shouldBe AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = web.id, detail = web.detail.copy(title = title))) }
            }
        }

        test("헤더를 담으면 지금 헤더를 모두 바꾸고, 빈 목록이면 모두 지운다") {
            runTest {
                val web = Arb.web().next().copy(isDeleted = false)
                val headerName = Arb.alphanumericString(prefix = "Header").next()
                val headerValue = Arb.alphanumericString(prefix = "Value").next()
                val findWebUseCase = mockk<FindWebUseCase>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))
                coEvery { updateWebUseCase(parameter = any()) } returns Result.success(1)
                val action = webAppActionOf(findWebUseCase, updateWebUseCase)

                action.update(webId = web.id.toString(), title = null, description = null, url = null, headers = listOf("$headerName: $headerValue"))
                action.update(webId = web.id.toString(), title = null, description = null, url = null, headers = emptyList())

                coVerify(exactly = 1) {
                    updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = web.id, detail = web.detail.copy(headerList = listOf(WebHeader(name = headerName, value = headerValue)))))
                }
                coVerify(exactly = 1) { updateWebUseCase(parameter = UpdateWebUseCase.Parameter(id = web.id, detail = web.detail.copy(headerList = emptyList()))) }
            }
        }

        test("없는 웹을 고치면 고친 수 0을 돌려준다") {
            runTest {
                val webId = Arb.kotlinUuid().next()
                val findWebUseCase = mockk<FindWebUseCase>()
                val updateWebUseCase = mockk<UpdateWebUseCase>()
                every { findWebUseCase(parameter = webId) } returns flowOf(Result.success(null))
                val action = webAppActionOf(findWebUseCase, updateWebUseCase)

                action.update(webId = webId.toString(), title = null, description = null, url = null, headers = null) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateWebUseCase(parameter = any()) }
            }
        }

        test("삭제와 되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val webId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                val restoreWebUseCase = mockk<RestoreWebUseCase>()
                coEvery { deleteWebUseCase(parameter = webId) } returns Result.success(changedCount)
                coEvery { restoreWebUseCase(parameter = webId) } returns Result.success(changedCount)
                val action = webAppActionOf(deleteWebUseCase, restoreWebUseCase)

                action.delete(webId = webId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.restore(webId = webId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 웹 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val webId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.WEB, entityId = webId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.WEB, entityId = webId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = webId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = webId, tagId = tagId)) }
            }
        }

        test("태그 연결이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.add(kind = AppActionEntityTagKind.WEB, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 웹의 상세는 없는 웹과 같이 null을 돌려준다") {
            runTest {
                val web = Arb.web().next().copy(isDeleted = true)
                val findWebUseCase = mockk<FindWebUseCase>()
                every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))
                val action = webAppActionOf(findWebUseCase)

                action.get(webId = web.id.toString()) shouldBe null
            }
        }

        test("삭제된 웹도 포함하는 조회는 삭제된 웹를 돌려준다") {
            runTest {
                val web = Arb.web().next().copy(isDeleted = true)
                val findWebUseCase = mockk<FindWebUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findWebUseCase(parameter = web.id) } returns flowOf(Result.success(web))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.WEB, entityId = web.id)) } returns flowOf(Result.success(emptyList()))
                val action = webAppActionOf(findWebUseCase, getEntityTagUseCase)

                action.getIncludingDeleted(webId = web.id.toString())?.web?.id shouldBe web.id.toString()
            }
        }
    })

private const val RGB = 0xFFFFFFL

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

private fun Web.expected(): AppActionWeb =
    AppActionWeb(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        url = detail.url,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun Tag.expected(): AppActionTag =
    AppActionTag(
        id = id.toString(),
        emoji = detail.emoji,
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun Memo.expected(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        dateTime =
            when (val dateTime = detail.dateTime) {
                is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateTime.dateRange.start.toString(), endInclusive = dateTime.dateRange.endInclusive.toString())
                is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = dateTime.start.toString(), endInclusive = dateTime.endInclusive.toString())
                null -> null
            },
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
