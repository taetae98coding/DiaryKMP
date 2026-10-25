package io.github.taetae98coding.diary.compose.memo

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
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
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
class MemoCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `메모 카드의 제목 왼쪽 원형 표시에 저장된 컬러를 사용한다`() {
        val colorArgb = fixtureMonkey.giveMeOne<Int>() or 0xFF000000.toInt()
        val detail = fixtureMonkey.giveMeOne<MemoDetail>()
        val memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    detail.copy(title = "Title${detail.title}", color = colorArgb.toLong()),
                ).setExp(
                    Memo::updatedAt,
                    fixtureMonkey.giveMeOne<Instant>(),
                ).setExp(
                    Memo::createdAt,
                    fixtureMonkey.giveMeOne<Instant>(),
                ).sample()

        setMemoCard(memo)

        val indicator =
            composeRule.onNodeWithTag(
                testTag = MEMO_COLOR_INDICATOR_TEST_TAG,
                useUnmergedTree = true,
            )
        val indicatorBounds = indicator.fetchSemanticsNode().boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()
        val titleBounds =
            composeRule
                .onNodeWithText(
                    text = memo.detail.title,
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
    fun `메모 자리 표시 카드에는 컬러 표시를 노출하지 않는다`() {
        setMemoCard(memo = null)

        composeRule
            .onNodeWithTag(
                testTag = MEMO_COLOR_INDICATOR_TEST_TAG,
                useUnmergedTree = true,
            ).assertDoesNotExist()
    }

    @Test
    fun `TC-34863fc6-f9ec-47d1-9291-c92efa8270e2 기간은 제목 아래에 표시된다`() {
        val memo =
            memo(
                MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 21)),
            )

        setMemoCard(memo)

        val titleBounds =
            composeRule
                .onNodeWithText(
                    text = memo.detail.title,
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        val dateTimeBounds =
            composeRule
                .onNodeWithTag(
                    testTag = MEMO_DATE_TIME_TEST_TAG,
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot

        (dateTimeBounds.top >= titleBounds.bottom) shouldBe true
    }

    @Test
    fun `TC-a42c7b4e-7205-4b2d-9e4e-06ebb46eccd6 기간이 없는 메모의 카드에는 기간을 노출하지 않는다`() {
        setMemoCard(memo(dateTime = null))

        composeRule
            .onNodeWithTag(
                testTag = MEMO_DATE_TIME_TEST_TAG,
                useUnmergedTree = true,
            ).assertDoesNotExist()
    }

    @Test
    fun `TC-c7c834f5-7b6d-4bb8-b7fa-9a8153eab35d 메모 자리 표시 카드에는 기간을 노출하지 않는다`() {
        setMemoCard(memo = null)

        composeRule
            .onNodeWithTag(
                testTag = MEMO_DATE_TIME_TEST_TAG,
                useUnmergedTree = true,
            ).assertDoesNotExist()
    }

    @Test
    fun `TC-f37b3a3e-09fd-4ad0-bd7f-db2f0cb9e101 자리 표시 카드는 선택할 수 없다`() {
        var clickCount = 0

        setMemoCard(memo = null, onClick = { clickCount += 1 })

        composeRule.onNodeWithTag(MEMO_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    private fun memo(dateTime: MemoDateTime?): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(
                Memo::detail,
                fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = dateTime),
            ).setExp(
                Memo::updatedAt,
                fixtureMonkey.giveMeOne<Instant>(),
            ).setExp(
                Memo::createdAt,
                fixtureMonkey.giveMeOne<Instant>(),
            ).sample()

    private fun setMemoCard(
        memo: Memo?,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoCard(
                    memo = memo,
                    onClick = onClick,
                )
            }
        }
    }
}
