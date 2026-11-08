package io.github.taetae98coding.diary.work.appaction.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.CopyMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class MemoAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 처음 정렬로, 담으면 그 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = memoAppActionOf(pageMemoHomeUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe memoList.map { memo -> memo.expected() }

                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.DEFAULT) }
                verify(exactly = 1) { pageMemoHomeUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("목록이 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                val action = memoAppActionOf(pageMemoHomeUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = "name", cursor = null) }.message shouldBe
                    "'sort' must be one of default, title, recently_updated."
                verify(exactly = 0) { pageMemoHomeUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 101..150).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(memoList))) }
                val action = memoAppActionOf(pageMemoHomeUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = memoList.take(100).map { memo -> memo.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = memoList.drop(100).map { memo -> memo.expected() }, nextCursor = null)
            }
        }

        test("계정이 확정되기 전 요청은 확정된 뒤의 목록을 받는다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns
                    flow {
                        delay(ACCOUNT_CONFIRM_DELAY_MILLIS)
                        emit(Result.success(PagingData.from(memoList)))
                    }
                val action = memoAppActionOf(pageMemoHomeUseCase)

                action.list(sort = null, cursor = null).items shouldBe memoList.map { memo -> memo.expected() }
            }
        }

        test("계정 조회가 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val pageMemoHomeUseCase = mockk<PageMemoHomeUseCase>()
                every { pageMemoHomeUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = memoAppActionOf(pageMemoHomeUseCase)

                shouldThrow<AppActionException.Failure> { action.list(sort = null, cursor = null) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("완료 목록은 완료한 메모 목록 기능으로 조회한다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageFinishedMemoUseCase = mockk<PageFinishedMemoUseCase>()
                every { pageFinishedMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = memoAppActionOf(pageFinishedMemoUseCase)

                action.finishedList(sort = "title", cursor = null) shouldBe AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
                verify(exactly = 1) { pageFinishedMemoUseCase(parameter = ListSort.TITLE) }
            }
        }

        test("메모 하나를 연결된 태그, 장소, 웹, 연락처와 함께 돌려준다") {
            runTest {
                val memo = Arb.memo().next()
                val tagList = Arb.list(Arb.tag(), 0..3).next()
                val placeList = Arb.list(Arb.place(), 0..3).next()
                val webList = Arb.list(Arb.web(), 0..3).next()
                val contactList = Arb.list(Arb.contact(), 0..3).next().map { contact -> ContactListItem(contact = contact, nameQualifier = Arb.string().next()) }
                val findMemoUseCase = mockk<FindMemoUseCase>()
                val getMemoTagUseCase = mockk<GetMemoTagUseCase>()
                val getMemoPlaceUseCase = mockk<GetMemoPlaceUseCase>()
                val getMemoWebUseCase = mockk<GetMemoWebUseCase>()
                val getMemoContactUseCase = mockk<GetMemoContactUseCase>()
                every { findMemoUseCase(parameter = memo.id) } returns flowOf(Result.success(memo))
                every { getMemoTagUseCase(parameter = memo.id) } returns flowOf(Result.success(tagList))
                every { getMemoPlaceUseCase(parameter = memo.id) } returns flowOf(Result.success(placeList))
                every { getMemoWebUseCase(parameter = memo.id) } returns flowOf(Result.success(webList))
                every { getMemoContactUseCase(parameter = memo.id) } returns flowOf(Result.success(contactList))
                val action = memoAppActionOf(findMemoUseCase, getMemoTagUseCase, getMemoPlaceUseCase, getMemoWebUseCase, getMemoContactUseCase)

                action.get(memoId = memo.id.toString()) shouldBe
                    AppActionMemoDetail(
                        memo = memo.expected(),
                        tags =
                            tagList.map { tag ->
                                AppActionTag(
                                    id = tag.id.toString(),
                                    emoji = tag.detail.emoji,
                                    title = tag.detail.title,
                                    description = tag.detail.description,
                                    color = tag.detail.color.expectedHex(),
                                    isFinished = tag.isFinished,
                                    createdAt = tag.createdAt.toString(),
                                    updatedAt = tag.updatedAt.toString(),
                                )
                            },
                        places =
                            placeList.map { place ->
                                AppActionPlace(
                                    id = place.id.toString(),
                                    title = place.detail.title,
                                    description = place.detail.description,
                                    color = place.detail.color.expectedHex(),
                                    address = place.detail.address,
                                    latitude =
                                        place.detail.coordinate.latitude
                                            .takeIf(Double::isFinite),
                                    longitude =
                                        place.detail.coordinate.longitude
                                            .takeIf(Double::isFinite),
                                    createdAt = place.createdAt.toString(),
                                    updatedAt = place.updatedAt.toString(),
                                )
                            },
                        webs =
                            webList.map { web ->
                                AppActionWeb(
                                    id = web.id.toString(),
                                    title = web.detail.title,
                                    description = web.detail.description,
                                    url = web.detail.url,
                                    createdAt = web.createdAt.toString(),
                                    updatedAt = web.updatedAt.toString(),
                                )
                            },
                        contacts =
                            contactList.map { item ->
                                AppActionContact(
                                    id = item.contact.id.toString(),
                                    name = item.contact.detail.name,
                                    nameQualifier = item.nameQualifier,
                                    description = item.contact.detail.description,
                                    isFavorite = item.contact.isFavorite,
                                    createdAt = item.contact.createdAt.toString(),
                                    updatedAt = item.contact.updatedAt.toString(),
                                )
                            },
                    )
            }
        }

        test("없는 메모를 조회하면 null을 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(null))
                val action = memoAppActionOf(findMemoUseCase)

                action.get(memoId = memoId.toString()) shouldBe null
            }
        }

        test("메모 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findMemoUseCase = mockk<FindMemoUseCase>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                val action = memoAppActionOf(findMemoUseCase, finishMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.get(memoId = "not-id") }.message shouldBe "'memoId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.finish(memoId = "not-id") }.message shouldBe "'memoId' must be an id."
                verify(exactly = 0) { findMemoUseCase(parameter = any()) }
                coVerify(exactly = 0) { finishMemoUseCase(parameter = any()) }
            }
        }

        test("메모를 추가하면 받은 값으로 추가하고 추가한 메모의 id를 돌려준다") {
            runTest {
                val detail = Arb.memoDetail().next()
                val dateRange = Arb.localDateRange().next()
                val primaryTagId = Arb.kotlinUuid().next()
                val tagIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val placeIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val webIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val contactIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val color = detail.color.expectedHex()
                val parameter = slot<AddMemoUseCase.Parameter>()
                val addMemoUseCase = mockk<AddMemoUseCase>()
                coEvery { addMemoUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = memoAppActionOf(addMemoUseCase)

                val result =
                    action.add(
                        title = detail.title,
                        description = detail.description,
                        color = color,
                        dateTime = AppActionMemoDateTimeInput(allDay = true, start = dateRange.start.toString(), endInclusive = dateRange.endInclusive.toString()),
                        primaryTagId = primaryTagId.toString(),
                        tagIds = tagIdList.map(Uuid::toString),
                        placeIds = placeIdList.map(Uuid::toString),
                        webIds = webIdList.map(Uuid::toString),
                        contactIds = contactIdList.map(Uuid::toString),
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured shouldBe
                    AddMemoUseCase.Parameter(
                        detail = detail.copy(color = (OPAQUE or (detail.color and RGB)).toInt().toLong(), dateTime = MemoDateTime.AllDay(dateRange = dateRange)),
                        primaryTagId = primaryTagId,
                        tagIdSet = tagIdList.toSet(),
                        placeIdSet = placeIdList.toSet(),
                        webIdSet = webIdList.toSet(),
                        contactIdSet = contactIdList.toSet(),
                    )
            }
        }

        test("색과 설명을 담지 않으면 빈 설명과 불투명한 무작위 색으로 추가한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val parameter = slot<AddMemoUseCase.Parameter>()
                val addMemoUseCase = mockk<AddMemoUseCase>()
                coEvery { addMemoUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val action = memoAppActionOf(addMemoUseCase)

                action.add(title = title, description = null, color = null, dateTime = null, primaryTagId = null, tagIds = emptyList(), placeIds = emptyList(), webIds = emptyList(), contactIds = emptyList())

                parameter.captured.detail.description shouldBe ""
                parameter.captured.detail.dateTime shouldBe null
                (parameter.captured.detail.color and OPAQUE) shouldBe OPAQUE
            }
        }

        test("제목이 비어 있어 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addMemoUseCase = mockk<AddMemoUseCase>()
                coEvery { addMemoUseCase(parameter = any()) } returns Result.failure(MemoTitleBlankException())
                val action = memoAppActionOf(addMemoUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.add(title = " ", description = null, color = null, dateTime = null, primaryTagId = null, tagIds = emptyList(), placeIds = emptyList(), webIds = emptyList(), contactIds = emptyList())
                }.message shouldBe "MemoTitleBlankException"
            }
        }

        test("잘못된 색, 기간, 연결 id는 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val date = Arb.kotlinxLocalDate().next()
                val dateTime = Arb.kotlinxLocalDateTime().next()
                val addMemoUseCase = mockk<AddMemoUseCase>()
                val action = memoAppActionOf(addMemoUseCase)
                val caseList =
                    listOf(
                        AddCase(color = "red") to "'color' must be a hex color like #4285F4.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = null, start = null, endInclusive = null)) to "'dateTime.allDay' is required.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = true, start = null, endInclusive = date.toString())) to "'start' is required.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = true, start = "2026", endInclusive = date.toString())) to "'start' must be a date like 2026-01-31.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = true, start = date.toString(), endInclusive = date.minusDay().toString())) to
                            "'dateTime.endInclusive' must not be before 'dateTime.start'.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = false, start = null, endInclusive = dateTime.toString())) to "'dateTime.start' is required.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = false, start = dateTime.toString(), endInclusive = null)) to "'dateTime.endInclusive' is required.",
                        AddCase(dateTime = AppActionMemoDateTimeInput(allDay = false, start = date.toString(), endInclusive = dateTime.toString())) to
                            "'start' must be a date time like 2026-01-31T09:30.",
                        AddCase(primaryTagId = "tag") to "'primaryTagId' must be an id.",
                        AddCase(tagIds = listOf("tag")) to "'tagIds' must be an id.",
                        AddCase(contactIds = listOf("contact")) to "'contactIds' must be an id.",
                    )

                caseList.forEach { (case, message) ->
                    shouldThrow<AppActionException.InvalidArgument> {
                        action.add(
                            title = Arb.nonBlankString().next(),
                            description = null,
                            color = case.color,
                            dateTime = case.dateTime,
                            primaryTagId = case.primaryTagId,
                            tagIds = case.tagIds,
                            placeIds = emptyList(),
                            webIds = emptyList(),
                            contactIds = case.contactIds,
                        )
                    }.message shouldBe message
                }
                coVerify(exactly = 0) { addMemoUseCase(parameter = any()) }
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val memo = Arb.memo().next()
                val title = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findMemoUseCase = mockk<FindMemoUseCase>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                every { findMemoUseCase(parameter = memo.id) } returns flowOf(Result.success(memo))
                coEvery { updateMemoUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = memoAppActionOf(findMemoUseCase, updateMemoUseCase)

                action.update(memoId = memo.id.toString(), title = title, description = null, color = null, dateTime = null, clearDateTime = null) shouldBe
                    AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = memo.id, detail = memo.detail.copy(title = title))) }
            }
        }

        test("기간 지우기를 요청하면 기간을 지우고, 새 기간을 담으면 그 기간으로 고친다") {
            runTest {
                val memo = Arb.memo().next()
                val start = Arb.kotlinxLocalDateTime().next()
                val findMemoUseCase = mockk<FindMemoUseCase>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                every { findMemoUseCase(parameter = memo.id) } returns flowOf(Result.success(memo))
                coEvery { updateMemoUseCase(parameter = any()) } returns Result.success(1)
                val action = memoAppActionOf(findMemoUseCase, updateMemoUseCase)

                action.update(memoId = memo.id.toString(), title = null, description = null, color = null, dateTime = null, clearDateTime = true)
                action.update(
                    memoId = memo.id.toString(),
                    title = null,
                    description = null,
                    color = null,
                    dateTime = AppActionMemoDateTimeInput(allDay = false, start = start.toString(), endInclusive = start.toString()),
                    clearDateTime = null,
                )

                coVerify(exactly = 1) { updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = memo.id, detail = memo.detail.copy(dateTime = null))) }
                coVerify(exactly = 1) {
                    updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = memo.id, detail = memo.detail.copy(dateTime = MemoDateTime.DateTime(start = start, endInclusive = start))))
                }
            }
        }

        test("없는 메모를 고치면 고친 수 0을 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val findMemoUseCase = mockk<FindMemoUseCase>()
                val updateMemoUseCase = mockk<UpdateMemoUseCase>()
                every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(null))
                val action = memoAppActionOf(findMemoUseCase, updateMemoUseCase)

                action.update(memoId = memoId.toString(), title = null, description = null, color = null, dateTime = null, clearDateTime = null) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateMemoUseCase(parameter = any()) }
            }
        }

        test("복사는 새 메모의 id를, 완료·다시 시작·삭제·되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val copiedId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val copyMemoUseCase = mockk<CopyMemoUseCase>()
                val finishMemoUseCase = mockk<FinishMemoUseCase>()
                val restartMemoUseCase = mockk<RestartMemoUseCase>()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                val restoreMemoUseCase = mockk<RestoreMemoUseCase>()
                coEvery { copyMemoUseCase(parameter = memoId) } returns Result.success(copiedId)
                coEvery { finishMemoUseCase(parameter = memoId) } returns Result.success(changedCount)
                coEvery { restartMemoUseCase(parameter = memoId) } returns Result.success(changedCount)
                coEvery { deleteMemoUseCase(parameter = memoId) } returns Result.success(changedCount)
                coEvery { restoreMemoUseCase(parameter = memoId) } returns Result.success(changedCount)
                val action = memoAppActionOf(copyMemoUseCase, finishMemoUseCase, restartMemoUseCase, deleteMemoUseCase, restoreMemoUseCase)
                val expected = AppActionChangedCount(changedCount = changedCount)

                action.copy(memoId = memoId.toString()) shouldBe AppActionId(id = copiedId.toString())
                action.finish(memoId = memoId.toString()) shouldBe expected
                action.restart(memoId = memoId.toString()) shouldBe expected
                action.delete(memoId = memoId.toString()) shouldBe expected
                action.restore(memoId = memoId.toString()) shouldBe expected
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val deleteMemoUseCase = mockk<DeleteMemoUseCase>()
                coEvery { deleteMemoUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = memoAppActionOf(deleteMemoUseCase)

                shouldThrow<AppActionException.Failure> { action.delete(memoId = Arb.kotlinUuid().next().toString()) }.message shouldBe "IllegalStateException: $message"
            }
        }
    })

private const val ACCOUNT_CONFIRM_DELAY_MILLIS = 1_000L
private const val OPAQUE = 0xFF000000L
private const val RGB = 0xFFFFFFL

private data class AddCase(
    val color: String? = null,
    val dateTime: AppActionMemoDateTimeInput? = null,
    val primaryTagId: String? = null,
    val tagIds: List<String> = emptyList(),
    val contactIds: List<String> = emptyList(),
)

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

private fun LocalDate.minusDay(): LocalDate = LocalDate.fromEpochDays(toEpochDays() - 1)

private fun MemoDateTime.expected(): AppActionMemoDateTime =
    when (this) {
        is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateRange.start.toString(), endInclusive = dateRange.endInclusive.toString())
        is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = start.toString(), endInclusive = endInclusive.toString())
    }

private fun Memo.expected(): AppActionMemo =
    AppActionMemo(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        dateTime = detail.dateTime?.expected(),
        primaryTagId = primaryTagId?.toString(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
