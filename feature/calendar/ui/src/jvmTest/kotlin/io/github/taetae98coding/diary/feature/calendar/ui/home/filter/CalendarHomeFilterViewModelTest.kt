@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectAllCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnselectCalendarFilterTagUseCase
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

class CalendarHomeFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-HOME-FEATURE-048 조회한 태그 페이지와 선택 집합을 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(title = "Alpha"), tag(title = "Bravo"))
                val selectedTagList = listOf(tagList.last())
                val viewModel =
                    viewModel(
                        pageTagUseCase = pageTagUseCase(flow = flowOf(Result.success(tagList))),
                        getCalendarFilterUseCase =
                            filterUseCase(
                                flow = flowOf(Result.success(selectedTagList)),
                            ),
                    )

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe
                        CalendarHomeFilterUiState(selectedTagIdSet = selectedTagList.map { tag -> tag.id }.toSet())
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
                        getCalendarFilterUseCase =
                            filterUseCase(
                                flow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeFilterUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("태그 선택을 선택 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val selectCalendarFilterTagUseCase = mockk<SelectCalendarFilterTagUseCase>()
                coEvery { selectCalendarFilterTagUseCase(parameter = tagId) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        selectCalendarFilterTagUseCase = selectCalendarFilterTagUseCase,
                    )

                viewModel.selectTag(id = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    selectCalendarFilterTagUseCase(parameter = tagId)
                }
            }
        }

        test("태그 선택 해제를 선택 해제 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val unselectCalendarFilterTagUseCase = mockk<UnselectCalendarFilterTagUseCase>()
                coEvery { unselectCalendarFilterTagUseCase(parameter = tagId) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        unselectCalendarFilterTagUseCase = unselectCalendarFilterTagUseCase,
                    )

                viewModel.unselectTag(id = tagId)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    unselectCalendarFilterTagUseCase(parameter = tagId)
                }
            }
        }

        test("TC-CALENDAR-HOME-FEATURE-082 태그 선택 전체 해제를 전체 해제 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val unselectAllCalendarFilterTagUseCase = mockk<UnselectAllCalendarFilterTagUseCase>()
                coEvery { unselectAllCalendarFilterTagUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    viewModel(
                        unselectAllCalendarFilterTagUseCase = unselectAllCalendarFilterTagUseCase,
                    )

                viewModel.unselectAllTag()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    unselectAllCalendarFilterTagUseCase(parameter = Unit)
                }
            }
        }
    }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageTagUseCase: PageTagUseCase = pageTagUseCase(flow = emptyFlow()),
            getCalendarFilterUseCase: GetCalendarFilterUseCase = filterUseCase(flow = emptyFlow()),
            selectCalendarFilterTagUseCase: SelectCalendarFilterTagUseCase = mockk(),
            unselectCalendarFilterTagUseCase: UnselectCalendarFilterTagUseCase = mockk(),
            unselectAllCalendarFilterTagUseCase: UnselectAllCalendarFilterTagUseCase = mockk(),
        ): CalendarHomeFilterViewModel =
            CalendarHomeFilterViewModel(
                pageTagUseCase = pageTagUseCase,
                getCalendarFilterUseCase = getCalendarFilterUseCase,
                selectCalendarFilterTagUseCase = selectCalendarFilterTagUseCase,
                unselectCalendarFilterTagUseCase = unselectCalendarFilterTagUseCase,
                unselectAllCalendarFilterTagUseCase = unselectAllCalendarFilterTagUseCase,
            )

        private fun pageTagUseCase(flow: Flow<Result<List<Tag>>>): PageTagUseCase {
            val pageTagUseCase = mockk<PageTagUseCase>()
            every { pageTagUseCase(parameter = "") } returns
                flow.map { result -> result.map { tagList -> PagingData.from(tagList) } }

            return pageTagUseCase
        }

        private fun filterUseCase(flow: Flow<Result<List<Tag>>>): GetCalendarFilterUseCase {
            val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
            every { getCalendarFilterUseCase(parameter = Unit) } returns flow

            return getCalendarFilterUseCase
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
