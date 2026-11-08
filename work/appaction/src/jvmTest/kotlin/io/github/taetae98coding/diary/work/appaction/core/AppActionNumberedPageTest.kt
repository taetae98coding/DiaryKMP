package io.github.taetae98coding.diary.work.appaction.core

import androidx.paging.PagingData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.test.runTest

class AppActionNumberedPageTest :
    FunSpec({
        val itemList = List(250) { index -> index + 1 }
        val read: suspend (String?) -> AppActionPage<Int> = { cursor -> PagingData.from(itemList).readPage(offset = cursor.toPageOffset(), size = APP_ACTION_PAGE_SIZE) }

        test("TC-ca672f5e-d132-4423-ace6-06414739ab49 페이지 번호로 그 페이지의 항목과 다음 페이지 번호를 돌려준다") {
            runTest {
                readNumberedPage(pageNumber = 1, read = read) shouldBe AppActionNumberedPage(items = (1..100).toList(), nextPageNumber = 2)
                readNumberedPage(pageNumber = 2, read = read) shouldBe AppActionNumberedPage(items = (101..200).toList(), nextPageNumber = 3)
            }
        }

        test("TC-ca672f5e-d132-4423-ace6-06414739ab49 마지막 페이지에는 다음 페이지 번호가 없다") {
            runTest {
                readNumberedPage(pageNumber = 3, read = read) shouldBe AppActionNumberedPage(items = (201..250).toList(), nextPageNumber = null)
            }
        }

        test("TC-ca672f5e-d132-4423-ace6-06414739ab49 마지막 페이지 뒤의 번호는 빈 페이지를 돌려준다") {
            runTest {
                readNumberedPage(pageNumber = 4, read = read) shouldBe AppActionNumberedPage(items = emptyList(), nextPageNumber = null)
            }
        }

        test("TC-ca672f5e-d132-4423-ace6-06414739ab49 1보다 작은 페이지 번호는 목록을 읽지 않고 실패를 알린다") {
            runTest {
                listOf(0, Arb.int(-100..-1).next()).forEach { pageNumber ->
                    var isRead = false

                    shouldThrow<AppActionException.InvalidArgument> {
                        readNumberedPage(pageNumber = pageNumber) { cursor ->
                            isRead = true
                            read(cursor)
                        }
                    }.message shouldBe "'page' must be 1 or greater."
                    isRead shouldBe false
                }
            }
        }
    })
