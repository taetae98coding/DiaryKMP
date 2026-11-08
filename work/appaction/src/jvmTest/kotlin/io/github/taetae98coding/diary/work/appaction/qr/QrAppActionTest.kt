package io.github.taetae98coding.diary.work.appaction.qr

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.DeleteQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.RestoreQrUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.UpdateQrUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
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

        test("QR 하나를 MCP와 같은 항목으로 돌려주고, 없으면 null을 돌려준다") {
            runTest {
                val qr = Arb.qr().next()
                val missingId = Arb.kotlinUuid().next()
                val findQrUseCase = mockk<FindQrUseCase>()
                every { findQrUseCase(parameter = qr.id) } returns flowOf(Result.success(qr))
                every { findQrUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                val action = qrAppActionOf(findQrUseCase)

                action.get(qrId = qr.id.toString()) shouldBe qr.expected()
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

                action.add(title = detail.title, description = detail.description, value = detail.value) shouldBe AppActionId(id = addedId.toString())
                coVerify(exactly = 1) { addQrUseCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("설명을 담지 않으면 빈 설명으로 추가한다") {
            runTest {
                val detail = Arb.qrDetail(description = "").next()
                val addQrUseCase = mockk<AddQrUseCase>()
                coEvery { addQrUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val action = qrAppActionOf(addQrUseCase)

                action.add(title = detail.title, description = null, value = detail.value)

                coVerify(exactly = 1) { addQrUseCase(parameter = AddQrUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("값이 비어 있어 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addQrUseCase = mockk<AddQrUseCase>()
                coEvery { addQrUseCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
                val action = qrAppActionOf(addQrUseCase)

                shouldThrow<AppActionException.Failure> { action.add(title = Arb.nonBlankString().next(), description = null, value = "") }.message shouldBe "QrValueEmptyException"
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val qr = Arb.qr().next()
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
                val qr = Arb.qr().next()
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
