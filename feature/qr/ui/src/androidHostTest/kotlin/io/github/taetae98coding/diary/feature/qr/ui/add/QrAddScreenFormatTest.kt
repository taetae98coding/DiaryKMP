package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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
    fun `TC-QR-ADD-FEATURE-047 제목 없이 추가하면 정보 탭으로 바꾸고 제목 입력을 알린다`() {
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

    private fun valueEmptyViewModel(): QrAddViewModel {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
        return qrAddViewModel(addQrUseCase = useCase)
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
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val URL = "https://a.com"
        const val WIFI_SSID = "home"
        const val WIFI_FORMAT_NAME = "Wi-Fi"
        const val URL_FORMAT_NAME = "URL"
        const val DEFAULT_HIDDEN_NETWORK_LABEL = "Hidden network"
        const val DEFAULT_NETWORK_NAME_LABEL = "Network name"
        const val DEFAULT_FORMAT_LABEL = "Format"
        const val KOREAN_FORMAT_LABEL = "포맷"
        const val DEFAULT_URL_LABEL = "URL"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        const val PERIOD_SWITCH_LABEL = "날짜·시간"
        const val COORDINATE_MESSAGE = "지도에서 위치를 선택하거나, 위도는 -90에서 90, 경도는 -180에서 180 사이의 숫자로 입력해 주세요."
    }
}
