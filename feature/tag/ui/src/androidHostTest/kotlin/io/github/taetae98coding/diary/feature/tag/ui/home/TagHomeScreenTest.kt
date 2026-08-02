package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-013 완료된 태그 버튼을 선택하면 완료된 태그 목록으로 이동한다`() {
        var navigateToFinishedListCount = 0
        setTagHomeScreen(
            viewModel = screenTestViewModel(),
            navigateToFinishedList = { navigateToFinishedListCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).performClick()

        navigateToFinishedListCount shouldBe 1
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-027 필터 버튼을 선택하면 필터를 연다`() {
        var navigateToFilterCount = 0
        setTagHomeScreen(
            viewModel = screenTestViewModel(),
            navigateToFilter = { navigateToFilterCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).performClick()

        navigateToFilterCount shouldBe 1
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-024 검색 버튼을 선택하면 검색 화면으로 이동한다`() {
        var navigateToSearchCount = 0
        setTagHomeScreen(
            viewModel = screenTestViewModel(),
            navigateToSearch = { navigateToSearchCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()

        navigateToSearchCount shouldBe 1
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-001 태그 추가 버튼을 누르면 태그 추가 화면 전환 행동을 한 번 전달한다`() {
        var navigateToAddCount = 0
        setTagHomeScreen(
            viewModel = screenTestViewModel(),
            navigateToAdd = { navigateToAddCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `Cmd A 단축키를 입력하면 태그 추가 화면 전환 행동을 한 번 전달한다`() {
        var navigateToAddCount = 0
        setTagHomeScreen(
            viewModel = screenTestViewModel(),
            navigateToAdd = { navigateToAddCount += 1 },
        )

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }

        navigateToAddCount shouldBe 1
    }

    private fun setTagHomeScreen(
        viewModel: TagHomeViewModel,
        navigateToAdd: () -> Unit = {},
        navigateToFilter: () -> Unit = {},
        navigateToFinishedList: () -> Unit = {},
        navigateToSearch: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagHomeScreen(
                    navigateToAdd = navigateToAdd,
                    navigateToDetail = {},
                    navigateToFilter = navigateToFilter,
                    navigateToFinishedList = navigateToFinishedList,
                    navigateToSearch = navigateToSearch,
                    tagViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                    componentVisibleProvider = { TagHomeScaffoldComponentVisible() },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished tags"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
        private const val TAG_TITLE = "TagHomeScreenTitle"

        private fun screenTestViewModel(): TagHomeViewModel {
            val tag =
                fixtureMonkey
                    .giveMeKotlinBuilder<Tag>()
                    .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = TAG_TITLE))
                    .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .sample()
            val viewModel = mockk<TagHomeViewModel>()
            every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)

            every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(listOf(tag)))
            every { viewModel.filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState())

            return viewModel
        }
    }
}
