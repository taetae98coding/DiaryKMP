package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
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

class TagLinkAppActionTest :
    FunSpec({
        test("태그 연결은 태그에서 하위 태그로 연결하고 완료를 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val linkedTagId = Arb.kotlinUuid().next()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val action = tagLinkAppActionOf(addTagLinkUseCase)

                action.add(tagId = tagId.toString(), linkedTagId = linkedTagId.toString()) shouldBe AppActionDone(done = true)
                coVerify(exactly = 1) { addTagLinkUseCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = tagId, toTagId = linkedTagId)) }
            }
        }

        test("태그 연결 해제는 태그에서 하위 태그로 가는 연결을 해제하고 완료를 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val linkedTagId = Arb.kotlinUuid().next()
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                coEvery { removeTagLinkUseCase(parameter = any()) } returns Result.success(Unit)
                val action = tagLinkAppActionOf(removeTagLinkUseCase)

                action.remove(tagId = tagId.toString(), linkedTagId = linkedTagId.toString()) shouldBe AppActionDone(done = true)
                coVerify(exactly = 1) { removeTagLinkUseCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = tagId, toTagId = linkedTagId)) }
            }
        }

        test("id 형식이 아닌 태그 id는 앱 기능을 실행하지 않고 그 인자 이름으로 실패를 알린다") {
            runTest {
                val tagId = Arb.kotlinUuid().next().toString()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                val removeTagLinkUseCase = mockk<RemoveTagLinkUseCase>()
                val action = tagLinkAppActionOf(addTagLinkUseCase, removeTagLinkUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(tagId = "not-id", linkedTagId = tagId) }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.remove(tagId = tagId, linkedTagId = "not-id") }.message shouldBe "'linkedTagId' must be an id."
                coVerify(exactly = 0) { addTagLinkUseCase(parameter = any()) }
                coVerify(exactly = 0) { removeTagLinkUseCase(parameter = any()) }
            }
        }

        test("앱 기능이 연결을 거부하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val tagId = Arb.kotlinUuid().next().toString()
                val addTagLinkUseCase = mockk<AddTagLinkUseCase>()
                coEvery { addTagLinkUseCase(parameter = any()) } returns Result.failure(IllegalArgumentException(message))
                val action = tagLinkAppActionOf(addTagLinkUseCase)

                shouldThrow<AppActionException.Failure> { action.add(tagId = tagId, linkedTagId = tagId) }.message shouldBe "IllegalArgumentException: $message"
            }
        }
    })
