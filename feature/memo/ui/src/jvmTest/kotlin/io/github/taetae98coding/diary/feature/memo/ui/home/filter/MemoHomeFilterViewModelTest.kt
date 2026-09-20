@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoDateExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPlaceExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoTagExistenceFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectMemoFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoHomeFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-HOME-FEATURE-020 조회한 태그 페이지와 선택 집합을 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(title = "Alpha"), tag(title = "Bravo"))
                val selectedTagList = listOf(tagList.last())
                val viewModel =
                    viewModel(
                        pageTagUseCase = pageTagUseCase(flow = flowOf(Result.success(tagList))),
                        getMemoFilterUseCase =
                            filterUseCase(
                                flow = flowOf(Result.success(selectedTagList)),
                            ),
                    )

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoHomeFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe
                        MemoHomeFilterUiState(selectedTagIdSet = selectedTagList.map { tag -> tag.id }.toSet())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("태그 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageTagUseCase = pageTagUseCase(flow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.tagPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("선택 태그 조회에 실패하면 빈 선택 집합을 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase =
                            filterUseCase(
                                flow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoHomeFilterUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MEMO-HOME-FEATURE-032 유지된 유무 필터 상태를 노출한다") {
            runTest(mainDispatcher) {
                val existence =
                    MemoExistenceFilter(
                        date = MemoFilterExistence.EXIST,
                        tag = MemoFilterExistence.NOT_EXIST,
                        place = MemoFilterExistence.ALL,
                    )
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(flow = flowOf(Result.success(emptyList()))),
                        getMemoExistenceFilterUseCase = existenceFilterUseCase(flow = flowOf(Result.success(existence))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoHomeFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe MemoHomeFilterUiState(existence = existence)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("유무 필터 조회에 실패하면 세 축을 전체로 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(flow = flowOf(Result.success(emptyList()))),
                        getMemoExistenceFilterUseCase =
                            existenceFilterUseCase(
                                flow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoHomeFilterUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("날짜 축 변경을 날짜 축 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val setMemoDateExistenceFilterUseCase = mockk<SetMemoDateExistenceFilterUseCase>()
                coEvery { setMemoDateExistenceFilterUseCase(parameter = MemoFilterExistence.EXIST) } returns Result.success(Unit)
                val viewModel = viewModel(setMemoDateExistenceFilterUseCase = setMemoDateExistenceFilterUseCase)

                viewModel.setDateExistence(existence = MemoFilterExistence.EXIST)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    setMemoDateExistenceFilterUseCase(parameter = MemoFilterExistence.EXIST)
                }
            }
        }

        test("태그 축 변경을 태그 축 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val setMemoTagExistenceFilterUseCase = mockk<SetMemoTagExistenceFilterUseCase>()
                coEvery { setMemoTagExistenceFilterUseCase(parameter = MemoFilterExistence.NOT_EXIST) } returns Result.success(Unit)
                val viewModel = viewModel(setMemoTagExistenceFilterUseCase = setMemoTagExistenceFilterUseCase)

                viewModel.setTagExistence(existence = MemoFilterExistence.NOT_EXIST)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    setMemoTagExistenceFilterUseCase(parameter = MemoFilterExistence.NOT_EXIST)
                }
            }
        }

        test("장소 축 변경을 장소 축 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val setMemoPlaceExistenceFilterUseCase = mockk<SetMemoPlaceExistenceFilterUseCase>()
                coEvery { setMemoPlaceExistenceFilterUseCase(parameter = MemoFilterExistence.ALL) } returns Result.success(Unit)
                val viewModel = viewModel(setMemoPlaceExistenceFilterUseCase = setMemoPlaceExistenceFilterUseCase)

                viewModel.setPlaceExistence(existence = MemoFilterExistence.ALL)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    setMemoPlaceExistenceFilterUseCase(parameter = MemoFilterExistence.ALL)
                }
            }
        }

        test("태그 선택을 선택 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val selectMemoFilterTagUseCase = mockk<SelectMemoFilterTagUseCase>()
                coEvery { selectMemoFilterTagUseCase(parameter = tagId) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        selectMemoFilterTagUseCase = selectMemoFilterTagUseCase,
                    )

                viewModel.selectTag(id = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    selectMemoFilterTagUseCase(parameter = tagId)
                }
            }
        }

        test("태그 선택 해제를 선택 해제 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val unselectMemoFilterTagUseCase = mockk<UnselectMemoFilterTagUseCase>()
                coEvery { unselectMemoFilterTagUseCase(parameter = tagId) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        unselectMemoFilterTagUseCase = unselectMemoFilterTagUseCase,
                    )

                viewModel.unselectTag(id = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    unselectMemoFilterTagUseCase(parameter = tagId)
                }
            }
        }

        test("TC-MEMO-HOME-FEATURE-056 태그 선택 전체 해제를 전체 해제 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val unselectAllMemoFilterTagUseCase = mockk<UnselectAllMemoFilterTagUseCase>()
                coEvery { unselectAllMemoFilterTagUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        unselectAllMemoFilterTagUseCase = unselectAllMemoFilterTagUseCase,
                    )

                viewModel.unselectAllTag()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    unselectAllMemoFilterTagUseCase(parameter = Unit)
                }
            }
        }
        test("TC-MEMO-HOME-FEATURE-058 선택 전체 해제는 유무 필터의 축 상태를 바꾸지 않는다") {
            runTest(mainDispatcher) {
                val existence =
                    MemoExistenceFilter(
                        date = MemoFilterExistence.EXIST,
                        tag = MemoFilterExistence.ALL,
                        place = MemoFilterExistence.ALL,
                    )
                val unselectAllMemoFilterTagUseCase = mockk<UnselectAllMemoFilterTagUseCase>()
                coEvery { unselectAllMemoFilterTagUseCase(parameter = Unit) } returns Result.success(Unit)
                val setMemoDateExistenceFilterUseCase = mockk<SetMemoDateExistenceFilterUseCase>()
                val setMemoTagExistenceFilterUseCase = mockk<SetMemoTagExistenceFilterUseCase>()
                val setMemoPlaceExistenceFilterUseCase = mockk<SetMemoPlaceExistenceFilterUseCase>()
                val viewModel =
                    viewModel(
                        getMemoFilterUseCase = filterUseCase(flow = flowOf(Result.success(emptyList()))),
                        getMemoExistenceFilterUseCase = existenceFilterUseCase(flow = flowOf(Result.success(existence))),
                        unselectAllMemoFilterTagUseCase = unselectAllMemoFilterTagUseCase,
                        setMemoDateExistenceFilterUseCase = setMemoDateExistenceFilterUseCase,
                        setMemoTagExistenceFilterUseCase = setMemoTagExistenceFilterUseCase,
                        setMemoPlaceExistenceFilterUseCase = setMemoPlaceExistenceFilterUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem()
                    advanceUntilIdle()
                    awaitItem() shouldBe MemoHomeFilterUiState(existence = existence)

                    viewModel.unselectAllTag()
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { unselectAllMemoFilterTagUseCase(parameter = Unit) }
                coVerify(exactly = 0) { setMemoDateExistenceFilterUseCase(parameter = any()) }
                coVerify(exactly = 0) { setMemoTagExistenceFilterUseCase(parameter = any()) }
                coVerify(exactly = 0) { setMemoPlaceExistenceFilterUseCase(parameter = any()) }
            }
        }
    }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageTagUseCase: PageTagUseCase = pageTagUseCase(flow = emptyFlow()),
            getMemoFilterUseCase: GetMemoFilterUseCase = filterUseCase(flow = emptyFlow()),
            getMemoExistenceFilterUseCase: GetMemoExistenceFilterUseCase = existenceFilterUseCase(),
            selectMemoFilterTagUseCase: SelectMemoFilterTagUseCase = mockk(),
            unselectMemoFilterTagUseCase: UnselectMemoFilterTagUseCase = mockk(),
            unselectAllMemoFilterTagUseCase: UnselectAllMemoFilterTagUseCase = mockk(),
            setMemoDateExistenceFilterUseCase: SetMemoDateExistenceFilterUseCase = mockk(),
            setMemoTagExistenceFilterUseCase: SetMemoTagExistenceFilterUseCase = mockk(),
            setMemoPlaceExistenceFilterUseCase: SetMemoPlaceExistenceFilterUseCase = mockk(),
        ): MemoHomeFilterViewModel =
            MemoHomeFilterViewModel(
                pageTagUseCase = pageTagUseCase,
                getMemoFilterUseCase = getMemoFilterUseCase,
                getMemoExistenceFilterUseCase = getMemoExistenceFilterUseCase,
                selectMemoFilterTagUseCase = selectMemoFilterTagUseCase,
                unselectMemoFilterTagUseCase = unselectMemoFilterTagUseCase,
                unselectAllMemoFilterTagUseCase = unselectAllMemoFilterTagUseCase,
                setMemoDateExistenceFilterUseCase = setMemoDateExistenceFilterUseCase,
                setMemoTagExistenceFilterUseCase = setMemoTagExistenceFilterUseCase,
                setMemoPlaceExistenceFilterUseCase = setMemoPlaceExistenceFilterUseCase,
            )

        private fun existenceFilterUseCase(flow: Flow<Result<MemoExistenceFilter>> = flowOf(Result.success(MemoExistenceFilter()))): GetMemoExistenceFilterUseCase {
            val getMemoExistenceFilterUseCase = mockk<GetMemoExistenceFilterUseCase>()
            every { getMemoExistenceFilterUseCase(parameter = Unit) } returns flow

            return getMemoExistenceFilterUseCase
        }

        private fun pageTagUseCase(flow: Flow<Result<List<Tag>>>): PageTagUseCase {
            val pageTagUseCase = mockk<PageTagUseCase>()
            every { pageTagUseCase(parameter = "") } returns
                flow.map { result -> result.map { tagList -> PagingData.from(tagList) } }

            return pageTagUseCase
        }

        private fun filterUseCase(flow: Flow<Result<List<Tag>>>): GetMemoFilterUseCase {
            val getMemoFilterUseCase = mockk<GetMemoFilterUseCase>()
            every { getMemoFilterUseCase(parameter = Unit) } returns flow

            return getMemoFilterUseCase
        }

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(title = title))
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
