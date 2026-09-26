package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class QrHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-HOME-FEATURE-003 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setQrHomeScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-QR-HOME-FEATURE-013 QR 추가를 선택하면 QrAdd 화면으로 이동한다`() {
        var navigateToAddCount = 0
        setQrHomeScreen(navigateToAdd = { navigateToAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToAddCount shouldBe 1
    }

    private fun setQrHomeScreen(
        navigateUp: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                QrHomeScreen(
                    navigateUp = navigateUp,
                    navigateToAdd = navigateToAdd,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
    }
}
