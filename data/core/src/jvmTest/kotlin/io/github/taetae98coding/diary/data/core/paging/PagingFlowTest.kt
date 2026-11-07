package io.github.taetae98coding.diary.data.core.paging

import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.test.runTest

class PagingFlowTest :
    FunSpec({
        test("읽은 항목을 순서대로 변환해 제공한다") {
            runTest {
                val localList = List(PAGE_SIZE * 5 + 1) { Arb.int().next() }

                val snapshot =
                    pagingFlow(
                        pagingSourceFactory = localList.asPagingSourceFactory(),
                        transform = { local -> local.toString() },
                    ).asSnapshot { appendScrollWhile { true } }

                snapshot shouldBe localList.map { local -> local.toString() }
            }
        }

        test("읽은 항목이 없으면 빈 목록을 제공한다") {
            val snapshot =
                pagingFlow(
                    pagingSourceFactory = emptyList<Int>().asPagingSourceFactory(),
                    transform = { local -> local.toString() },
                ).asSnapshot()

            snapshot shouldBe emptyList()
        }
    })
