package io.github.taetae98coding.diary.compose.tag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TagCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `태그 카드의 제목 왼쪽 원형 표시에 저장된 컬러를 사용한다`() {
        val colorArgb = Arb.int().next() or 0xFF000000.toInt()
        val tag = tag(colorArgb = colorArgb)

        setTagCard(tag)

        val indicator =
            composeRule.onNodeWithTag(
                testTag = TAG_COLOR_INDICATOR_TEST_TAG,
                useUnmergedTree = true,
            )
        val indicatorBounds = indicator.fetchSemanticsNode().boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()
        val titleBounds =
            composeRule
                .onNodeWithText(
                    text = tag.detail.emojiWithTitle,
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot

        pixelMap[
            indicatorBounds.center.x.roundToInt(),
            indicatorBounds.center.y.roundToInt(),
        ] shouldBe Color(colorArgb)
        (indicatorBounds.right < titleBounds.left) shouldBe true
    }

    @Test
    fun `TC-8df6f641-a6db-4d82-ba6e-289153d32335 TC-10a84e58-1e05-42de-8968-2648dc04ffe3 자리 표시 카드는 이모지와 제목을 비워 표시한다`() {
        val tag = tag(colorArgb = Arb.int().next() or 0xFF000000.toInt())

        setTagCard(tag = null)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithText(tag.detail.emojiWithTitle).assertDoesNotExist()
    }

    @Test
    fun `자리 표시 카드의 컬러 원형 표시는 카드 배경과 같은 색으로 비워 둔다`() {
        setTagCard(tag = null)

        val indicatorBounds =
            composeRule
                .onNodeWithTag(
                    testTag = TAG_COLOR_INDICATOR_TEST_TAG,
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()
        val indicatorColor =
            pixelMap[
                indicatorBounds.center.x.roundToInt(),
                indicatorBounds.center.y.roundToInt(),
            ]
        val cardBackgroundColor =
            pixelMap[
                (indicatorBounds.right + BESIDE_INDICATOR_OFFSET).roundToInt(),
                indicatorBounds.center.y.roundToInt(),
            ]

        indicatorColor shouldBe cardBackgroundColor
    }

    @Test
    fun `TC-f7afc78a-1b0f-48f3-b488-22d190b0130e TC-b0231338-6fe8-4df1-9392-fce90f7711f6 TC-f37b3a3e-09fd-4ad0-bd7f-db2f0cb9e101 자리 표시 카드는 선택할 수 없다`() {
        var clickCount = 0

        setTagCard(tag = null, onClick = { clickCount += 1 })

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    @Test
    fun `TC-5c13d3ed-4d6b-43c3-b434-2ffa97b75334 TC-aa7644ae-95fc-4af9-8aa5-14a62c2f7417 카드 폭보다 긴 제목의 태그는 이모지와 제목이 여러 줄로 나뉘지 않고 한 줄에 생략 없이 담긴다`() {
        val baseTag = tag(colorArgb = Arb.int().next() or 0xFF000000.toInt())
        val tag = baseTag.copy(detail = baseTag.detail.copy(emoji = EMOJI, title = LONG_TITLE))
        val emojiWithTitle = tag.detail.emojiWithTitle

        setTagCard(tag)

        val cardWidth =
            composeRule
                .onNodeWithTag(TAG_CARD_TEST_TAG)
                .fetchSemanticsNode()
                .size.width
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        composeRule
            .onNodeWithText(text = emojiWithTitle, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(textLayoutResults) }
        val textLayoutResult = textLayoutResults.single()

        (textLayoutResult.size.width > cardWidth) shouldBe true
        textLayoutResult.lineCount shouldBe 1
        textLayoutResult.isLineEllipsized(lineIndex = 0) shouldBe false
        textLayoutResult.getLineEnd(lineIndex = 0, visibleEnd = false) shouldBe emojiWithTitle.length
    }

    @Test
    fun `카드 폭보다 긴 제목은 시간이 지나면 그려지는 위치가 바뀐다`() {
        val baseTag = tag(colorArgb = Arb.int().next() or 0xFF000000.toInt())
        val tag = baseTag.copy(detail = baseTag.detail.copy(emoji = EMOJI, title = LONG_TITLE))
        composeRule.mainClock.autoAdvance = false
        setTagCard(tag)
        composeRule.mainClock.advanceTimeByFrame()

        val first = captureCard()
        val sameTime = captureCard()
        composeRule.mainClock.advanceTimeBy(MARQUEE_MOVE_MILLIS)
        val later = captureCard()

        sameTime.contentEquals(first) shouldBe true
        later.contentEquals(first) shouldBe false
    }

    private fun captureCard(): IntArray {
        val pixelMap = composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).captureToImage().toPixelMap()
        return IntArray(pixelMap.width * pixelMap.height) { index -> pixelMap[index % pixelMap.width, index / pixelMap.width].toArgb() }
    }

    private fun tag(colorArgb: Int): Tag =
        Arb
            .tag()
            .next()
            .copy(
                detail = Arb.tagDetail().next().copy(title = "title-${Arb.string().next()}", color = colorArgb.toLong()),
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

    private fun setTagCard(
        tag: Tag?,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagCard(
                    tag = tag,
                    onClick = onClick,
                )
            }
        }
    }

    private companion object {
        private const val BESIDE_INDICATOR_OFFSET = 4f
        private const val EMOJI = "🏃"
        private val LONG_TITLE = "LongTagCardTitle".repeat(20)

        // 반복 이동은 처음 1.2초를 기다린 뒤 시작하므로 그보다 충분히 뒤의 화면을 비교한다.
        private const val MARQUEE_MOVE_MILLIS = 3_000L
    }
}
