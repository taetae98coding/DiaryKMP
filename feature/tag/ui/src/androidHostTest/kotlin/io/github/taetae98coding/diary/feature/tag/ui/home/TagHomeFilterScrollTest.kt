package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomeFilterScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-DOMAIN-011 좁힌 목록이 놓이기 전에는 목록 위치를 유지한다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val isApplied = mutableStateOf(false)
        val tagPagingData = MutableStateFlow(tagPagingDataOf(allTagList))
        setTagHomeScaffold(isApplied = isApplied, tagPagingData = tagPagingData)

        scrollToLast(tagList = allTagList)
        switchFilter(isApplied = isApplied, value = true)

        composeRule.onNodeWithText(allTagList.first().title()).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-034 필터를 켜면 좁힌 결과가 놓일 때 목록을 처음부터 다시 본다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val isApplied = mutableStateOf(false)
        val tagListState = mutableStateOf(allTagList)
        setTagHomeScaffoldWithResult(isApplied = isApplied, tagListState = tagListState)
        scrollToLast(tagList = allTagList)

        composeRule.runOnIdle {
            isApplied.value = true
            tagListState.value = allTagList.take(NARROWED_TAG_COUNT)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(allTagList.first().title()).assertIsDisplayed()
        composeRule.onNodeWithText(allTagList[NARROWED_TAG_COUNT - 1].title()).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-034 필터를 끄면 전체 결과가 놓일 때 목록을 처음부터 다시 본다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val narrowedTagList = allTagList.take(NARROWED_TAG_COUNT)
        val isApplied = mutableStateOf(true)
        val tagListState = mutableStateOf(narrowedTagList)
        setTagHomeScaffoldWithResult(isApplied = isApplied, tagListState = tagListState)
        scrollToLast(tagList = narrowedTagList)

        composeRule.runOnIdle {
            isApplied.value = false
            tagListState.value = allTagList
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(allTagList.first().title()).assertIsDisplayed()
        composeRule.onNodeWithText(narrowedTagList.last().title()).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-012 필터를 바꾸지 않은 목록 갱신에서는 목록 위치를 유지한다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val isApplied = mutableStateOf(false)
        val tagListState = mutableStateOf(allTagList)
        setTagHomeScaffoldWithResult(isApplied = isApplied, tagListState = tagListState)
        scrollToLast(tagList = allTagList)

        composeRule.runOnIdle { tagListState.value = allTagList + tag(title = fixtureText(prefix = TITLE_PREFIX)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(allTagList.first().title()).assertDoesNotExist()
        composeRule.onNodeWithText(allTagList.last().title()).assertIsDisplayed()
    }

    private fun scrollToLast(tagList: List<Tag>) {
        composeRule.onNodeWithTag(TAG_HOME_LIST_TEST_TAG).performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(tagList.first().title()).assertDoesNotExist()
    }

    private fun switchFilter(
        isApplied: MutableState<Boolean>,
        value: Boolean,
    ) {
        composeRule.runOnIdle { isApplied.value = value }
        composeRule.waitForIdle()
    }

    private fun setTagHomeScaffold(
        isApplied: MutableState<Boolean>,
        tagPagingData: MutableStateFlow<PagingData<Tag>>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                // TagHomeScreen이 collectAsStateWithLifecycle로 읽어 넘기는 것과 같은 흐름을 만든다.
                val currentIsApplied by isApplied

                TagHomeScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                    onEvent = {},
                    filterUiStateProvider = { TagHomeScaffoldFilterUiState(isApplied = currentIsApplied) },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun setTagHomeScaffoldWithResult(
        isApplied: MutableState<Boolean>,
        tagListState: MutableState<List<Tag>>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                val currentIsApplied by isApplied
                val currentTagList by tagListState
                // 같은 목록에 이어서 넘긴 조회 결과는 화면 스레드의 공용 디스패처를 거쳐야 도착해 결과가 일정하지 않다.
                // 결과마다 새 목록을 만들어 목록이 처음 그릴 때 그 결과를 받게 한다.
                val tagPagingItems = remember(currentTagList) { MutableStateFlow(tagPagingDataOf(currentTagList)) }.collectAsLazyPagingItems()

                TagHomeScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingItems,
                    onEvent = {},
                    filterUiStateProvider = { TagHomeScaffoldFilterUiState(isApplied = currentIsApplied) },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val ALL_TAG_COUNT = 100
        private const val NARROWED_TAG_COUNT = 50
        private const val TITLE_PREFIX = "ScrollTagTitle"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun Tag.title(): String = detail.title

        private fun tagList(count: Int): List<Tag> {
            val titlePrefix = fixtureText(prefix = TITLE_PREFIX)

            return List(count) { index -> tag(title = "${titlePrefix}Index$index") }
        }

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
