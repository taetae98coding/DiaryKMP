package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.testing.file.fileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileScreen
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileUiState
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileViewModel
import io.github.taetae98coding.diary.feature.setting.ui.resetAndroidUiDispatcher
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidDirectoryPickerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-4a16856d-4ec7-44bf-a4bf-d11d437a63b5 선택 도구가 열린 동안 화면이 재생성되어도 고른 폴더를 다시 만들어진 화면에서 저장한다`() {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/${Arb.int().next()}")
        val harness = ActivityResultRegistryHarness()
        val viewModel = viewModel()
        val tester = StateRestorationTester(composeRule)

        tester.setContent { SettingFileContent(harness = harness, viewModel = viewModel) }
        openPicker()
        tester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { harness.registry.dispatchResult(harness.launchedRequestCode.shouldNotBeNull(), uri) }
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.setDirectory(uri = uri.toString()) }
    }

    @Test
    fun `TC-4a16856d-4ec7-44bf-a4bf-d11d437a63b5 선택 도구가 열린 동안 시스템이 앱을 정리했다가 다시 만들어도 고른 폴더를 다시 만든 화면에서 저장한다`() {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/${Arb.int().next()}")
        var harness = ActivityResultRegistryHarness()
        var viewModel = viewModel()
        val tester = StateRestorationTester(composeRule)

        tester.setContent { SettingFileContent(harness = harness, viewModel = viewModel) }
        openPicker()
        val requestCode = harness.launchedRequestCode.shouldNotBeNull()
        // 시스템이 앱을 정리하면 결과 전달 수단과 ViewModel도 새로 만들어지고, 남겨 둔 상태에서 선택 도구를 연 기록을 되살린다.
        val savedState = Bundle().also { bundle -> harness.registry.onSaveInstanceState(bundle) }
        val discarded = viewModel
        harness = ActivityResultRegistryHarness().also { restored -> restored.registry.onRestoreInstanceState(savedState) }
        viewModel = viewModel()
        tester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { harness.registry.dispatchResult(requestCode, uri) }
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.setDirectory(uri = uri.toString()) }
        verify(exactly = 0) { discarded.setDirectory(uri = any()) }
    }

    @Test
    fun `TC-5a7c116a-9f0a-4126-9c26-1624c7a91034 폴더를 고르지 않고 선택 도구를 닫으면 저장하지 않고 위치가 그대로다`() {
        val harness = ActivityResultRegistryHarness()
        val viewModel = viewModel()

        composeRule.setContent { SettingFileContent(harness = harness, viewModel = viewModel) }
        openPicker()
        composeRule.runOnIdle { harness.registry.dispatchResult(harness.launchedRequestCode.shouldNotBeNull(), null) }
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel.setDirectory(uri = any()) }
        verify(exactly = 0) { viewModel.unsetDirectory() }
        composeRule.onNodeWithText(DIRECTORY_NAME).assertExists()
    }

    // 고른 폴더를 저장으로 잇는 연결은 화면을 등록하는 곳과 같게 둔다.
    @Composable
    private fun SettingFileContent(
        harness: ActivityResultRegistryHarness,
        viewModel: SettingFileViewModel,
    ) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides harness.owner) {
            DiaryTheme {
                SettingFileScreen(
                    navigateUp = {},
                    componentVisibleProvider = { SettingFileScaffoldComponentVisible() },
                    directoryPicker = remember { AndroidDirectoryPickerFactory() }.rememberDirectoryPicker(onPick = viewModel::setDirectory),
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun openPicker() {
        composeRule.onNodeWithText(DIRECTORY_NAME).performClick()
        composeRule.waitForIdle()
    }

    private fun viewModel(): SettingFileViewModel {
        val viewModel = mockk<SettingFileViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(SettingFileUiState.Content(directory = Arb.fileDownloadDirectory().next().copy(name = DIRECTORY_NAME)))
        every { viewModel.effect } returns emptyFlow()
        return viewModel
    }

    private companion object {
        private const val DIRECTORY_NAME = "Documents"
    }
}

// 선택 도구를 실제로 여는 대신 연 기록만 남기고, 결과 전달과 상태 저장·복원은 결과 전달 수단의 실제 동작을 쓴다.
private class ActivityResultRegistryHarness {
    private val requestCodeSlot = slot<Int>()

    val registry: ActivityResultRegistry =
        spyk<ActivityResultRegistry>().also { registry ->
            every { registry.onLaunch(capture(requestCodeSlot), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just Runs
        }

    val owner: ActivityResultRegistryOwner =
        mockk<ActivityResultRegistryOwner>().also { owner -> every { owner.activityResultRegistry } returns registry }

    val launchedRequestCode: Int?
        get() = if (requestCodeSlot.isCaptured) requestCodeSlot.captured else null
}
