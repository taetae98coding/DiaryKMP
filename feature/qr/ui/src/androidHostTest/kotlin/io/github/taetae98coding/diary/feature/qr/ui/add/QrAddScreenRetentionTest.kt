package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-ADD-DOMAIN-003 화면이 재생성되어도 입력한 내용을 유지한다`() {
        val viewModel = screenTestViewModel()

        assertInputRetained { viewModel }
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-003 시스템이 앱을 되살리며 화면을 다시 보여 줘도 입력한 내용을 모두 복원한다`() {
        // 메모리 정리 뒤에는 화면 상태를 들고 있던 객체도 새로 만들어지므로 복원할 때마다 새 인스턴스를 쓴다.
        assertInputRetained { screenTestViewModel() }
    }

    private fun assertInputRetained(viewModelFactory: () -> QrAddViewModel) {
        val (title, description, value) = qrTestFixtureMonkey.qrDetail()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            val viewModel = remember { viewModelFactory() }

            DiaryTheme {
                QrAddScreen(
                    navigateUp = {},
                    navigateToScan = {},
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = remember { ResultEventBus() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.onTitleInput().performTextInput(title)
        composeRule.onDescriptionInput().performTextInput(description)
        composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe title
        composeRule.descriptionInputText() shouldBe description
        composeRule.qrValueInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }
}
