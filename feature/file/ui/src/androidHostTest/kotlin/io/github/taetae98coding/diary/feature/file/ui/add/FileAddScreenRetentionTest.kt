package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class FileAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-003 화면이 재생성되어도 입력과 설명 미리보기 상태와 고른 파일을 유지한다`() {
        val screen = FileAddTestScreen()
        val tester = StateRestorationTester(composeRule)
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME))

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        fillForm()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFormKept(isFileKept = true)
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-003 앱이 백그라운드에 다녀와도 입력과 설명 미리보기 상태와 고른 파일을 유지한다`() {
        val screen = FileAddTestScreen()
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME))

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        fillForm()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertFormKept(isFileKept = true)
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-004 시스템이 앱을 정리했다가 복원하면 제목과 설명만 되살린다`() {
        // 시스템이 앱을 정리하면 ViewModel도 새로 만들어지므로, 복원하는 순간 새 화면 상태로 바꾼다.
        var screen by mutableStateOf(FileAddTestScreen())
        val tester = StateRestorationTester(composeRule)
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME))

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        fillForm()
        screen = FileAddTestScreen()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFormKept(isFileKept = false)
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-005 파일 선택 도구가 열린 동안 화면이 회전해 다시 만들어져도 고른 파일이 고른 파일이 된다`() {
        assertPickedAfterRecreation(isProcessRecreated = false)
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-005 파일 선택 도구가 열린 동안 시스템이 앱을 정리했다가 다시 만들어도 고른 파일이 고른 파일이 된다`() {
        assertPickedAfterRecreation(isProcessRecreated = true)
    }

    @Test
    fun `TC-FILE-ADD-DOMAIN-006 화면을 떠났다가 다시 들어오면 처음 상태로 시작한다`() {
        var screen by mutableStateOf(FileAddTestScreen())
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME))

        composeRule.setContent { key(screen) { screen.Content() } }
        composeRule.waitForIdle()
        fillForm()
        composeRule.runOnIdle { screen = FileAddTestScreen() }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
    }

    // 선택 도구의 결과는 다시 만들어진 화면의 onPick으로 전달된다. 전달 자체는 플랫폼 선택 도구 테스트가 확인한다.
    private fun assertPickedAfterRecreation(isProcessRecreated: Boolean) {
        val source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME)
        var screen by mutableStateOf(FileAddTestScreen())
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.clickChooseFile()
        if (isProcessRecreated) screen = FileAddTestScreen(mocks = screen.mocks)
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        screen.pick(source = source)
        composeRule.runOnIdle { checkNotNull(screen.pickedUri).let(screen.viewModel::select) }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TITLE))
        composeRule.onNodeWithText(FILE_NAME).assertExists()
    }

    private fun fillForm() {
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput(DESCRIPTION)
        composeRule.clickChooseFile()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun assertFormKept(isFileKept: Boolean) {
        composeRule.titleInput().assert(hasText(TITLE))
        composeRule.descriptionInput().assert(hasText(DESCRIPTION))
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        if (isFileKept) {
            composeRule.onNodeWithText(FILE_NAME).assertExists()
        } else {
            composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
        }
    }

    private companion object {
        private const val TITLE = "회의록"
        private const val DESCRIPTION = "메모"
        private const val FILE_NAME = "a.txt"
    }
}
