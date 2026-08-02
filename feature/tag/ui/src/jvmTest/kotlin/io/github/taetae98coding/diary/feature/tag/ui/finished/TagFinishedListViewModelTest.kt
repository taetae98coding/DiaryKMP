@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant

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
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(pageFinishedTagUseCase: PageFinishedTagUseCase = pageFinishedTagUseCase(tagListFlow = emptyFlow())): TagFinishedListViewModel = TagFinishedListViewModel(pageFinishedTagUseCase = pageFinishedTagUseCase)

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
