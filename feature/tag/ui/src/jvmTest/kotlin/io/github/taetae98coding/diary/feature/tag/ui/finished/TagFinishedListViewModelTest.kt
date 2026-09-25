@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

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
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
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

class TagFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-001 TC-TAG-FINISHED-LIST-DATA-006 조회한 완료 태그 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagList = listOf(tag(title = "Alpha"), tag(title = "Bravo"))
                val viewModel = viewModel(pageFinishedTagUseCase = pageFinishedTagUseCase(tagListFlow = flowOf(Result.success(tagList))))

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList
            }
        }

        test("완료된 태그 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageFinishedTagUseCase = pageFinishedTagUseCase(tagListFlow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.tagPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-021 다시 시작에 성공하면 그 태그의 다시 시작을 요청하고 다시 시작 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(restartTagUseCase = restartTagUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = id)

                    awaitItem() shouldBe TagListEffect.Restarted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { restartTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-022 삭제에 성공하면 그 태그의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
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

        test("TC-TAG-FINISHED-LIST-FEATURE-028 다시 시작이 저장되지 못하면 다시 시작 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restartTagUseCase = mockk<RestartTagUseCase>()
                coEvery { restartTagUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(restartTagUseCase = restartTagUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-028 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
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

        test("TC-TAG-FINISHED-LIST-FEATURE-023 다시 시작을 실행 취소하면 그 태그의 완료를 한 번 요청하고 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val finishTagUseCase = mockk<FinishTagUseCase>()
                coEvery { finishTagUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(finishTagUseCase = finishTagUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { finishTagUseCase(parameter = id) }
            }
        }

        test("TC-TAG-FINISHED-LIST-FEATURE-024 삭제를 실행 취소하면 그 태그의 삭제 되돌리기를 한 번 요청하고 안내를 보내지 않는다") {
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
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageFinishedTagUseCase: PageFinishedTagUseCase = pageFinishedTagUseCase(tagListFlow = emptyFlow()),
            finishTagUseCase: FinishTagUseCase = mockk(),
            restartTagUseCase: RestartTagUseCase = mockk(),
            deleteTagUseCase: DeleteTagUseCase = mockk(),
            restoreTagUseCase: RestoreTagUseCase = mockk(),
        ): TagFinishedListViewModel =
            TagFinishedListViewModel(
                pageFinishedTagUseCase = pageFinishedTagUseCase,
                finishTagUseCase = finishTagUseCase,
                restartTagUseCase = restartTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
                restoreTagUseCase = restoreTagUseCase,
            )

        private fun pageFinishedTagUseCase(tagListFlow: Flow<Result<List<Tag>>>): PageFinishedTagUseCase {
            val pageFinishedTagUseCase = mockk<PageFinishedTagUseCase>()
            every { pageFinishedTagUseCase(parameter = ListSort.TITLE) } returns
                tagListFlow.map { result -> result.map { tagList -> PagingData.from(tagList) } }

            return pageFinishedTagUseCase
        }

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(title = title))
                .setExp(Tag::isFinished, true)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
