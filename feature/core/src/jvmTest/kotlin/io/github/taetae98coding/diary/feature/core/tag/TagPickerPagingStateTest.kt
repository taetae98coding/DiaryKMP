package io.github.taetae98coding.diary.feature.core.tag

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class TagPickerPagingStateTest : FunSpec() {
    init {
        test("검색어는 입력 정지 대기 시간 뒤에 조회에 반영된다") {
            runTest {
                val query = Arb.nonBlankString().next()
                val allTagList = List(2) { Arb.tag().next() }
                val matchedTagList = listOf(allTagList.first())
                val pageTag = mockk<(String) -> Flow<Result<PagingData<Tag>>>>()
                every { pageTag("") } returns flowOf(Result.success(PagingData.from(allTagList)))
                every { pageTag(query) } returns flowOf(Result.success(PagingData.from(matchedTagList)))
                val state = TagPickerPagingState(pageTag = pageTag)

                state.tagPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allTagList

                    state.updateQuery(query = query)
                    advanceTimeBy(INPUT_IDLE_DELAY / 2)
                    expectNoEvents()

                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe matchedTagList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택할 태그 목록은 검색어와 관계없이 빈 검색어로 조회한다") {
            runTest {
                val query = Arb.nonBlankString().next()
                val tagList = List(2) { Arb.tag().next() }
                val pageTag = mockk<(String) -> Flow<Result<PagingData<Tag>>>>()
                every { pageTag(any()) } returns flowOf(Result.success(PagingData.from(tagList)))
                val state = TagPickerPagingState(pageTag = pageTag)

                state.updateQuery(query = query)
                advanceUntilIdle()
                val itemList = flowOf(state.selectableTagPagingData.first()).asSnapshot()

                itemList shouldBe tagList
                verify(exactly = 1) { pageTag("") }
                verify(exactly = 0) { pageTag(query) }
            }
        }

        test("조회가 실패하면 빈 목록을 내보낸다") {
            runTest {
                val pageTag = mockk<(String) -> Flow<Result<PagingData<Tag>>>>()
                every { pageTag(any()) } returns flowOf(Result.failure(IllegalStateException()))
                val state = TagPickerPagingState(pageTag = pageTag)

                flowOf(state.tagPagingData.first()).asSnapshot().shouldBeEmpty()
                flowOf(state.selectableTagPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }
    }
}
