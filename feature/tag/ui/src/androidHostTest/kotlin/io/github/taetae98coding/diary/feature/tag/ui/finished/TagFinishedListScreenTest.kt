package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-006 완료된 태그가 하나도 없어도 화면을 사용할 수 있다`() {
        val viewModel = mockk<TagFinishedListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(emptyList()))
        every { viewModel.effect } returns emptyFlow()

        setTagFinishedListScreen(viewModel)

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-011 태그를 선택하면 그 태그의 상세로 이동한다`() {
        val environment = screenTestEnvironment()
        val navigatedIdList = mutableListOf<Uuid>()
        setTagFinishedListScreen(
            viewModel = environment.viewModel,
            navigateToDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(environment.tag.id)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-012 뒤로가기 버튼을 선택하면 이전 화면으로 돌아간다`() {
        val environment = screenTestEnvironment()
        var navigateUpCount = 0
        setTagFinishedListScreen(
            viewModel = environment.viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    private fun setTagFinishedListScreen(
        viewModel: TagFinishedListViewModel,
        navigateUp: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagFinishedListScreen(
                    navigateUp = navigateUp,
                    navigateToDetail = navigateToDetail,
                    tagViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
    }

    public companion object {
        private const val TAG_TITLE = "TagFinishedListScreenTitle"
        private const val DEFAULT_TITLE = "Finished Tags"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun screenTestEnvironment(): ScreenTestEnvironment {
            val tag =
                fixtureMonkey
                    .giveMeKotlinBuilder<Tag>()
                    .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = TAG_TITLE))
                    .setExp(Tag::isFinished, true)
                    .setExp(Tag::isDeleted, false)
                    .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()
            val viewModel = mockk<TagFinishedListViewModel>()
            every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)

            every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(listOf(tag)))

            every { viewModel.effect } returns emptyFlow()

            return ScreenTestEnvironment(
                tag = tag,
                viewModel = viewModel,
            )
        }
    }
}

private class ScreenTestEnvironment(
    val tag: Tag,
    val viewModel: TagFinishedListViewModel,
)
