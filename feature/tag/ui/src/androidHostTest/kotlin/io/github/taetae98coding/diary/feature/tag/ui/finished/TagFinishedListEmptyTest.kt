package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.loadingTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagFinishedListEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-016 완료된 태그가 없으면 빈 상태 안내를 표시한다`() {
        setTagFinishedListScaffold(MutableStateFlow(tagPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-FINISHED-LIST-FEATURE-016 한국어 환경에서 빈 상태 안내는 완료한 태그가 없습니다이다`() {
        setTagFinishedListScaffold(MutableStateFlow(tagPagingDataOf(emptyList())))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-016 빈 상태에 태그 추가를 권하는 안내를 두지 않는다`() {
        setTagFinishedListScaffold(MutableStateFlow(tagPagingDataOf(emptyList())))

        composeRule.onNodeWithText(TAG_HOME_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-017 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setTagFinishedListScaffold(MutableStateFlow(loadingTagPagingData()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-020 처음 불러오기에 실패하면 오류 안내 없이 빈 상태 안내를 표시한다`() {
        setTagFinishedListScaffold(
            MutableStateFlow(tagEntityPagingData(itemList = emptyList<Tag>(), refresh = LoadState.Error(IllegalStateException("Refresh failed")))),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-020 이어서 불러오기에 실패해도 앞서 불러온 태그를 그대로 표시한다`() {
        setTagFinishedListScaffold(
            MutableStateFlow(tagEntityPagingData(itemList = listOf(tag(title = TAG_TITLE)), append = LoadState.Error(IllegalStateException("Append failed")))),
        )

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    private fun setTagFinishedListScaffold(
        tagPagingDataFlow: MutableStateFlow<PagingData<Tag>>,
        onEvent: (TagFinishedListScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagFinishedListScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No finished tags"
        private const val KOREAN_EMPTY_TITLE = "완료한 태그가 없습니다"
        private const val TAG_HOME_EMPTY_DESCRIPTION = "Use the add button to create a tag."
        private const val TAG_TITLE = "FinishedEmptyStateTagTitle"
        private const val DEFAULT_RETRY_TEXT = "Retry"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
