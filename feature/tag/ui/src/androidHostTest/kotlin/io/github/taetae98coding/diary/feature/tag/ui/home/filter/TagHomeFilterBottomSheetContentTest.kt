package io.github.taetae98coding.diary.feature.tag.ui.home.filter

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ddf3850b-a348-43e0-8574-d522000b9e7f 필터를 열면 꺼진 최상위 태그만 보기를 표시한다`() {
        setContent(isTopLevelOnly = false)

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_TOP_LEVEL_ONLY_LABEL).assertIsOff()
    }

    @Test
    fun `TC-ca4e614b-4a87-4964-a002-4b57f24ecf8f 필터를 열면 켜 둔 최상위 태그만 보기를 표시한다`() {
        setContent(isTopLevelOnly = true)

        composeRule.onNodeWithText(DEFAULT_TOP_LEVEL_ONLY_LABEL).assertIsOn()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 필터 제목과 항목 문구는 필터와 최상위 태그만이다`() {
        setContent()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_TOP_LEVEL_ONLY_LABEL).assertIsOff()
    }

    @Test
    fun `TC-7bcf8584-ace4-4e5a-b5fe-6aaf5fc8bb0a 최상위 태그만 보기를 켜면 켜짐 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<TagHomeFilterBottomSheetEvent>()
        setContent(isTopLevelOnly = false, onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_TOP_LEVEL_ONLY_LABEL).performClick()

        eventList shouldBe listOf(TagHomeFilterBottomSheetEvent.SetTopLevelOnly(isTopLevelOnly = true))
    }

    @Test
    fun `TC-a9dac55d-0689-4876-b138-7041b9950710 최상위 태그만 보기를 끄면 꺼짐 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<TagHomeFilterBottomSheetEvent>()
        setContent(isTopLevelOnly = true, onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_TOP_LEVEL_ONLY_LABEL).performClick()

        eventList shouldBe listOf(TagHomeFilterBottomSheetEvent.SetTopLevelOnly(isTopLevelOnly = false))
    }

    @Test
    fun `최상위 태그만 행에서 누를 수 있는 자리는 좌우 끝까지 채운다`() {
        setContent()

        val rootBounds = composeRule.onRoot().getBoundsInRoot()
        val rowBounds = composeRule.onNodeWithText(DEFAULT_TOP_LEVEL_ONLY_LABEL).getBoundsInRoot()

        rowBounds.left shouldBe rootBounds.left
        rowBounds.right shouldBe rootBounds.right
    }

    private fun setContent(
        isTopLevelOnly: Boolean = false,
        onEvent: (TagHomeFilterBottomSheetEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagHomeFilterBottomSheetContent(
                    onEvent = onEvent,
                    uiStateProvider = { TagHomeFilterUiState(isTopLevelOnly = isTopLevelOnly) },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_TITLE = "Filter"
        private const val KOREAN_TITLE = "필터"
        private const val DEFAULT_TOP_LEVEL_ONLY_LABEL = "Top-level tags only"
        private const val KOREAN_TOP_LEVEL_ONLY_LABEL = "최상위 태그만"
    }
}
