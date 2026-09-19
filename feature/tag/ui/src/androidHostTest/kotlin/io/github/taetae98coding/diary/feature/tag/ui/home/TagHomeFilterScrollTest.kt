package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    fun `TC-TAG-HOME-FEATURE-034 필터를 켜고 좁힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val isApplied = mutableStateOf(false)
        val tagPagingData = MutableStateFlow(tagPagingDataOf(allTagList))
        setTagHomeScaffold(isApplied = isApplied, tagPagingData = tagPagingData)

        scrollToLast(tagList = allTagList)
        switchFilter(isApplied = isApplied, value = true)
        place(tagPagingData = tagPagingData, tagList = allTagList.take(FILTERED_TAG_COUNT))

        composeRule.onNodeWithText(allTagList.first().title()).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-034 필터를 끄고 넓힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val filteredTagList = allTagList.take(FILTERED_TAG_COUNT)
        val isApplied = mutableStateOf(true)
        val tagPagingData = MutableStateFlow(tagPagingDataOf(filteredTagList))
        setTagHomeScaffold(isApplied = isApplied, tagPagingData = tagPagingData)

        scrollToLast(tagList = filteredTagList)
        switchFilter(isApplied = isApplied, value = false)
        place(tagPagingData = tagPagingData, tagList = allTagList)

        composeRule.onNodeWithText(allTagList.first().title()).assertExists()
    }

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
    fun `TC-TAG-HOME-DOMAIN-012 필터를 바꾸지 않은 목록 갱신에서는 목록 위치를 유지한다`() {
        val allTagList = tagList(ALL_TAG_COUNT)
        val isApplied = mutableStateOf(false)
        val tagPagingData = MutableStateFlow(tagPagingDataOf(allTagList))
        setTagHomeScaffold(isApplied = isApplied, tagPagingData = tagPagingData)

        scrollToLast(tagList = allTagList)
        place(tagPagingData = tagPagingData, tagList = allTagList + tag(title = "${TITLE_PREFIX}Added"))

        composeRule.onNodeWithText(allTagList.first().title()).assertDoesNotExist()
    }

    // 필터를 바꾸기 전에 사용자가 첫 태그 카드가 보이지 않는 자리까지 이동해 둔 상태를 만든다.
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

    // 필터를 바꾼 뒤 한 박자 늦게 놓이는 목록을 만든다.
    private fun place(
        tagPagingData: MutableStateFlow<PagingData<Tag>>,
        tagList: List<Tag>,
    ) {
        composeRule.runOnIdle { tagPagingData.value = tagPagingDataOf(tagList) }
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
                    tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                    onEvent = {},
                    filterUiStateProvider = { TagHomeScaffoldFilterUiState(isApplied = currentIsApplied) },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val ALL_TAG_COUNT = 100
        private const val FILTERED_TAG_COUNT = 60
        private const val TITLE_PREFIX = "ScrollTagTitle"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun Tag.title(): String = detail.title

        private fun tagList(count: Int): List<Tag> = List(count) { index -> tag(title = "$TITLE_PREFIX$index") }

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
