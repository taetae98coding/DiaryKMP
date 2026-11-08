package io.github.taetae98coding.diary.work.appaction.tag

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class TagItemAppActionTest :
    FunSpec({
        test("태그의 메모 목록은 범위와 정렬을 담지 않으면 이 태그만, 처음 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = tagItemAppActionOf(pageTagMemoUseCase)

                action.memoList(tagId = tagId.toString(), scope = null, sort = null, cursor = null) shouldBe
                    AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
                verify(exactly = 1) { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.DEFAULT)) }
            }
        }

        test("범위 값은 하위 태그와 모든 하위 태그를 포함하는 범위로 조회한다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                every { pageTagMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList())))
                val action = tagItemAppActionOf(pageTagMemoUseCase)

                listOf("self" to TagScope.SELF, "child" to TagScope.CHILD, "descendant" to TagScope.DESCENDANT).forEach { (scope, expected) ->
                    action.memoList(tagId = tagId.toString(), scope = scope, sort = "title", cursor = null)

                    verify(exactly = 1) { pageTagMemoUseCase(parameter = PageTagMemoUseCase.Parameter(tagId = tagId, scope = expected, sort = ListSort.TITLE)) }
                }
            }
        }

        test("제공하지 않는 범위는 고를 수 있는 범위를 알리며 실패한다") {
            runTest {
                val tagId = Arb.kotlinUuid().next().toString()
                val pageTagMemoUseCase = mockk<PageTagMemoUseCase>()
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                val action = tagItemAppActionOf(pageTagMemoUseCase, pageTagWebUseCase, pageTagPlaceUseCase)
                val message = "'scope' must be one of self, child, descendant."

                shouldThrow<AppActionException.InvalidArgument> { action.memoList(tagId = tagId, scope = "all", sort = null, cursor = null) }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.webList(tagId = tagId, scope = "all", sort = null, cursor = null) }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.placeList(tagId = tagId, scope = "all", sort = null, cursor = null) }.message shouldBe message
                verify(exactly = 0) { pageTagMemoUseCase(parameter = any()) }
                verify(exactly = 0) { pageTagWebUseCase(parameter = any()) }
                verify(exactly = 0) { pageTagPlaceUseCase(parameter = any()) }
            }
        }

        test("메모 목록은 메모 정렬을, 웹과 장소 목록은 태그 정렬을 받는다") {
            runTest {
                val tagId = Arb.kotlinUuid().next().toString()
                val action = tagItemAppActionOf()

                shouldThrow<AppActionException.InvalidArgument> { action.memoList(tagId = tagId, scope = null, sort = "name", cursor = null) }.message shouldBe
                    "'sort' must be one of default, title, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.memoFinishedList(tagId = tagId, sort = "name", cursor = null) }.message shouldBe
                    "'sort' must be one of default, title, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.webList(tagId = tagId, scope = null, sort = "default", cursor = null) }.message shouldBe
                    "'sort' must be one of title, recently_updated."
                shouldThrow<AppActionException.InvalidArgument> { action.placeList(tagId = tagId, scope = null, sort = "default", cursor = null) }.message shouldBe
                    "'sort' must be one of title, recently_updated."
            }
        }

        test("태그 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val pageFinishedTagMemoUseCase = mockk<PageFinishedTagMemoUseCase>()
                val action = tagItemAppActionOf(pageFinishedTagMemoUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.memoFinishedList(tagId = "not-id", sort = null, cursor = null) }.message shouldBe "'tagId' must be an id."
                verify(exactly = 0) { pageFinishedTagMemoUseCase(parameter = any()) }
            }
        }

        test("태그의 완료 메모 목록은 완료한 메모 목록 기능으로 조회한다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val pageFinishedTagMemoUseCase = mockk<PageFinishedTagMemoUseCase>()
                every { pageFinishedTagMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = tagItemAppActionOf(pageFinishedTagMemoUseCase)

                action.memoFinishedList(tagId = tagId.toString(), sort = "recently_updated", cursor = null) shouldBe
                    AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
                verify(exactly = 1) { pageFinishedTagMemoUseCase(parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId, sort = ListSort.RECENTLY_UPDATED)) }
            }
        }

        test("태그의 웹 목록은 이 태그만, 제목순으로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val webList = Arb.list(Arb.web(), 1..3).next()
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(webList)))
                val action = tagItemAppActionOf(pageTagWebUseCase)

                action.webList(tagId = tagId.toString(), scope = null, sort = null, cursor = null) shouldBe AppActionPage(items = webList.map { web -> web.expected() }, nextCursor = null)
                verify(exactly = 1) { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("태그의 장소 목록은 받은 범위와 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val tagId = Arb.kotlinUuid().next()
                val placeList = Arb.list(Arb.place(), 1..3).next()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(placeList)))
                val action = tagItemAppActionOf(pageTagPlaceUseCase)

                action.placeList(tagId = tagId.toString(), scope = "descendant", sort = "recently_updated", cursor = null) shouldBe
                    AppActionPage(items = placeList.map { place -> place.expected() }, nextCursor = null)
                verify(exactly = 1) {
                    pageTagPlaceUseCase(parameter = PageTagPlaceUseCase.Parameter(tagId = tagId, scope = TagScope.DESCENDANT, sort = ListSort.RECENTLY_UPDATED))
                }
            }
        }

        test("태그의 목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val tagId = Arb.kotlinUuid().next().toString()
                val webList = Arb.list(Arb.web(), 101..150).next()
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(webList))) }
                val action = tagItemAppActionOf(pageTagWebUseCase)

                val firstPage = action.webList(tagId = tagId, scope = null, sort = null, cursor = null)
                val secondPage = action.webList(tagId = tagId, scope = null, sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = webList.take(100).map { web -> web.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = webList.drop(100).map { web -> web.expected() }, nextCursor = null)
            }
        }

        test("목록 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val pageTagPlaceUseCase = mockk<PageTagPlaceUseCase>()
                every { pageTagPlaceUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = tagItemAppActionOf(pageTagPlaceUseCase)

                shouldThrow<AppActionException.Failure> { action.placeList(tagId = Arb.kotlinUuid().next().toString(), scope = null, sort = null, cursor = null) }.message shouldBe
                    "IllegalStateException: $message"
            }
        }
    })
