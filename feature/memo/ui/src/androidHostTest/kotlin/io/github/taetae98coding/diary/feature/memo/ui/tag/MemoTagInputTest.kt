package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `선택한 태그를 칩으로 표시하고 대표 태그 칩에 접근성 이름을 제공한다`() {
        val primaryTag = testTag(title = WORK_TAG_TITLE)
        val otherTag = testTag(title = EXERCISE_TAG_TITLE)

        composeRule.setMemoTagInput(
            uiState = MemoTagInputUiState(selectedTagList = listOf(primaryTag, otherTag), primaryTagId = primaryTag.id),
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-013 선택한 태그의 이모지나 제목이 바뀌면 태그 칩에 반영된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val otherTag = testTag(title = EXERCISE_TAG_TITLE)
        val renamedTag = tag.copy(detail = tag.detail.copy(emoji = RENAMED_TAG_EMOJI, title = RENAMED_TAG_TITLE))
        val selectTagList = composeRule.setMemoTagInputWithSelection()
        selectTagList(listOf(tag, otherTag), tag.id)
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        selectTagList(listOf(renamedTag, otherTag), tag.id)

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText("$RENAMED_TAG_EMOJI $RENAMED_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `선택한 태그 구성이 그대로면 태그 내용이 바뀌어도 칩을 전환 없이 갱신한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val selectTagList = composeRule.setMemoTagInputWithSelection()
        selectTagList(listOf(tag), tag.id)

        composeRule.mainClock.autoAdvance = false
        selectTagList(listOf(tag.copy(detail = tag.detail.copy(title = RENAMED_TAG_TITLE))), tag.id)
        composeRule.mainClock.advanceTimeByFrame()

        // 전환이 일어나면 이전 칩이 사라지기 전까지 두 제목이 함께 남는다.
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(RENAMED_TAG_TITLE).assertExists()
    }

    @Test
    fun `칩이 칩 영역보다 많으면 태그 입력이 그만큼 높아진다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> testTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }
        val selectTagList = composeRule.setMemoTagInputWithSelection()
        val emptyHeight = composeRule.memoTagInputHeight()

        selectTagList(tagList, tagList.first().id)

        composeRule.onNodeWithText(tagList.last().detail.title).assertExists()
        composeRule.memoTagInputHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> testTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }

        composeRule.setMemoTagInput(uiState = MemoTagInputUiState(selectedTagList = tagList))

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun `태그를 선택해 칩이 칩 영역 안에 들어가면 태그 입력의 높이가 바뀌지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val selectTagList = composeRule.setMemoTagInputWithSelection()
        val emptyHeight = composeRule.memoTagInputHeight()

        selectTagList(tagList, tagList.first().id)

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.memoTagInputHeight() shouldBe emptyHeight
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-022 태그 칩을 눌러도 추가 항목의 동작이 실행되지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var addClickCount = 0
        composeRule.setMemoTagInput(
            uiState = MemoTagInputUiState(selectedTagList = listOf(tagList.first())),
            onAddClick = { addClickCount += 1 },
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        addClickCount shouldBe 0
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-022 대표 태그 칩을 눌러도 추가 항목의 동작이 실행되지 않는다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        var addClickCount = 0
        composeRule.setMemoTagInput(
            uiState = MemoTagInputUiState(selectedTagList = listOf(tag), primaryTagId = tag.id),
            onAddClick = { addClickCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).performClick()

        addClickCount shouldBe 0
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    public companion object {
        private const val SCROLL_TAG_TITLE_PREFIX = "MemoTagScroll"
        private const val SCROLL_TAG_COUNT = 30
        private const val RENAMED_TAG_TITLE = "MemoTagRenamed"
        private const val RENAMED_TAG_EMOJI = "🏃"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagInputAddChipTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 입력의 문구를 표시한다`() {
        composeRule.setMemoTagInput(uiState = MemoTagInputUiState())

        composeRule.onNodeWithText(KOREAN_TAG_SELECT_LABEL).assert(hasClickLabel(KOREAN_SELECT_ACTION))
    }

    @Test
    fun `기본 환경에서 태그 입력의 문구를 표시한다`() {
        composeRule.setMemoTagInput(uiState = MemoTagInputUiState())

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).assert(hasClickLabel(DEFAULT_SELECT_ACTION))
    }

    @Test
    fun `추가 항목을 누르면 추가 항목의 동작이 한 번 실행된다`() {
        var addClickCount = 0
        composeRule.setMemoTagInput(onAddClick = { addClickCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performClick()

        addClickCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-028 선택한 태그가 없거나 여러 개여도 태그 추가 항목이 표시된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val selectTagList = composeRule.setMemoTagInputWithSelection()

        listOf(
            emptyList(),
            tagList,
        ).forEach { selectedTagList ->
            selectTagList(selectedTagList, null)

            composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).assertExists()
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagInputNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-021 태그 칩을 누르면 그 태그를 대상으로 상세 이동을 요청한다`() {
        val primaryTag = testTag(title = WORK_TAG_TITLE)
        val otherTag = testTag(title = EXERCISE_TAG_TITLE)
        val clickedTagIdList = mutableListOf<Uuid>()

        composeRule.setMemoTagInput(
            uiState = MemoTagInputUiState(selectedTagList = listOf(primaryTag, otherTag), primaryTagId = primaryTag.id),
            onTagClick = { id -> clickedTagIdList += id },
        )

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assert(hasClickLabel(DEFAULT_TAG_DETAIL_ACTION))
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        clickedTagIdList shouldBe listOf(otherTag.id, primaryTag.id)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-TAG-INPUT-FEATURE-021 한국어 환경에서 태그 칩에 상세 이동 동작 이름을 제공한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoTagInput(
            uiState = MemoTagInputUiState(selectedTagList = listOf(tag)),
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).assert(hasClickLabel(KOREAN_TAG_DETAIL_ACTION))
    }
}
