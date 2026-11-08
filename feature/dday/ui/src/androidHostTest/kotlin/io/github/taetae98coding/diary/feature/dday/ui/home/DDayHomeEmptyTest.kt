package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-c5fdf9c8-7bf5-42fe-87f8-a3d2c7dcaaff 선택한 태그가 없는데 표시할 디데이가 없으면 아직 디데이가 없음을 알린다`() {
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-c5fdf9c8-7bf5-42fe-87f8-a3d2c7dcaaff 선택한 태그가 모두 무시되고 있으면 아직 디데이가 없음을 알린다`() {
        setDDayHomeScaffold(
            pagingData = homeDDayPagingDataOf(emptyList()),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = emptySet(), storedTagIdSet = setOf(Arb.kotlinUuid().next())),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-c5fdf9c8-7bf5-42fe-87f8-a3d2c7dcaaff 기본 환경에서 아직 디데이가 없음을 알린다`() {
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(emptyList()))

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-4884a24c-2ffd-489c-80d0-a202d51e98a8 필터를 적용한 채로 표시할 디데이가 없으면 조건에 맞는 디데이가 없음을 알린다`() {
        val tagId = Arb.kotlinUuid().next()
        setDDayHomeScaffold(
            pagingData = homeDDayPagingDataOf(emptyList()),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-4884a24c-2ffd-489c-80d0-a202d51e98a8 기본 환경에서 조건에 맞는 디데이가 없음을 알린다`() {
        val tagId = Arb.kotlinUuid().next()
        setDDayHomeScaffold(
            pagingData = homeDDayPagingDataOf(emptyList()),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
        )

        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-d27b31e6-096d-4be1-9415-e8d941d89471 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setDDayHomeScaffold(pagingData = loadingHomeDDayPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-755a045f-5c2b-400c-a2b3-2affd6ff6852 표시할 디데이가 있으면 빈 상태 안내를 표시하지 않는다`() {
        val tagId = Arb.kotlinUuid().next()
        setDDayHomeScaffold(
            pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = DDAY_TITLE))),
            filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
        )

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(DDAY_TITLE)).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-3c1ca627-98c7-4412-b8f9-f4232bbab368 처음 불러오기에 실패하면 빈 상태 안내를 표시한다`() {
        setDDayHomeScaffold(pagingData = failedHomeDDayPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertDoesNotExist()
    }

    private fun setDDayHomeScaffold(
        pagingData: PagingData<DDay>,
        filterUiState: DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState(),
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                DDayHomeScaffold(
                    onEvent = {},
                    onDDayListEvent = {},
                    ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val KOREAN_EMPTY_TITLE = "아직 디데이가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 디데이를 만들 수 있습니다"
        const val KOREAN_FILTERED_EMPTY_TITLE = "조건에 맞는 디데이가 없습니다"
        const val KOREAN_FILTERED_EMPTY_DESCRIPTION = "필터를 바꾸면 다른 디데이를 볼 수 있습니다"
        const val DEFAULT_EMPTY_TITLE = "No D-Days yet"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a D-Day."
        const val DEFAULT_FILTERED_EMPTY_TITLE = "No D-Days match the filter"
        const val DEFAULT_FILTERED_EMPTY_DESCRIPTION = "Change the filter to see other D-Days."
        const val DDAY_TITLE = "Anniversary"
    }
}
