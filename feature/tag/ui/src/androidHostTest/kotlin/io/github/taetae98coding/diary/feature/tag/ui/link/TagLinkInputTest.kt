package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class TagLinkInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-001 연결한 태그를 이모지와 제목으로 표시한다`() {
        val emojiTag = testTag(title = WORK_TAG_TITLE, emoji = EMOJI)
        val noEmojiTag = testTag(title = EXERCISE_TAG_TITLE)

        composeRule.setTagLinkInput(uiState = TagLinkInputUiState(linkedTagList = listOf(emojiTag, noEmojiTag)))

        composeRule.onNodeWithText("$EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-002 연결한 태그의 이모지나 제목이 바뀌면 칩에 반영된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val linkTagList = composeRule.setTagLinkInputWithLink()
        linkTagList(listOf(tag))

        linkTagList(listOf(tag.copy(detail = tag.detail.copy(title = RENAMED_TAG_TITLE))))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(RENAMED_TAG_TITLE).assertExists()

        linkTagList(listOf(tag.copy(detail = tag.detail.copy(emoji = EMOJI, title = RENAMED_TAG_TITLE, color = tag.detail.color.inv()))))

        composeRule.onNodeWithText(RENAMED_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText("$EMOJI $RENAMED_TAG_TITLE").assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-003 연결한 태그가 없어도 태그 연결 칩을 표시한다`() {
        composeRule.setTagLinkInput()

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-003 연결한 태그가 여러 개여도 태그 연결 칩을 표시한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))

        composeRule.setTagLinkInput(uiState = TagLinkInputUiState(linkedTagList = tagList))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-011 태그 칩을 누르면 그 태그의 상세 이동을 요청한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setTagLinkInput(
            uiState = TagLinkInputUiState(linkedTagList = listOf(tag)),
            onTagClick = clickedIdList::add,
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        clickedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-012 태그 칩을 눌러도 태그 연결 칩의 동작이 실행되지 않는다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        var linkClickCount = 0
        composeRule.setTagLinkInput(
            uiState = TagLinkInputUiState(linkedTagList = listOf(tag)),
            onLinkClick = { linkClickCount += 1 },
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        linkClickCount shouldBe 0
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `태그 연결 칩을 누르면 연결 목록 열기를 요청한다`() {
        var linkClickCount = 0
        composeRule.setTagLinkInput(onLinkClick = { linkClickCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()

        linkClickCount shouldBe 1
    }

    @Test
    fun `칩이 칩 영역보다 많으면 연결 입력이 그만큼 높아진다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> testTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }
        val linkTagList = composeRule.setTagLinkInputWithLink()
        val emptyHeight = composeRule.tagLinkInputHeight()

        linkTagList(tagList)

        composeRule.onNodeWithText(tagList.last().detail.title).assertExists()
        composeRule.tagLinkInputHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> testTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }

        composeRule.setTagLinkInput(uiState = TagLinkInputUiState(linkedTagList = tagList))

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun `태그를 연결해 칩이 칩 영역 안에 들어가면 연결 입력의 높이가 바뀌지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val linkTagList = composeRule.setTagLinkInputWithLink()
        val emptyHeight = composeRule.tagLinkInputHeight()

        linkTagList(tagList)

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.tagLinkInputHeight() shouldBe emptyHeight
    }

    public companion object {
        private const val EMOJI: String = "💼"
        private const val SCROLL_TAG_COUNT: Int = 20
        private const val SCROLL_TAG_TITLE_PREFIX: String = "TagLinkScroll"
    }
}
