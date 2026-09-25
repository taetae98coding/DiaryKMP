package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.failedTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.list.loadingTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-018 표시할 태그가 없으면 빈 상태 안내를 표시한다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-018 한국어 환경에서 빈 상태 안내는 아직 태그가 없습니다이다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-019 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setTagHomeScaffold(pagingData = loadingTagPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-020 표시할 태그가 있으면 빈 상태 안내를 표시하지 않는다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(tag(title = TAG_TITLE))))

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-022 목록을 조회하지 못하면 빈 상태 안내를 표시한다`() {
        setTagHomeScaffold(pagingData = failedTagPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-038 이어서 불러오지 못해도 이미 보이는 태그를 그대로 둔다`() {
        setTagHomeScaffold(
            pagingData =
                PagingData.from(
                    data = listOf(tag(title = TAG_TITLE)),
                    sourceLoadStates =
                        LoadStates(
                            refresh = LoadState.NotLoading(endOfPaginationReached = false),
                            prepend = LoadState.NotLoading(endOfPaginationReached = true),
                            append = LoadState.Error(IllegalStateException(TAG_TITLE)),
                        ),
                ),
        )

        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-021 빈 상태에서도 태그 추가와 완료된 태그 확인을 실행할 수 있다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-025 빈 상태에서도 검색을 실행할 수 있다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `빈 상태 안내는 목록 영역의 세로 가운데에 놓인다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        val listBounds = composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).getUnclippedBoundsInRoot()
        val emptyBounds = composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).getUnclippedBoundsInRoot()

        val listCenter = (listBounds.top + listBounds.bottom) / 2
        val emptyCenter = (emptyBounds.top + emptyBounds.bottom) / 2

        abs((emptyCenter - listCenter).value) shouldBeLessThan CENTER_TOLERANCE.value
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-023 빈 상태에서도 목록을 당겨 새로고침할 수 있다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()), onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagHomeScaffoldEvent.Refresh)
    }

    private fun setTagHomeScaffold(
        pagingData: PagingData<Tag>,
        onEvent: (TagHomeScaffoldEvent) -> Unit = {},
    ) {
        val tagPagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagHomeScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No tags yet"
        private const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a tag."
        private const val KOREAN_EMPTY_TITLE = "아직 태그가 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 태그를 만들 수 있습니다"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished tags"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val TAG_TITLE = "EmptyStateTagTitle"
        private val CENTER_TOLERANCE: Dp = 2.dp

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(title: String): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
