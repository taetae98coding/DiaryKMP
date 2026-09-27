@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TagDetailWebViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-TAG-DETAIL-WEB-DATA-001 상세 대상 태그로 조회한 목록을 그대로 전달한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val itemList = List(2) { item() }
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.success(PagingData.from(itemList)))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase, deleteWebUseCase = mockk(), restoreWebUseCase = mockk())

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe itemList
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-TAG-DETAIL-WEB-DOMAIN-002 상세 대상 태그로만 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val otherTagId = fixtureMonkey.giveMeOne<Uuid>()
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList<Web>())))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase, deleteWebUseCase = mockk(), restoreWebUseCase = mockk())

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot().shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
                verify(exactly = 0) { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = otherTagId, scope = TagScope.SELF, sort = ListSort.TITLE)) }
            }
        }

        test("TC-TAG-DETAIL-DATA-005 표시 범위를 넓혔다 되돌리면 웹 목록을 각 범위 기준으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val selfWebList = List(2) { item() }
                val childWebList = selfWebList + List(2) { item() }
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns
                    flowOf(Result.success(PagingData.from(selfWebList)))
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.CHILD, sort = ListSort.TITLE)) } returns
                    flowOf(Result.success(PagingData.from(childWebList)))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase, deleteWebUseCase = mockk(), restoreWebUseCase = mockk())

                viewModel.webPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe selfWebList

                    viewModel.select(scope = TagScope.CHILD)
                    flowOf(awaitItem()).asSnapshot() shouldBe childWebList

                    viewModel.select(scope = TagScope.SELF)
                    flowOf(awaitItem()).asSnapshot() shouldBe selfWebList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
        test("TC-TAG-DETAIL-WEB-FEATURE-004 최초 조회가 실패하면 조회가 끝난 빈 목록을 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.failure(throwable))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase, deleteWebUseCase = mockk(), restoreWebUseCase = mockk())

                viewModel.webPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList.shouldBeEmpty()
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-004 조회가 성공한 뒤 실패하면 마지막으로 불러온 웹 항목을 그대로 노출한다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val webList = List(2) { item() }
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns
                    flowOf(Result.success(PagingData.from(webList)), Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase, deleteWebUseCase = mockk(), restoreWebUseCase = mockk())

                viewModel.webPagingData.test {
                    advanceUntilIdle()
                    val itemList = flowOf(awaitItem()).asSnapshot()
                    expectNoEvents()

                    itemList shouldBe webList
                }
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-021 삭제하면 태그 연결 해제가 아니라 그 웹 항목의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = webId) } returns Result.success(1)
                val viewModel =
                    TagDetailWebViewModel(
                        tagId = tagId,
                        pageTagWebUseCase = mockk(),
                        deleteWebUseCase = deleteWebUseCase,
                        restoreWebUseCase = mockk(),
                    )

                viewModel.effect.test {
                    viewModel.delete(id = webId)

                    awaitItem() shouldBe WebListEffect.Deleted(id = webId)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteWebUseCase(parameter = webId) }
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-025 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val deleteWebUseCase = mockk<DeleteWebUseCase>()
                coEvery { deleteWebUseCase(parameter = webId) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel =
                    TagDetailWebViewModel(
                        tagId = fixtureMonkey.giveMeOne<Uuid>(),
                        pageTagWebUseCase = mockk(),
                        deleteWebUseCase = deleteWebUseCase,
                        restoreWebUseCase = mockk(),
                    )

                viewModel.effect.test {
                    viewModel.delete(id = webId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-022 실행 취소하면 그 웹 항목의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val webId = fixtureMonkey.giveMeOne<Uuid>()
                val restoreWebUseCase = mockk<RestoreWebUseCase>()
                coEvery { restoreWebUseCase(parameter = webId) } returns Result.success(1)
                val viewModel =
                    TagDetailWebViewModel(
                        tagId = fixtureMonkey.giveMeOne<Uuid>(),
                        pageTagWebUseCase = mockk(),
                        deleteWebUseCase = mockk(),
                        restoreWebUseCase = restoreWebUseCase,
                    )

                viewModel.restore(id = webId)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreWebUseCase(parameter = webId) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun item(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::isDeleted, false)
                .setExp(Web::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Web::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
