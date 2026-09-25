package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeScaffoldFilterTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-027 필터 버튼을 누르면 필터 열기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        setTagHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagHomeScaffoldEvent.ClickFilter)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-027 한국어 환경에서 필터 버튼 이름은 필터이다`() {
        setTagHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_FILTER_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-037 최상위 태그만 보기를 끄면 필터 버튼에 적용 상태를 알리지 않는다`() {
        setTagHomeScaffold(isFilterApplied = false)

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION).not())
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-037 최상위 태그만 보기를 켜면 필터 버튼이 적용 상태를 알린다`() {
        setTagHomeScaffold(isFilterApplied = true)

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 필터 적용 상태 설명은 필터 적용됨이다`() {
        setTagHomeScaffold(isFilterApplied = true)

        composeRule
            .onNodeWithContentDescription(KOREAN_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(KOREAN_FILTER_APPLIED_STATE_DESCRIPTION))
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-031 필터를 켠 채로 표시할 태그가 없으면 최상위 태그가 없음을 알린다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()), isFilterApplied = true)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-031 한국어 환경에서 좁힌 빈 상태 안내는 최상위 태그가 없습니다이다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()), isFilterApplied = true)

        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-032 필터를 끈 채로 표시할 태그가 없으면 아직 태그가 없음을 알린다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()), isFilterApplied = false)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-033 필터를 켜 두어도 태그 추가와 완료된 태그 확인, 검색, 태그 선택을 실행할 수 있다`() {
        val tag = tag(title = TAG_TITLE)
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(tag)), isFilterApplied = true)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).assert(hasClickAction())
    }

    private fun setTagHomeScaffold(
        pagingData: PagingData<Tag> = tagPagingDataOf(emptyList()),
        isFilterApplied: Boolean = false,
        onEvent: (TagHomeScaffoldEvent) -> Unit = {},
    ) {
        val tagPagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagHomeScaffold(
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    filterUiStateProvider = { TagHomeScaffoldFilterUiState(isApplied = isFilterApplied) },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val KOREAN_FILTER_BUTTON_DESCRIPTION = "필터"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private const val KOREAN_FILTER_APPLIED_STATE_DESCRIPTION = "필터 적용됨"
        private const val DEFAULT_EMPTY_TITLE = "No tags yet"
        private const val DEFAULT_FILTERED_EMPTY_TITLE = "No top-level tags"
        private const val DEFAULT_FILTERED_EMPTY_DESCRIPTION = "Turn off the filter to see all tags."
        private const val KOREAN_FILTERED_EMPTY_TITLE = "최상위 태그가 없습니다"
        private const val KOREAN_FILTERED_EMPTY_DESCRIPTION = "필터를 끄면 모든 태그를 볼 수 있습니다"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished tags"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val TAG_TITLE = "FilterTagTitle"

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
