package io.github.taetae98coding.diary.work.appaction.core

import androidx.paging.PagingData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.test.runTest

class AppActionPageTest :
    FunSpec({
        test("한 페이지에 최대 100개를 담고 다음 페이지 위치를 알린다") {
            runTest {
                val itemList = List(Arb.int(101..250).next()) { index -> index }
                val pagingData = PagingData.from(itemList)

                val firstPage = pagingData.readPage(offset = null.toPageOffset(), size = APP_ACTION_PAGE_SIZE)
                val secondPage = PagingData.from(itemList).readPage(offset = firstPage.nextCursor.toPageOffset(), size = APP_ACTION_PAGE_SIZE)

                firstPage shouldBe AppActionPage(items = itemList.take(100), nextCursor = "100")
                firstPage.hasNextPage shouldBe true
                secondPage.items shouldBe itemList.drop(100).take(100)
            }
        }

        test("마지막 페이지에는 다음 페이지 위치가 없다") {
            runTest {
                val itemList = List(Arb.int(0..100).next()) { index -> index }

                val page = PagingData.from(itemList).readPage(offset = 0, size = APP_ACTION_PAGE_SIZE)

                page shouldBe AppActionPage(items = itemList, nextCursor = null)
                page.hasNextPage shouldBe false
            }
        }

        test("이전 페이지가 알린 위치가 아니면 실패를 알린다") {
            listOf("-1", "abc", "1.5").forEach { cursor ->
                shouldThrow<AppActionException.InvalidArgument> { cursor.toPageOffset() }.message shouldBe
                    "'cursor' must be a nextCursor value from a previous page."
            }
        }

        test("페이지 번호를 같은 크기의 페이지 위치로 바꾼다") {
            val pageNumber = Arb.int(2..100).next()

            appActionPageCursor(pageNumber = 1) shouldBe null
            appActionPageCursor(pageNumber = pageNumber) shouldBe ((pageNumber - 1) * 100).toString()
            appActionPageCursor(pageNumber = pageNumber).toPageOffset() shouldBe (pageNumber - 1) * 100
        }

        test("1보다 작은 페이지 번호는 실패를 알린다") {
            val pageNumber = Arb.int(-100..0).next()

            shouldThrow<AppActionException.InvalidArgument> { appActionPageCursor(pageNumber = pageNumber) }.message shouldBe "'page' must be 1 or greater."
        }
    })
