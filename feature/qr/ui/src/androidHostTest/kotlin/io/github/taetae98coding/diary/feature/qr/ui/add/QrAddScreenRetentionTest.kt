package io.github.taetae98coding.diary.feature.qr.ui.add

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
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
    fun `TC-020b000b-bc89-4362-8e8e-b9a27c65ebdb 화면이 재생성되어도 입력한 내용을 유지한다`() {
        val viewModel = screenTestViewModel()

        assertInputRetained { viewModel }
    }

    @Test
    fun `TC-020b000b-bc89-4362-8e8e-b9a27c65ebdb 시스템이 앱을 되살리며 화면을 다시 보여 줘도 입력한 내용을 모두 복원한다`() {
        // 메모리 정리 뒤에는 화면 상태를 들고 있던 객체도 새로 만들어지므로 복원할 때마다 새 인스턴스를 쓴다.
        assertInputRetained { screenTestViewModel() }
    }

    @Test
    fun `TC-f905e970-9b7c-43e5-8fb6-529fcf695138 앱이 백그라운드에 다녀와도 입력한 내용을 유지한다`() {
        val (title, description, value) = qrTestFixtureMonkey.qrDetail()
        val viewModel = screenTestViewModel()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                QrAddScreenContent(registry = remember { deniedRegistry() }, viewModel = viewModel)
            }
        }
        composeRule.onTitleInput().performTextInput(title)
        composeRule.onDescriptionInput().performTextInput(description)
        composeRule.onQrTextInput().performTextInput(value)
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe title
        composeRule.descriptionInputText() shouldBe description
        composeRule.qrTextInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-601ce40d-0a4e-4d1b-9e06-596b7c2cb2cf 추가 결과 피드백이 보이던 채 앱을 다시 실행해 진입하면 새 입력 상태로 시작하고 피드백을 표시하지 않는다`() {
        assertRelaunchStartsFresh(
            firstViewModel = effectViewModel(effect = QrAddEffect.AddSucceeded),
            message = KOREAN_ADD_SUCCEEDED_MESSAGE,
            showMessage = { composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick() },
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-601ce40d-0a4e-4d1b-9e06-596b7c2cb2cf 권한 안내가 보이던 채 앱을 다시 실행해 진입하면 새 입력 상태로 시작하고 안내를 표시하지 않는다`() {
        assertRelaunchStartsFresh(
            firstViewModel = screenTestViewModel(),
            message = KOREAN_PERMISSION_DENIED_MESSAGE,
            showMessage = { composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick() },
        )
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
                    componentVisibleProvider = { QrAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.onTitleInput().performTextInput(title)
        composeRule.onDescriptionInput().performTextInput(description)
        composeRule.onQrTextInput().performTextInput(value)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe title
        composeRule.descriptionInputText() shouldBe description
        composeRule.qrTextInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키와 새 인스턴스로 화면을 다시 구성한다.
    private fun assertRelaunchStartsFresh(
        firstViewModel: QrAddViewModel,
        message: String,
        showMessage: () -> Unit,
    ) {
        val (title, description, value) = qrTestFixtureMonkey.qrDetail()
        val registry = deniedRegistry()
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount) {
                val viewModel = remember { if (launchCount == 0) firstViewModel else screenTestViewModel() }

                QrAddScreenContent(registry = registry, viewModel = viewModel)
            }
        }
        composeRule.onTitleInput().performTextInput(title)
        composeRule.onDescriptionInput().performTextInput(description)
        composeRule.onQrTextInput().performTextInput(value)
        composeRule.waitForIdle()
        showMessage()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(message).assertExists()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertDoesNotExist()
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.qrTextInputText() shouldBe ""
    }

    @Composable
    private fun QrAddScreenContent(
        registry: ActivityResultRegistry,
        viewModel: QrAddViewModel,
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        DiaryTheme {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                QrAddScreen(
                    navigateUp = {},
                    navigateToScan = {},
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = remember { ResultEventBus() },
                    componentVisibleProvider = { QrAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
    }

    // 카메라 권한 요청을 시작하면 사용자가 거부한 것처럼 바로 응답한다.
    private fun deniedRegistry(): ActivityResultRegistry {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } answers {
            registry.dispatchResult(firstArg<Int>(), mapOf(Manifest.permission.CAMERA to false))
        }

        return registry
    }

    private companion object {
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        const val KOREAN_SCAN_DESCRIPTION = "QR 스캔"
        const val KOREAN_ADD_SUCCEEDED_MESSAGE = "QR이 추가되었습니다."
        const val KOREAN_PERMISSION_DENIED_MESSAGE = "카메라 권한을 허용해야 QR을 스캔할 수 있습니다."
    }
}
