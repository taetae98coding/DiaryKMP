package io.github.taetae98coding.diary.compose.tag

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
class EntityTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-001 연결한 태그를 이모지와 제목으로 표시한다`() {
        val emojiTag = entityTestTag(title = WORK_TAG_TITLE, emoji = EMOJI)
        val noEmojiTag = entityTestTag(title = EXERCISE_TAG_TITLE)

        composeRule.setEntityTagInput(uiState = EntityTagInputUiState(tagList = listOf(emojiTag, noEmojiTag)))

        composeRule.onNodeWithText("$EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-002 연결한 태그의 제목이 바뀌면 칩에 반영된다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)
        val setTagList = composeRule.setEntityTagInputWithTagList()
        setTagList(listOf(tag))

        setTagList(listOf(tag.copy(detail = tag.detail.copy(title = RENAMED_TAG_TITLE))))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(RENAMED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-003 연결한 태그가 없어도 태그 추가 칩을 표시한다`() {
        composeRule.setEntityTagInput()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-003 연결한 태그가 여러 개여도 태그 추가 칩을 표시한다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))

        composeRule.setEntityTagInput(uiState = EntityTagInputUiState(tagList = tagList))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-011 태그 칩을 누르면 그 태그의 상세 이동을 요청한다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        composeRule.setEntityTagInput(
            uiState = EntityTagInputUiState(tagList = listOf(tag)),
            onTagClick = clickedIdList::add,
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        clickedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-012 태그 칩을 눌러도 태그 추가 칩의 동작이 실행되지 않는다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)
        var addClickCount = 0
        composeRule.setEntityTagInput(
            uiState = EntityTagInputUiState(tagList = listOf(tag)),
            onAddClick = { addClickCount += 1 },
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()

        addClickCount shouldBe 0
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `태그 추가 칩을 누르면 연결 목록 열기를 요청한다`() {
        var addClickCount = 0
        composeRule.setEntityTagInput(onAddClick = { addClickCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()

        addClickCount shouldBe 1
    }

    @Test
    fun `칩이 칩 영역보다 많으면 태그 입력이 그만큼 높아진다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> entityTestTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }
        val setTagList = composeRule.setEntityTagInputWithTagList()
        val emptyHeight = composeRule.entityTagInputHeight()

        setTagList(tagList)

        composeRule.onNodeWithText(tagList.last().detail.title).assertExists()
        composeRule.entityTagInputHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val tagList = List(size = SCROLL_TAG_COUNT) { index -> entityTestTag(title = "$SCROLL_TAG_TITLE_PREFIX$index") }

        composeRule.setEntityTagInput(uiState = EntityTagInputUiState(tagList = tagList))

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun `태그를 연결해 칩이 칩 영역 안에 들어가면 연결 입력의 높이가 바뀌지 않는다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        val setTagList = composeRule.setEntityTagInputWithTagList()
        val emptyHeight = composeRule.entityTagInputHeight()

        setTagList(tagList)

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.entityTagInputHeight() shouldBe emptyHeight
    }

    public companion object {
        private const val EMOJI: String = "💼"
        private const val SCROLL_TAG_COUNT: Int = 20
        private const val SCROLL_TAG_TITLE_PREFIX: String = "EntityTagScroll"
    }
}
