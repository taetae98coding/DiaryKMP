package io.github.taetae98coding.diary.compose.tag

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TagCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `태그 카드의 제목 왼쪽 원형 표시에 저장된 컬러를 사용한다`() {
        val colorArgb = fixtureMonkey.giveMeOne<Int>() or 0xFF000000.toInt()
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
    fun `TC-TAG-HOME-FEATURE-015 TC-TAG-FINISHED-LIST-FEATURE-014 자리 표시 카드는 이모지, 제목과 컬러를 비워 표시한다`() {
        val tag = tag(colorArgb = fixtureMonkey.giveMeOne<Int>() or 0xFF000000.toInt())

        setTagCard(tag = null)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithText(tag.detail.emojiWithTitle).assertDoesNotExist()

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
    fun `TC-TAG-HOME-FEATURE-016 TC-TAG-FINISHED-LIST-FEATURE-015 TC-SEARCH-HOME-FEATURE-013 자리 표시 카드는 선택할 수 없다`() {
        var clickCount = 0

        setTagCard(tag = null, onClick = { clickCount += 1 })

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    private fun tag(colorArgb: Int): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(
                Tag::detail,
                fixtureMonkey.giveMeOne<TagDetail>().copy(color = colorArgb.toLong()),
            ).setExp(
                Tag::updatedAt,
                fixtureMonkey.giveMeOne<Instant>(),
            ).setExp(
                Tag::createdAt,
                fixtureMonkey.giveMeOne<Instant>(),
            ).sample()

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
    }
}
