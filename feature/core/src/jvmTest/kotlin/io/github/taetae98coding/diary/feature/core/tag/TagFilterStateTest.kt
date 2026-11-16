package io.github.taetae98coding.diary.feature.core.tag

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class TagFilterStateTest : FunSpec() {
    init {
        test("검색어 없이 조회한 태그 페이지를 순서대로 노출한다") {
            runTest {
                val tagList = List(3) { Arb.tag().next() }
                val useCases = UseCases(pageFlow = flowOf(Result.success(PagingData.from(tagList))))
                val state = state(useCases = useCases)

                flowOf(state.tagPagingData.first()).asSnapshot() shouldBe tagList

                verify(exactly = 1) { useCases.page(parameter = "") }
            }
        }

        test("태그 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest {
                val useCases = UseCases(pageFlow = flowOf(Result.failure(IllegalStateException())))
                val state = state(useCases = useCases)

                flowOf(state.tagPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("유지된 선택을 태그 ID 집합으로 노출한다") {
            runTest {
                val selectedTagList = List(2) { Arb.tag().next() }
                val useCases = UseCases(filterFlow = flowOf(Result.success(selectedTagList)))
                val state = state(useCases = useCases)

                state.selectedTagIdSet.first() shouldBe selectedTagList.map { tag -> tag.id }.toSet()
            }
        }

        test("선택 조회에 실패하면 빈 선택 집합을 노출한다") {
            runTest {
                val useCases = UseCases(filterFlow = flowOf(Result.failure(IllegalStateException())))
                val state = state(useCases = useCases)

                state.selectedTagIdSet.first().shouldBeEmpty()
            }
        }

        test("선택·선택 해제·전체 해제를 각 UseCase에 한 번씩 전달한다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                useCases.succeedAll()
                val state = state(useCases = useCases)

                state.select(id = id)
                state.unselect(id = id)
                state.unselectAll()

                coVerify(exactly = 1) { useCases.select(parameter = id) }
                coVerify(exactly = 1) { useCases.unselect(parameter = id) }
                coVerify(exactly = 1) { useCases.unselectAll() }
            }
        }

        test("저장에 실패해도 예외를 던지지 않고 다음 요청을 다시 받는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                coEvery { useCases.select(parameter = any()) } returns Result.failure(IllegalStateException())
                coEvery { useCases.unselectAll() } returns Result.failure(IllegalStateException())
                val state = state(useCases = useCases)

                repeat(2) {
                    state.select(id = id)
                    state.unselectAll()
                }

                coVerify(exactly = 2) { useCases.select(parameter = id) }
                coVerify(exactly = 2) { useCases.unselectAll() }
            }
        }

        test("같은 태그의 같은 동작이 진행 중이면 다시 실행하지 않고 끝나면 다시 받는다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Unit>>()
                coEvery { useCases.select(parameter = id) } coAnswers { pending.await() }
                coEvery { useCases.unselect(parameter = id) } coAnswers { pending.await() }
                coEvery { useCases.unselectAll() } coAnswers { pending.await() }
                val state = state(useCases = useCases)

                launch { state.select(id = id) }
                launch { state.unselect(id = id) }
                launch { state.unselectAll() }
                runCurrent()
                state.select(id = id)
                state.unselect(id = id)
                state.unselectAll()

                coVerify(exactly = 1) { useCases.select(parameter = id) }
                coVerify(exactly = 1) { useCases.unselect(parameter = id) }
                coVerify(exactly = 1) { useCases.unselectAll() }

                pending.complete(Result.success(Unit))
                runCurrent()
                state.select(id = id)
                state.unselect(id = id)
                state.unselectAll()

                coVerify(exactly = 2) { useCases.select(parameter = id) }
                coVerify(exactly = 2) { useCases.unselect(parameter = id) }
                coVerify(exactly = 2) { useCases.unselectAll() }
            }
        }

        test("다른 태그의 선택은 진행 중인 선택과 함께 실행한다") {
            runTest {
                val id = Arb.kotlinUuid().next()
                val otherId = Arb.kotlinUuid().next()
                val useCases = UseCases()
                val pending = CompletableDeferred<Result<Unit>>()
                coEvery { useCases.select(parameter = any()) } coAnswers { pending.await() }
                val state = state(useCases = useCases)

                launch { state.select(id = id) }
                launch { state.select(id = otherId) }
                runCurrent()

                coVerify(exactly = 1) { useCases.select(parameter = id) }
                coVerify(exactly = 1) { useCases.select(parameter = otherId) }

                pending.complete(Result.success(Unit))
                runCurrent()
            }
        }
    }

    private class UseCases(
        pageFlow: Flow<Result<PagingData<Tag>>> = flowOf(Result.success(PagingData.empty())),
        filterFlow: Flow<Result<List<Tag>>> = flowOf(Result.success(emptyList())),
    ) {
        val page = mockk<PageTagUseCase>()
        val filter = mockk<GetMemoFilterUseCase>()
        val select = mockk<SelectMemoFilterTagUseCase>()
        val unselect = mockk<UnselectMemoFilterTagUseCase>()
        val unselectAll = mockk<UnselectAllMemoFilterTagUseCase>()

        init {
            every { page(parameter = "") } returns pageFlow
            every { filter() } returns filterFlow
        }

        fun succeedAll() {
            coEvery { select(parameter = any()) } returns Result.success(Unit)
            coEvery { unselect(parameter = any()) } returns Result.success(Unit)
            coEvery { unselectAll() } returns Result.success(Unit)
        }
    }

    private fun state(useCases: UseCases): TagFilterState =
        TagFilterState(
            pageTagUseCase = useCases.page,
            getFilterUseCase = useCases.filter,
            selectTagUseCase = useCases.select,
            unselectTagUseCase = useCases.unselect,
            unselectAllTagUseCase = useCases.unselectAll,
        )
}
