package io.github.taetae98coding.diary.work.appaction.search

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.kotest.assertions.throwables.shouldThrow
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

class SearchAppActionTest :
    FunSpec({
        test("메모 검색은 정렬을 담지 않으면 제목순으로 검색해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val query = Arb.string().next()
                val memoList = Arb.list(Arb.memo(), 1..3).next()
                val searchMemoUseCase = mockk<SearchMemoUseCase>()
                every { searchMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = searchAppActionOf(searchMemoUseCase)

                action.memo(query = query, sort = null, cursor = null) shouldBe AppActionPage(items = memoList.map { memo -> memo.expected() }, nextCursor = null)
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = query, sort = ListSort.TITLE)) }
            }
        }

        test("TC-f61abd56-16fd-4232-8939-3191cee6d724 빈 검색어는 그대로 검색 기능에 넘기고 그 결과를 돌려준다") {
            runTest {
                val memoList = Arb.list(Arb.memo(), 0..3).next()
                val searchMemoUseCase = mockk<SearchMemoUseCase>()
                every { searchMemoUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(memoList)))
                val action = searchAppActionOf(searchMemoUseCase)

                action.memo(query = "", sort = null, cursor = null).items shouldBe memoList.map { memo -> memo.expected() }
                verify(exactly = 1) { searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = "", sort = ListSort.TITLE)) }
            }
        }

        test("태그, 장소, 웹 검색은 고른 정렬로 검색해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val query = Arb.string().next()
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val placeList = Arb.list(Arb.place(), 1..3).next()
                val webList = Arb.list(Arb.web(), 1..3).next()
                val searchTagUseCase = mockk<SearchTagUseCase>()
                val searchPlaceUseCase = mockk<SearchPlaceUseCase>()
                val searchWebUseCase = mockk<SearchWebUseCase>()
                every { searchTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(tagList)))
                every { searchPlaceUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(placeList)))
                every { searchWebUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(webList)))
                val action = searchAppActionOf(searchTagUseCase, searchPlaceUseCase, searchWebUseCase)

                action.tag(query = query, sort = RECENTLY_UPDATED, cursor = null) shouldBe AppActionPage(items = tagList.map { tag -> tag.expected() }, nextCursor = null)
                action.place(query = query, sort = RECENTLY_UPDATED, cursor = null) shouldBe AppActionPage(items = placeList.map { place -> place.expected() }, nextCursor = null)
                action.web(query = query, sort = RECENTLY_UPDATED, cursor = null) shouldBe AppActionPage(items = webList.map { web -> web.expected() }, nextCursor = null)

                verify(exactly = 1) { searchTagUseCase(parameter = SearchTagUseCase.Parameter(query = query, sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 1) { searchPlaceUseCase(parameter = SearchPlaceUseCase.Parameter(query = query, sort = ListSort.RECENTLY_UPDATED)) }
                verify(exactly = 1) { searchWebUseCase(parameter = SearchWebUseCase.Parameter(query = query, sort = ListSort.RECENTLY_UPDATED)) }
            }
        }

        test("검색이 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val searchTagUseCase = mockk<SearchTagUseCase>()
                val action = searchAppActionOf(searchTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.tag(query = Arb.string().next(), sort = "default", cursor = null) }.message shouldBe
                    "'sort' must be one of title, recently_updated."
                verify(exactly = 0) { searchTagUseCase(parameter = any()) }
            }
        }

        test("검색 결과는 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val webList = Arb.list(Arb.web(), 101..150).next()
                val searchWebUseCase = mockk<SearchWebUseCase>()
                every { searchWebUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(webList))) }
                val action = searchAppActionOf(searchWebUseCase)

                val firstPage = action.web(query = Arb.string().next(), sort = null, cursor = null)
                val secondPage = action.web(query = Arb.string().next(), sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = webList.take(100).map { web -> web.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = webList.drop(100).map { web -> web.expected() }, nextCursor = null)
            }
        }

        test("다음 페이지 위치가 아닌 값은 검색하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val searchPlaceUseCase = mockk<SearchPlaceUseCase>()
                val action = searchAppActionOf(searchPlaceUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.place(query = Arb.string().next(), sort = null, cursor = "-1") }.message shouldBe
                    "'cursor' must be a nextCursor value from a previous page."
            }
        }

        test("검색 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val searchMemoUseCase = mockk<SearchMemoUseCase>()
                every { searchMemoUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = searchAppActionOf(searchMemoUseCase)

                shouldThrow<AppActionException.Failure> { action.memo(query = Arb.string().next(), sort = null, cursor = null) }.message shouldBe "IllegalStateException: $message"
            }
        }
    })

private const val RECENTLY_UPDATED = "recently_updated"
private const val RGB = 0xFFFFFFL

private fun Long.expectedHex(): String = "#%06X".format(this and RGB)

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

private fun Place.expected(): AppActionPlace =
    AppActionPlace(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.expectedHex(),
        address = detail.address,
        latitude = detail.coordinate.latitude.takeIf(Double::isFinite),
        longitude = detail.coordinate.longitude.takeIf(Double::isFinite),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun Web.expected(): AppActionWeb =
    AppActionWeb(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        url = detail.url,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
