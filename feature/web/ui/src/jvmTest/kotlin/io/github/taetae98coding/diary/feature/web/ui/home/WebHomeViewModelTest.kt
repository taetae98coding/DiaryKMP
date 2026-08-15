@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.usecase.PageWebUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class WebHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-WEB-HOME-FEATURE-001 조회한 웹 항목 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val webList = listOf(web(title = "Alpha"), web(title = "Bravo"))
                val viewModel = viewModel(pageWebUseCase = pageWebUseCase(webListFlow = flowOf(Result.success(webList))))

                flowOf(viewModel.webPagingData.first()).asSnapshot() shouldBe webList
            }
        }

        test("TC-WEB-HOME-DATA-003 웹 항목 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageWebUseCase = pageWebUseCase(webListFlow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.webPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-WEB-HOME-FEATURE-004 저장된 웹 항목이 바뀌면 바뀐 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val beforeWebList = listOf(web(title = "Alpha"))
                val afterWebList = listOf(web(title = "Alpha"), web(title = "Bravo"))
                val webListFlow = MutableStateFlow(Result.success(beforeWebList))
                val viewModel = viewModel(pageWebUseCase = pageWebUseCase(webListFlow = webListFlow))

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe beforeWebList

                    webListFlow.value = Result.success(afterWebList)

                    flowOf(awaitItem()).asSnapshot() shouldBe afterWebList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-WEB-HOME-DATA-006 처음 정렬은 제목순이다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageWebUseCase = pageWebUseCase(webListFlow = flowOf(Result.success(emptyList()))))

                viewModel.sort.value shouldBe ListSort.TITLE
            }
        }

        test("TC-WEB-HOME-DATA-005 정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val titleWebList = listOf(web(title = "Alpha"), web(title = "Bravo"))
                val recentlyUpdatedWebList = listOf(web(title = "Bravo"), web(title = "Alpha"))
                val pageWebUseCase = mockk<PageWebUseCase>()
                every { pageWebUseCase(parameter = ListSort.TITLE) } returns flowOf(Result.success(PagingData.from(titleWebList)))
                every { pageWebUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns
                    flowOf(Result.success(PagingData.from(recentlyUpdatedWebList)))
                val viewModel = viewModel(pageWebUseCase = pageWebUseCase)

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe titleWebList

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)

                    flowOf(awaitItem()).asSnapshot() shouldBe recentlyUpdatedWebList
                    viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(pageWebUseCase: PageWebUseCase): WebHomeViewModel = WebHomeViewModel(pageWebUseCase = pageWebUseCase)

        private fun pageWebUseCase(webListFlow: Flow<Result<List<Web>>>): PageWebUseCase {
            val pageWebUseCase = mockk<PageWebUseCase>()
            every { pageWebUseCase(parameter = ListSort.TITLE) } returns
                webListFlow.map { result -> result.map { webList -> PagingData.from(webList) } }

            return pageWebUseCase
        }

        // FixtureMonkey가 Instant를 생성하지 못하므로 웹 항목은 직접 만든다.
        private fun web(title: String): Web {
            val detail =
                fixtureMonkey
                    .giveMeKotlinBuilder<WebDetail>()
                    .setExp(WebDetail::title, title)
                    .sample()

            return Web(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
        }
    }
}
