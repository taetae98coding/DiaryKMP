package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.testing.file.fileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPicker
import io.github.taetae98coding.diary.feature.setting.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class SettingFileScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-792f8c12-646b-4778-b076-60d8c401f617 현재 위치를 선택하면 폴더 선택 도구를 연다`() {
        val directoryPicker = mockk<DirectoryPicker>(relaxed = true)
        setSettingFileScreen(viewModel = viewModel(directory = null), directoryPicker = directoryPicker)

        composeRule.onNodeWithText("기본 위치").performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { directoryPicker.open() }
    }

    @Test
    fun `TC-cef9e1ad-cca8-4668-b96c-e2124fbc09d7 기본 위치로 되돌리기를 실행하면 되돌리기를 요청한다`() {
        val viewModel = viewModel(directory = Arb.fileDownloadDirectory().next())
        setSettingFileScreen(viewModel = viewModel)

        composeRule.onNodeWithText("기본 위치로 되돌리기").performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.unsetDirectory() }
    }

    @Test
    fun `TC-41fd402c-68d3-4b41-9366-c2f9bb19d9ca TC-3a05cdca-74ef-4bb0-8d41-247f33ec83d2 위치를 바꾸지 못하면 실패를 알린다`() {
        setSettingFileScreen(viewModel = viewModel(directory = null, effect = flowOf(SettingFileEffect.ChangeFailed)))

        composeRule.onNodeWithText("다운로드 위치를 바꾸지 못했습니다.").assertExists()
    }

    @Test
    fun `TC-816e8b89-d6dc-49d7-a87f-47342ad9d4e5 뒤로가기 동작을 선택하면 이전 화면으로 돌아가고 설정은 바뀌지 않는다`() {
        var navigateUpCount = 0
        val viewModel = viewModel(directory = Arb.fileDownloadDirectory().next())
        setSettingFileScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription("뒤로가기").performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.setDirectory(uri = any()) }
        verify(exactly = 0) { viewModel.unsetDirectory() }
    }

    private fun setSettingFileScreen(
        viewModel: SettingFileViewModel,
        directoryPicker: DirectoryPicker = mockk(relaxed = true),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingFileScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { SettingFileScaffoldComponentVisible() },
                    directoryPicker = directoryPicker,
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun viewModel(
        directory: FileDownloadDirectory?,
        effect: Flow<SettingFileEffect> = emptyFlow(),
    ): SettingFileViewModel {
        val viewModel = mockk<SettingFileViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(SettingFileUiState.Content(directory = directory))
        every { viewModel.effect } returns effect
        return viewModel
    }
}
