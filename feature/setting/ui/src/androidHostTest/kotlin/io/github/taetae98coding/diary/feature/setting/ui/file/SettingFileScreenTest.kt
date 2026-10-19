package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPicker
import io.github.taetae98coding.diary.feature.setting.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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
    fun `TC-SETTING-FILE-FEATURE-005 폴더 고르기를 실행하면 폴더 선택 도구를 연다`() {
        val directoryPicker = mockk<DirectoryPicker>(relaxed = true)
        setSettingFileScreen(viewModel = viewModel(directory = null), directoryPicker = directoryPicker)

        composeRule.onNodeWithText("폴더 고르기").performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { directoryPicker.open() }
    }

    @Test
    fun `TC-SETTING-FILE-FEATURE-009 기본 위치로 되돌리기를 실행하면 되돌리기를 요청한다`() {
        val viewModel = viewModel(directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>())
        setSettingFileScreen(viewModel = viewModel)

        composeRule.onNodeWithText("기본 위치로 되돌리기").performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.unsetDirectory() }
    }

    @Test
    fun `TC-SETTING-FILE-FEATURE-008 TC-SETTING-FILE-FEATURE-010 위치를 바꾸지 못하면 실패를 알린다`() {
        setSettingFileScreen(viewModel = viewModel(directory = null, effect = flowOf(SettingFileEffect.ChangeFailed)))

        composeRule.onNodeWithText("다운로드 위치를 바꾸지 못했습니다.").assertExists()
    }

    private fun setSettingFileScreen(
        viewModel: SettingFileViewModel,
        directoryPicker: DirectoryPicker = mockk(relaxed = true),
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingFileScreen(
                    navigateUp = {},
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
