package io.github.taetae98coding.diary.work.appaction.qr

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class QrAppActionTest :
    FunSpec({
        test("목록은 MCP와 같은 항목을 100개씩 나눠 돌려주고, 다음 페이지 위치로 다음 페이지를 받는다") {
            runTest {
                val qrList = Arb.list(Arb.qr(), 101..150).next()
                val pageQrUseCase = mockk<PageQrUseCase>()
                every { pageQrUseCase(parameter = Unit) } answers { flowOf(Result.success(PagingData.from(qrList))) }
                val action = qrAppActionOf(pageQrUseCase)

                val firstPage = action.list(cursor = null)
                val secondPage = action.list(cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = qrList.take(100).map { qr -> qr.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = qrList.drop(100).map { qr -> qr.expected() }, nextCursor = null)
            }
        }

        test("이전 페이지가 알린 위치가 아니면 목록을 조회하지 않고 실패를 알린다") {
            runTest {
                val pageQrUseCase = mockk<PageQrUseCase>()
                val action = qrAppActionOf(pageQrUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(cursor = "abc") }.message shouldBe "'cursor' must be a nextCursor value from a previous page."
                verify(exactly = 0) { pageQrUseCase(parameter = Unit) }
            }
        }

        test("계정 조회가 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val pageQrUseCase = mockk<PageQrUseCase>()
                every { pageQrUseCase(parameter = Unit) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = qrAppActionOf(pageQrUseCase)

                shouldThrow<AppActionException.Failure> { action.list(cursor = null) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 QR 하나를 연결된 태그와 함께 돌려주고, 없으면 null을 돌려준다") {
            runTest {
                val qr = Arb.qr().next().copy(isDeleted = false)
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val missingId = Arb.kotlinUuid().next()
                val findQrUseCase = mockk<FindQrUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                every { findQrUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.QR, entityId = qr.id)) } returns flowOf(Result.success(tagList))
                val action = qrAppActionOf(findQrUseCase, getEntityTagUseCase)

                action.get(qrId = qr.id.toString()) shouldBe AppActionQrDetail(qr = qr.expected(), tags = tagList.map { tag -> tag.expected() })
                action.get(qrId = missingId.toString()) shouldBe null
            }
        }

        test("QR id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findQrUseCase = mockk<FindQrUseCase>()
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                val restoreQrUseCase = mockk<RestoreQrUseCase>()
                val action = qrAppActionOf(findQrUseCase, updateQrUseCase, deleteQrUseCase, restoreQrUseCase)
                val message = "'qrId' must be an id."

                shouldThrow<AppActionException.InvalidArgument> { action.get(qrId = "not-id") }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.update(qrId = "not-id", title = null, description = null, value = null) }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.delete(qrId = "not-id") }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.restore(qrId = "not-id") }.message shouldBe message
                verify(exactly = 0) { findQrUseCase(parameter = any()) }
                coVerify(exactly = 0) { updateQrUseCase(parameter = any()) }
                coVerify(exactly = 0) { deleteQrUseCase(parameter = any()) }
                coVerify(exactly = 0) { restoreQrUseCase(parameter = any()) }
            }
        }

        test("QR을 추가하면 받은 값과 연결 태그 없이 추가하고 추가한 QR의 id를 돌려준다") {
            runTest {
                val detail = Arb.qrDetail().next()
                val addedId = Arb.kotlinUuid().next()
                val addQrUseCase = mockk<AddQrUseCase>()
                coEvery { addQrUseCase(parameter = any()) } returns Result.success(addedId)
                val action = qrAppActionOf(addQrUseCase)

                action.add(title = detail.title, description = detail.description, value = detail.value, tagIds = emptyList()) shouldBe AppActionId(id = addedId.toString())
                coVerify(exactly = 1) { addQrUseCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("설명을 담지 않으면 빈 설명으로 추가한다") {
            runTest {
                val detail = Arb.qrDetail(description = "").next()
                val addQrUseCase = mockk<AddQrUseCase>()
                coEvery { addQrUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val action = qrAppActionOf(addQrUseCase)

                action.add(title = detail.title, description = null, value = detail.value, tagIds = emptyList())

                coVerify(exactly = 1) { addQrUseCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("값이 비어 있어 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addQrUseCase = mockk<AddQrUseCase>()
                coEvery { addQrUseCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
                val action = qrAppActionOf(addQrUseCase)

                shouldThrow<AppActionException.Failure> { action.add(title = Arb.nonBlankString().next(), description = null, value = "", tagIds = emptyList()) }.message shouldBe "QrValueEmptyException"
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val qr = Arb.qr().next().copy(isDeleted = false)
                val value = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findQrUseCase = mockk<FindQrUseCase>()
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                coEvery { updateQrUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = qrAppActionOf(findQrUseCase, updateQrUseCase)

                action.update(qrId = qr.id.toString(), title = null, description = null, value = value) shouldBe AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = qr.id, detail = qr.detail.copy(value = value))) }
            }
        }

        test("담은 값으로 제목, 설명, 값을 모두 고친다") {
            runTest {
                val qr = Arb.qr().next().copy(isDeleted = false)
                val detail = Arb.qrDetail().next()
                val findQrUseCase = mockk<FindQrUseCase>()
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                coEvery { updateQrUseCase(parameter = any()) } returns Result.success(1)
                val action = qrAppActionOf(findQrUseCase, updateQrUseCase)

                action.update(qrId = qr.id.toString(), title = detail.title, description = detail.description, value = detail.value)

                coVerify(exactly = 1) { updateQrUseCase(parameter = UpdateQrUseCase.Parameter(id = qr.id, detail = detail)) }
            }
        }

        test("없는 QR을 고치면 고친 수 0을 돌려준다") {
            runTest {
                val qrId = Arb.kotlinUuid().next()
                val findQrUseCase = mockk<FindQrUseCase>()
                val updateQrUseCase = mockk<UpdateQrUseCase>()
                every { findQrUseCase(parameter = qrId) } returns flowOf(Result.success(null))
                val action = qrAppActionOf(findQrUseCase, updateQrUseCase)

                action.update(qrId = qrId.toString(), title = Arb.nonBlankString().next(), description = null, value = null) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateQrUseCase(parameter = any()) }
            }
        }

        test("삭제와 되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val qrId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val deleteQrUseCase = mockk<DeleteQrUseCase>()
                val restoreQrUseCase = mockk<RestoreQrUseCase>()
                coEvery { deleteQrUseCase(parameter = qrId) } returns Result.success(changedCount)
                coEvery { restoreQrUseCase(parameter = qrId) } returns Result.success(changedCount)
                val action = qrAppActionOf(deleteQrUseCase, restoreQrUseCase)

                action.delete(qrId = qrId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.restore(qrId = qrId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val restoreQrUseCase = mockk<RestoreQrUseCase>()
                coEvery { restoreQrUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = qrAppActionOf(restoreQrUseCase)

                shouldThrow<AppActionException.Failure> { action.restore(qrId = Arb.kotlinUuid().next().toString()) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 QR의 상세는 없는 QR과 같이 null을 돌려준다") {
            runTest {
                val qr = Arb.qr().next().copy(isDeleted = true)
                val findQrUseCase = mockk<FindQrUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                val action = qrAppActionOf(findQrUseCase, getEntityTagUseCase)

                action.get(qrId = qr.id.toString()) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddQrUseCase.Parameter>()
                val addUseCase = mockk<AddQrUseCase>()
                coEvery { addUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = qrAppActionOf(addUseCase)

                action.add(title = Arb.nonBlankString().next(), description = null, value = Arb.nonBlankString().next(), tagIds = tagIdSet.map { tagId -> tagId.toString() }) shouldBe AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("추가 요청의 태그 id가 id 형식이 아니면 추가하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addUseCase = mockk<AddQrUseCase>()
                val action = qrAppActionOf(addUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(title = Arb.nonBlankString().next(), description = null, value = Arb.nonBlankString().next(), tagIds = listOf("tag")) }.message shouldBe "'tagIds' must be an id."
                coVerify(exactly = 0) { addUseCase(parameter = any()) }
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 QR 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val qrId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.QR, entityId = qrId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.QR, entityId = qrId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.QR, entityId = qrId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.QR, entityId = qrId, tagId = tagId)) }
            }
        }

        test("태그 연결의 id가 id 형식이 아니면 연결하지 않고, 연결이 실패하면 MCP와 같은 메시지로 실패한다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.QR, entityId = "not-id", tagId = Arb.kotlinUuid().next().toString()) }.message shouldBe "'qrId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.QR, entityId = Arb.kotlinUuid().next().toString(), tagId = "tag") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.Failure> {
                    action.remove(kind = AppActionEntityTagKind.QR, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
                coVerify(exactly = 0) { addEntityTagUseCase(parameter = any()) }
            }
        }

        test("삭제된 QR도 포함하는 조회는 삭제된 QR를 돌려준다") {
            runTest {
                val qr = Arb.qr().next().copy(isDeleted = true)
                val findQrUseCase = mockk<FindQrUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.QR, entityId = qr.id)) } returns flowOf(Result.success(emptyList()))
                val action = qrAppActionOf(findQrUseCase, getEntityTagUseCase)

                action.getIncludingDeleted(qrId = qr.id.toString())?.qr?.id shouldBe qr.id.toString()
            }
        }
    })

private fun Qr.expected(): AppActionQr =
    AppActionQr(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        value = detail.value,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
