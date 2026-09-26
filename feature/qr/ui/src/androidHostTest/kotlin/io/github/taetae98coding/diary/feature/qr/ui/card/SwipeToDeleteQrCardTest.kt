package io.github.taetae98coding.diary.feature.qr.ui.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.home.qrCodeValueList
import io.github.taetae98coding.diary.feature.qr.ui.home.testQr
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeToDeleteQrCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-HOME-FEATURE-025 QR 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        setCard(qr = testQr(), onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
    }

    @Test
    fun `TC-QR-HOME-FEATURE-027 QR 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        val qr = testQr()
        setCard(qr = qr, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithText(qr.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-028 아직 준비되지 않은 자리는 밀어도 삭제되지 않는다`() {
        var deleteCount = 0
        setCard(qr = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `아직 준비되지 않은 자리는 QR을 그리지 않고 제목을 비워 둔다`() {
        setCard(qr = null)

        composeRule.qrCodeValueList() shouldContainExactly listOf("")
        composeRule
            .onNodeWithTag(QR_CARD_TEST_TAG)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { text -> text.text } shouldBe listOf("")
    }

    @Test
    fun `QR 카드의 접근성 이름은 카드에 표시한 제목이다`() {
        val qr = testQr()
        setCard(qr = qr)

        composeRule
            .onNodeWithTag(QR_CARD_TEST_TAG)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { text -> text.text } shouldBe listOf(qr.detail.title)
    }

    private fun setCard(
        qr: Qr?,
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeToDeleteQrCard(
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    qr = qr,
                )
            }
        }
    }
}
