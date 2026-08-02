@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.GetTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant

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
                val tagList = listOf(tag(title = "Alpha"), tag(title = "Bravo"))
                val viewModel = viewModel(pageTagHomeUseCase = pageTagHomeUseCase(tagListFlow = flowOf(Result.success(tagList))))

                flowOf(viewModel.tagPagingData.first()).asSnapshot() shouldBe tagList
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

        test("TC-TAG-HOME-FEATURE-030 필터를 켜 두면 필터가 적용된 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(flowOf(Result.success(true))))

                viewModel.filterUiState.test {
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState()
                    awaitItem() shouldBe TagHomeScaffoldFilterUiState(isApplied = true)
                }
            }
        }

        test("필터를 끄면 필터가 적용되지 않은 상태를 노출한다") {
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
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase(emptyFlow()),
            pageTagHomeUseCase: PageTagHomeUseCase = pageTagHomeUseCase(tagListFlow = emptyFlow()),
        ): TagHomeViewModel =
            TagHomeViewModel(
                getTopLevelTagFilterUseCase = getTopLevelTagFilterUseCase,
                pageTagHomeUseCase = pageTagHomeUseCase,
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
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
