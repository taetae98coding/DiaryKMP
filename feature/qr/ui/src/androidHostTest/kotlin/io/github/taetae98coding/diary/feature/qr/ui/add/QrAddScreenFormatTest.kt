package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddScreenFormatTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-1a5e7fb5-df16-4fd9-8bef-df01b624f7e9 제목 없이 추가하면 정보 탭으로 바꾸고 제목 입력을 알린다`() {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrTitleBlankException())
        setQrAddScreen(viewModel = qrAddViewModel(addQrUseCase = useCase))

        listOf("", "   ").forEach { title ->
            composeRule.onTitleInput().performTextReplacement(title)
            composeRule.onQrTextInput().performTextReplacement("abc")
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            withClue("제목=\"$title\"") {
                composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
                composeRule.onNodeWithText("제목을 입력해 주세요.").assertExists()
                composeRule.onTitleInput().assertIsFocused()
            }
        }
    }

    @Test
    fun `TC-b8dc1118-cdec-49eb-8c8b-3e5a817b93cb 제목 없이 추가해도 되돌리기 기록은 유지된다`() {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrTitleBlankException())
        setQrAddScreen(viewModel = qrAddViewModel(addQrUseCase = useCase))
        composeRule.onQrTextInput().performTextInput(HELLO)
        composeRule.selectFormat(formatLabel = DEFAULT_FORMAT_LABEL, formatName = WIFI_FORMAT_NAME)
        composeRule.onQrFieldInput(DEFAULT_NETWORK_NAME_LABEL).performTextInput(WIFI_SSID)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UNDO_DESCRIPTION).assertIsEnabled()

        clickAdd()

        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.onNodeWithContentDescription(DEFAULT_UNDO_DESCRIPTION).assertIsEnabled()

        composeRule.onNodeWithContentDescription(DEFAULT_UNDO_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onNode(hasText(DEFAULT_FORMAT_LABEL) and hasClickAction()).editableText() shouldBe TEXT_FORMAT_NAME
        composeRule.qrCodeValue() shouldBe HELLO
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setQrAddScreen(
        viewModel: QrAddViewModel,
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                QrAddScreen(
                    navigateUp = {},
                    navigateToScan = {},
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = remember { resultEventBus },
                    componentVisibleProvider = { QrAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val HELLO = "hello"
        const val WIFI_SSID = "home"
        const val WIFI_FORMAT_NAME = "Wi-Fi"
        const val TEXT_FORMAT_NAME = "Text"
        const val DEFAULT_NETWORK_NAME_LABEL = "Network name"
        const val DEFAULT_FORMAT_LABEL = "Format"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val DEFAULT_UNDO_DESCRIPTION = "Undo"
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
    }
}
