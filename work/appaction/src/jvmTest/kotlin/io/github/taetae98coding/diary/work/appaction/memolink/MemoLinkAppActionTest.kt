package io.github.taetae98coding.diary.work.appaction.memolink

import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class MemoLinkAppActionTest :
    FunSpec({
        test("태그 연결, 해제, 대표 태그 지정은 받은 id로 앱 기능을 실행하고 MCP와 같은 완료 결과를 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>()
                val removeMemoTagUseCase = mockk<RemoveMemoTagUseCase>()
                val setMemoPrimaryTagUseCase = mockk<SetMemoPrimaryTagUseCase>()
                coEvery { addMemoTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeMemoTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { setMemoPrimaryTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = memoLinkAppActionOf(addMemoTagUseCase, removeMemoTagUseCase, setMemoPrimaryTagUseCase)

                action.tagAdd(memoId = memoId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.tagRemove(memoId = memoId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.primaryTagSet(memoId = memoId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)) }
                coVerify(exactly = 1) { removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId)) }
                coVerify(exactly = 1) { setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memoId, tagId = tagId)) }
            }
        }

        test("대표 태그 해제는 메모 id로 앱 기능을 실행하고 완료 결과를 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val unsetMemoPrimaryTagUseCase = mockk<UnsetMemoPrimaryTagUseCase>()
                coEvery { unsetMemoPrimaryTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = memoLinkAppActionOf(unsetMemoPrimaryTagUseCase)

                action.primaryTagUnset(memoId = memoId.toString()) shouldBe AppActionDone(done = true)
                coVerify(exactly = 1) { unsetMemoPrimaryTagUseCase(parameter = memoId) }
            }
        }

        test("장소, 웹, 연락처 연결과 해제는 받은 id로 앱 기능을 실행하고 완료 결과를 돌려준다") {
            runTest {
                val memoId = Arb.kotlinUuid().next()
                val targetId = Arb.kotlinUuid().next()
                val addMemoPlaceUseCase = mockk<AddMemoPlaceUseCase>()
                val removeMemoPlaceUseCase = mockk<RemoveMemoPlaceUseCase>()
                val addMemoWebUseCase = mockk<AddMemoWebUseCase>()
                val removeMemoWebUseCase = mockk<RemoveMemoWebUseCase>()
                val addMemoContactUseCase = mockk<AddMemoContactUseCase>()
                val removeMemoContactUseCase = mockk<RemoveMemoContactUseCase>()
                coEvery { addMemoPlaceUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeMemoPlaceUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { addMemoWebUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeMemoWebUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { addMemoContactUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeMemoContactUseCase(parameter = any()) } returns Result.success(Unit)
                val action =
                    memoLinkAppActionOf(
                        addMemoPlaceUseCase,
                        removeMemoPlaceUseCase,
                        addMemoWebUseCase,
                        removeMemoWebUseCase,
                        addMemoContactUseCase,
                        removeMemoContactUseCase,
                    )
                val memo = memoId.toString()
                val target = targetId.toString()

                listOf(
                    action.placeAdd(memoId = memo, placeId = target),
                    action.placeRemove(memoId = memo, placeId = target),
                    action.webAdd(memoId = memo, webId = target),
                    action.webRemove(memoId = memo, webId = target),
                    action.contactAdd(memoId = memo, contactId = target),
                    action.contactRemove(memoId = memo, contactId = target),
                ) shouldBe List(size = 6) { AppActionDone(done = true) }

                coVerify(exactly = 1) { addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memoId, placeId = targetId)) }
                coVerify(exactly = 1) { removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memoId, placeId = targetId)) }
                coVerify(exactly = 1) { addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = memoId, webId = targetId)) }
                coVerify(exactly = 1) { removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memoId, webId = targetId)) }
                coVerify(exactly = 1) { addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = memoId, contactId = targetId)) }
                coVerify(exactly = 1) { removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memoId, contactId = targetId)) }
            }
        }

        test("메모나 연결할 항목의 id가 id 형식이 아니면 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val validId = Arb.kotlinUuid().next().toString()
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>()
                val addMemoPlaceUseCase = mockk<AddMemoPlaceUseCase>()
                val addMemoWebUseCase = mockk<AddMemoWebUseCase>()
                val addMemoContactUseCase = mockk<AddMemoContactUseCase>()
                val unsetMemoPrimaryTagUseCase = mockk<UnsetMemoPrimaryTagUseCase>()
                val action = memoLinkAppActionOf(addMemoTagUseCase, addMemoPlaceUseCase, addMemoWebUseCase, addMemoContactUseCase, unsetMemoPrimaryTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.tagAdd(memoId = NOT_ID, tagId = validId) }.message shouldBe "'memoId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.tagAdd(memoId = validId, tagId = NOT_ID) }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.placeAdd(memoId = validId, placeId = NOT_ID) }.message shouldBe "'placeId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.webAdd(memoId = validId, webId = NOT_ID) }.message shouldBe "'webId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.contactAdd(memoId = validId, contactId = NOT_ID) }.message shouldBe "'contactId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.primaryTagUnset(memoId = NOT_ID) }.message shouldBe "'memoId' must be an id."

                coVerify(exactly = 0) { addMemoTagUseCase(parameter = any()) }
                coVerify(exactly = 0) { addMemoPlaceUseCase(parameter = any()) }
                coVerify(exactly = 0) { addMemoWebUseCase(parameter = any()) }
                coVerify(exactly = 0) { addMemoContactUseCase(parameter = any()) }
                coVerify(exactly = 0) { unsetMemoPrimaryTagUseCase(parameter = any()) }
            }
        }

        test("연결하는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addMemoTagUseCase = mockk<AddMemoTagUseCase>()
                coEvery { addMemoTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = memoLinkAppActionOf(addMemoTagUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.tagAdd(memoId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
            }
        }
    })

private const val NOT_ID = "not-id"
