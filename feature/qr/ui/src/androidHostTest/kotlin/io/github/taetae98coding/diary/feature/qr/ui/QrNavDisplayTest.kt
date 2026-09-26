package io.github.taetae98coding.diary.feature.qr.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrValueInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrCodeValue
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.add.qrValue
import io.github.taetae98coding.diary.feature.qr.ui.add.qrValueInputText
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(QrHomeNavKey)
    private val resultEventBus = ResultEventBus()

    @Test
    fun `TC-QR-ADD-FEATURE-015 QrScan 화면에서 읽지 않고 돌아오면 입력 값을 그대로 둔다`() {
        val value = qrTestFixtureMonkey.qrValue()
        setQrNavDisplay()
        openQrScanWithValue(value = value)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    @Test
    fun `TC-QR-ADD-FEATURE-014 QrScan 화면에서 QR을 읽어 돌아오면 입력 값을 읽은 값으로 바꾼다`() {
        val scannedValue = qrTestFixtureMonkey.qrValue()
        setQrNavDisplay()
        openQrScanWithValue(value = qrTestFixtureMonkey.qrValue())

        composeRule.runOnIdle { backStack.navigateUpWithQrScannedValue(resultEventBus = resultEventBus, value = scannedValue) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe scannedValue
        composeRule.qrCodeValue() shouldBe scannedValue
    }

    @Test
    fun `TC-QR-SCAN-FEATURE-007 QR을 읽으면 읽은 값을 넘기고 이전 화면으로 돌아간다`() {
        val scannedValue = qrTestFixtureMonkey.qrValue()
        setQrNavDisplay()
        openQrScanWithValue(value = qrTestFixtureMonkey.qrValue())

        composeRule.runOnIdle { backStack.navigateUpWithQrScannedValue(resultEventBus = resultEventBus, value = scannedValue) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertDoesNotExist()
        backStack.toList() shouldBe listOf(QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe scannedValue
    }

    @Test
    fun `TC-QR-ADD-FEATURE-017 뒤로가기로 떠났다가 다시 들어오면 값이 비어 있다`() {
        setQrNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onQrValueInput().performTextInput(qrTestFixtureMonkey.qrValue())
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(QrHomeNavKey, QrAddNavKey)
        composeRule.qrValueInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
    }

    private fun openQrScanWithValue(value: String) {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(QrScanNavKey) }
        composeRule.onNodeWithText(DEFAULT_SCAN_TITLE).assertExists()
    }

    private fun setQrNavDisplay() {
        composeRule.setContent {
            CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
                DiaryTheme {
                    NavDisplay(
                        backStack = backStack,
                        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                        entryProvider = entryProvider { qrEntry(backStack = backStack) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    public companion object {
        private const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SCAN_TITLE = "Scan QR code"
    }
}
