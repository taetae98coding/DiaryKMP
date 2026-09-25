@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase)

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
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase)

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
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase)

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
        test("TC-TAG-DETAIL-WEB-FEATURE-004 조회가 실패하면 목록을 전달하지 않는다") {
            runTest(mainDispatcher) {
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
                val pageTagWebUseCase = mockk<PageTagWebUseCase>()
                every { pageTagWebUseCase(parameter = PageTagWebUseCase.Parameter(tagId = tagId, scope = TagScope.SELF, sort = ListSort.TITLE)) } returns flowOf(Result.failure(throwable))
                val viewModel = TagDetailWebViewModel(tagId = tagId, pageTagWebUseCase = pageTagWebUseCase)

                viewModel.webPagingData.test {
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
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
                .setExp(Web::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Web::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
