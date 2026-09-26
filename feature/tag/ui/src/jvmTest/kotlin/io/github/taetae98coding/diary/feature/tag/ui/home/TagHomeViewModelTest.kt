@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class TagHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-HOME-DATA-005 조회한 태그 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(title = fixtureMonkey.giveMeOne()), tag(title = fixtureMonkey.giveMeOne()))
                val viewModel = viewModel(pageTagHomeUseCase = pageTagHomeUseCase(tagListFlow = flowOf(Result.success(tagList))))

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList
            }
        }

        test("TC-TAG-HOME-FEATURE-038 태그 페이지 조회가 성공한 뒤 실패하면 마지막으로 불러온 태그를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(title = fixtureMonkey.giveMeOne()), tag(title = fixtureMonkey.giveMeOne()))
                val viewModel =
                    viewModel(
                        pageTagHomeUseCase =
                            pageTagHomeUseCase(
                                tagListFlow = flowOf(Result.success(tagList), Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.tagPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList shouldBe tagList
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("태그 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageTagHomeUseCase = pageTagHomeUseCase(tagListFlow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.tagPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-TAG-HOME-FEATURE-037 필터를 켜 두면 필터가 적용된 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.success(true))))

                viewModel.filterUiState.test {
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState()
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState(isApplied = true)
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-037 필터를 끄면 필터가 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val isTopLevelOnly = MutableStateFlow(true)
                val viewModel =
                    viewModel(
                        getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(isTopLevelOnly.map { value -> Result.success(value) }),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState()
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState(isApplied = true)

                    isTopLevelOnly.value = false

                    awaitItem() shouldBe TagHomeScaffoldFilterUiState(isApplied = false)
                }
            }
        }

        test("필터 선택 조회에 실패하면 필터가 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.failure(IllegalStateException()))),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState()
                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-039 완료에 성공하면 그 태그의 완료를 요청하고 완료 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(finishTagUseCase = finishTagUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = id)

                    awaitItem() shouldBe TagListEffect.Finished(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-HOME-FEATURE-040 삭제에 성공하면 그 태그의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(deleteTagUseCase = deleteTagUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe TagListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-HOME-FEATURE-046 완료가 저장되지 못하면 완료 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(finishTagUseCase = finishTagUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-046 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteTagUseCase = mockk<DeleteTagUseCase>()
                coEvery { deleteTagUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(deleteTagUseCase = deleteTagUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-HOME-FEATURE-041 완료를 실행 취소하면 그 태그의 다시 시작을 한 번 요청하고 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restartTagUseCase = restartTagUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-HOME-FEATURE-042 삭제를 실행 취소하면 그 태그의 삭제 되돌리기를 한 번 요청하고 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreTagUseCase = mockk<RestoreTagUseCase>()
                coEvery { restoreTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restoreTagUseCase = restoreTagUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-HOME-DOMAIN-018 실행 취소를 저장하지 못하면 별도 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreTagUseCase = mockk<RestoreTagUseCase>()
                coEvery { restoreTagUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(restoreTagUseCase = restoreTagUseCase)

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreTagUseCase(parameter = id) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(emptyFlow()),
            pageTagHomeUseCase: PageTagHomeUseCase = pageTagHomeUseCase(tagListFlow = emptyFlow()),
            finishTagUseCase: FinishTagUseCase = mockk(),
            restartTagUseCase: RestartTagUseCase = mockk(),
            deleteTagUseCase: DeleteTagUseCase = mockk(),
            restoreTagUseCase: RestoreTagUseCase = mockk(),
        ): TagHomeViewModel =
            TagHomeViewModel(
                getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase,
                pageTagHomeUseCase = pageTagHomeUseCase,
                finishTagUseCase = finishTagUseCase,
                restartTagUseCase = restartTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
                restoreTagUseCase = restoreTagUseCase,
            )

        private fun getTopLevelTagFilterUseCase(flow: Flow<Result<Boolean>>): GetTopLevelTagFilterUseCase {
            val getTopLevelTagFilterUseCase = mockk<GetTopLevelTagFilterUseCase>()
            every { getTopLevelTagFilterUseCase(parameter = Unit) } returns flow

            return getTopLevelTagFilterUseCase
        }

        private fun pageTagHomeUseCase(tagListFlow: Flow<Result<List<Tag>>>): PageTagHomeUseCase {
            val pageTagHomeUseCase = mockk<PageTagHomeUseCase>()
            every { pageTagHomeUseCase(parameter = ListSort.TITLE) } returns
                tagListFlow.map { result -> result.map { tagList -> PagingData.from(tagList) } }

            return pageTagHomeUseCase
        }

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(title = title))
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
