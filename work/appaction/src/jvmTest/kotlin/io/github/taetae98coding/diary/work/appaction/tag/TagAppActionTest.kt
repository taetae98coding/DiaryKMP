package io.github.taetae98coding.diary.work.appaction.tag

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetLinkedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class TagAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 제목순으로, 담으면 그 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
                every { pageTagHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(tagList)))
                val action = tagAppActionOf(pageTagHomeUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = tagList.map { tag -> tag.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe tagList.map { tag -> tag.expected() }

                verify(exactly = 1) { pageTagHomeUseCase(parameter = ListSort.TITLE) }
                verify(exactly = 1) { pageTagHomeUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("태그 목록이 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
                val pageFinishedTagUseCase = mockk<PageFinishedTagUseCase>()
                val action = tagAppActionOf(pageTagHomeUseCase, pageFinishedTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = "default", cursor = null) }.message shouldBe "'sort' must be one of title, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.finishedList(sort = "name", cursor = null) }.message shouldBe "'sort' must be one of title, recently_updated."
                verify(exactly = 0) { pageTagHomeUseCase(parameter = any()) }
                verify(exactly = 0) { pageFinishedTagUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val tagList = Arb.list(Arb.tag(), 101..150).next()
                val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
                every { pageTagHomeUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(tagList))) }
                val action = tagAppActionOf(pageTagHomeUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = tagList.take(100).map { tag -> tag.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = tagList.drop(100).map { tag -> tag.expected() }, nextCursor = null)
            }
        }

        test("잘못된 페이지 위치는 앱 기능 결과를 읽지 않고 실패를 알린다") {
            runTest {
                val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
                val action = tagAppActionOf(pageTagHomeUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.list(sort = null, cursor = "-1") }.message shouldBe "'cursor' must be a nextCursor value from a previous page."
            }
        }

        test("완료 목록은 완료한 태그 목록 기능으로 조회한다") {
            runTest {
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val pageFinishedTagUseCase = mockk<PageFinishedTagUseCase>()
                every { pageFinishedTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(tagList)))
                val action = tagAppActionOf(pageFinishedTagUseCase)

                action.finishedList(sort = "recently_updated", cursor = null) shouldBe AppActionPage(items = tagList.map { tag -> tag.expected() }, nextCursor = null)
                verify(exactly = 1) { pageFinishedTagUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("목록 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
                every { pageTagHomeUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = tagAppActionOf(pageTagHomeUseCase)

                shouldThrow<AppActionException.Failure> { action.list(sort = null, cursor = null) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("태그 하나를 연결한 하위 태그와 함께 돌려준다") {
            runTest {
                val tag = Arb.tag().next()
                val linkedTagList = Arb.list(Arb.tag(), 0..3).next()
                val findTagUseCase = mockk<FindTagUseCase>()
                val getLinkedTagUseCase = mockk<GetLinkedTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                every { getLinkedTagUseCase(parameter = tag.id) } returns flowOf(Result.success(linkedTagList))
                val action = tagAppActionOf(findTagUseCase, getLinkedTagUseCase)

                action.get(tagId = tag.id.toString()) shouldBe AppActionTagDetail(tag = tag.expected(), linkedTags = linkedTagList.map { linkedTag -> linkedTag.expected() })
            }
        }

        test("없는 태그를 조회하면 하위 태그를 읽지 않고 null을 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val findTagUseCase = mockk<FindTagUseCase>()
                val getLinkedTagUseCase = mockk<GetLinkedTagUseCase>()
                every { findTagUseCase(parameter = tagId) } returns flowOf(Result.success(null))
                val action = tagAppActionOf(findTagUseCase, getLinkedTagUseCase)

                action.get(tagId = tagId.toString()) shouldBe null
                verify(exactly = 0) { getLinkedTagUseCase(parameter = any()) }
            }
        }

        test("태그 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findTagUseCase = mockk<FindTagUseCase>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                val action = tagAppActionOf(findTagUseCase, finishTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.get(tagId = "not-id") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.update(tagId = "not-id", emoji = null, title = null, description = null, color = null) }.message shouldBe
                    "'tagId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.finish(tagId = "not-id") }.message shouldBe "'tagId' must be an id."
                verify(exactly = 0) { findTagUseCase(parameter = any()) }
                coVerify(exactly = 0) { finishTagUseCase(parameter = any()) }
            }
        }

        test("태그를 추가하면 받은 값으로 추가하고 추가한 태그의 id를 돌려준다") {
            runTest {
                val detail = Arb.tagDetail().next()
                val linkedTagIdList = Arb.list(Arb.kotlinUuid(), 0..3).next()
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddTagUseCase.Parameter>()
                val addTagUseCase = mockk<AddTagUseCase>()
                coEvery { addTagUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = tagAppActionOf(addTagUseCase)

                val result =
                    action.add(
                        title = detail.title,
                        emoji = detail.emoji,
                        description = detail.description,
                        color = detail.color.expectedHex(),
                        linkedTagIds = linkedTagIdList.map(Uuid::toString),
                    )

                result shouldBe AppActionId(id = addedId.toString())
                parameter.captured shouldBe
                    AddTagUseCase.Parameter(
                        detail = detail.copy(color = (OPAQUE or (detail.color and RGB)).toInt().toLong()),
                        linkedTagIdSet = linkedTagIdList.toSet(),
                    )
            }
        }

        test("이모지, 설명, 색을 담지 않으면 빈 이모지와 설명, 불투명한 무작위 색으로 추가한다") {
            runTest {
                val title = Arb.nonBlankString().next()
                val parameter = slot<AddTagUseCase.Parameter>()
                val addTagUseCase = mockk<AddTagUseCase>()
                coEvery { addTagUseCase(parameter = capture(parameter)) } returns Result.success(Arb.kotlinUuid().next())
                val action = tagAppActionOf(addTagUseCase)

                action.add(title = title, emoji = null, description = null, color = null, linkedTagIds = emptyList())

                parameter.captured.detail.title shouldBe title
                parameter.captured.detail.emoji shouldBe ""
                parameter.captured.detail.description shouldBe ""
                parameter.captured.linkedTagIdSet shouldBe emptySet()
                (parameter.captured.detail.color and OPAQUE) shouldBe OPAQUE
            }
        }

        test("잘못된 색이나 연결 태그 id는 앱 기능을 실행하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addTagUseCase = mockk<AddTagUseCase>()
                val action = tagAppActionOf(addTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), emoji = null, description = null, color = "red", linkedTagIds = emptyList())
                }.message shouldBe "'color' must be a hex color like #4285F4."
                shouldThrow<AppActionException.InvalidArgument> {
                    action.add(title = Arb.nonBlankString().next(), emoji = null, description = null, color = null, linkedTagIds = listOf("tag"))
                }.message shouldBe "'linkedTagIds' must be an id."
                coVerify(exactly = 0) { addTagUseCase(parameter = any()) }
            }
        }

        test("앱 기능이 태그 추가를 거부하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addTagUseCase = mockk<AddTagUseCase>()
                coEvery { addTagUseCase(parameter = any()) } returns Result.failure(IllegalArgumentException(message))
                val action = tagAppActionOf(addTagUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.add(title = " ", emoji = null, description = null, color = null, linkedTagIds = emptyList())
                }.message shouldBe "IllegalArgumentException: $message"
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val tag = Arb.tag().next()
                val title = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findTagUseCase = mockk<FindTagUseCase>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                coEvery { updateTagUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = tagAppActionOf(findTagUseCase, updateTagUseCase)

                action.update(tagId = tag.id.toString(), emoji = null, title = title, description = null, color = null) shouldBe AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateTagUseCase(parameter = UpdateTagUseCase.Parameter(id = tag.id, detail = tag.detail.copy(title = title))) }
            }
        }

        test("빈 이모지를 담으면 이모지를 지우고, 색을 담으면 불투명한 그 색으로 고친다") {
            runTest {
                val tag = Arb.tag().next()
                val color = Arb.tagDetail().next().color
                val findTagUseCase = mockk<FindTagUseCase>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                every { findTagUseCase(parameter = tag.id) } returns flowOf(Result.success(tag))
                coEvery { updateTagUseCase(parameter = any()) } returns Result.success(1)
                val action = tagAppActionOf(findTagUseCase, updateTagUseCase)

                action.update(tagId = tag.id.toString(), emoji = "", title = null, description = null, color = color.expectedHex())

                coVerify(exactly = 1) {
                    updateTagUseCase(
                        parameter = UpdateTagUseCase.Parameter(id = tag.id, detail = tag.detail.copy(emoji = "", color = (OPAQUE or (color and RGB)).toInt().toLong())),
                    )
                }
            }
        }

        test("없는 태그를 고치면 고친 수 0을 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val findTagUseCase = mockk<FindTagUseCase>()
                val updateTagUseCase = mockk<UpdateTagUseCase>()
                every { findTagUseCase(parameter = tagId) } returns flowOf(Result.success(null))
                val action = tagAppActionOf(findTagUseCase, updateTagUseCase)

                action.update(tagId = tagId.toString(), emoji = null, title = null, description = null, color = null) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateTagUseCase(parameter = any()) }
            }
        }

        test("완료·다시 시작·삭제·되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                val restoreTagUseCase = mockk<RestoreTagUseCase>()
                coEvery { finishTagUseCase(parameter = tagId) } returns Result.success(changedCount)
                coEvery { restartTagUseCase(parameter = tagId) } returns Result.success(changedCount)
                coEvery { deleteTagUseCase(parameter = tagId) } returns Result.success(changedCount)
                coEvery { restoreTagUseCase(parameter = tagId) } returns Result.success(changedCount)
                val action = tagAppActionOf(finishTagUseCase, restartTagUseCase, deleteTagUseCase, restoreTagUseCase)
                val expected = AppActionChangedCount(changedCount = changedCount)

                action.finish(tagId = tagId.toString()) shouldBe expected
                action.restart(tagId = tagId.toString()) shouldBe expected
                action.delete(tagId = tagId.toString()) shouldBe expected
                action.restore(tagId = tagId.toString()) shouldBe expected
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = tagAppActionOf(deleteTagUseCase)

                shouldThrow<AppActionException.Failure> { action.delete(tagId = Arb.kotlinUuid().next().toString()) }.message shouldBe "IllegalStateException: $message"
            }
        }
    })
