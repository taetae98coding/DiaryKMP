package io.github.taetae98coding.diary.feature.file.ui.detail

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
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.testing.file.nonBlankDiaryFile
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class FileDetailScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-3c33f9db-bb3b-447e-8109-25ac4c34aad4 화면이 재생성되어도 파일 정보와 입력을 유지하고 다시 불러오지 않는다`() {
        val newTitle = prefixedValue(prefix = TITLE_PREFIX)
        val newDescription = prefixedValue(prefix = DESCRIPTION_PREFIX)
        val newName = prefixedValue(prefix = NAME_PREFIX)
        val screen = FileDetailTestScreen()
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        fillForm(title = newTitle, description = newDescription, name = newName)
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(newTitle))
        composeRule.descriptionInput().assert(hasText(newDescription))
        composeRule.nameInput().assert(hasText(newName))
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { screen.mocks.fetchUseCase(parameter = screen.mocks.file.id) }
    }

    @Test
    fun `TC-3c33f9db-bb3b-447e-8109-25ac4c34aad4 앱이 백그라운드에 갔다가 돌아와도 파일 정보와 입력을 유지하고 다시 불러오지 않는다`() {
        val newTitle = prefixedValue(prefix = TITLE_PREFIX)
        val newDescription = prefixedValue(prefix = DESCRIPTION_PREFIX)
        val newName = prefixedValue(prefix = NAME_PREFIX)
        val screen = FileDetailTestScreen()
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        fillForm(title = newTitle, description = newDescription, name = newName)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.waitForIdle()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(newTitle))
        composeRule.descriptionInput().assert(hasText(newDescription))
        composeRule.nameInput().assert(hasText(newName))
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { screen.mocks.fetchUseCase(parameter = screen.mocks.file.id) }
    }

    @Test
    fun `TC-1d35a91d-33e0-444d-a845-c66a87674bd6 진행 중인 수정과 다운로드는 화면이 재생성되어도 이어지고 결과가 보인다`() {
        val newTitle = prefixedValue(prefix = TITLE_PREFIX)
        val screen = FileDetailTestScreen()
        val updateResponse = CompletableDeferred<Unit>()
        val downloadResponse = CompletableDeferred<Unit>()
        val updateAnswer = screen.mocks.updateAnswer
        screen.mocks.updateAnswer = { parameter ->
            updateResponse.await()
            updateAnswer(parameter)
        }
        screen.mocks.downloadAnswer = {
            downloadResponse.await()
            Result.failure(IllegalStateException(FAILURE_MESSAGE))
        }
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()
        composeRule.clickDownload()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.progressIn(UPDATE_DESCRIPTION).assertExists()
        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertExists()

        composeRule.runOnIdle { updateResponse.complete(Unit) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertExists()

        composeRule.runOnIdle { downloadResponse.complete(Unit) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DOWNLOAD_FAILED).assertExists()
    }

    @Test
    fun `TC-01b1ffb1-c07e-4123-a10e-208093cf0131 시스템이 앱을 정리한 뒤 복원하면 다시 불러오고 입력은 복원한다`() {
        val file = fileDetailFixtureMonkey.nonBlankDiaryFile().copy(title = DOCUMENTED_OLD_TITLE)
        // 시스템이 앱을 정리하면 ViewModel도 새로 만들어지므로, 복원하는 순간 새 화면 상태로 바꾼다.
        var screen by mutableStateOf(FileDetailTestScreen(mocks = FileDetailMocks(file = file)))
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        composeRule.titleInput().performTextReplacement(DOCUMENTED_NEW_TITLE)
        composeRule.waitForIdle()
        val restored = FileDetailTestScreen(mocks = FileDetailMocks(file = file))
        screen = restored
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { restored.mocks.fetchUseCase(parameter = file.id) }
        composeRule.titleInput().assert(hasText(DOCUMENTED_NEW_TITLE))
        composeRule.onNodeWithText(DOCUMENTED_OLD_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0a93fd60-7169-4b9b-ac96-8476db338d29 화면을 떠났다가 다시 들어오면 새로 불러온 저장된 값으로 시작한다`() {
        val newTitle = prefixedValue(prefix = TITLE_PREFIX)
        val newDescription = prefixedValue(prefix = DESCRIPTION_PREFIX)
        val newName = prefixedValue(prefix = NAME_PREFIX)
        val file = fileDetailFixtureMonkey.nonBlankDiaryFile()
        var screen by mutableStateOf(FileDetailTestScreen(mocks = FileDetailMocks(file = file)))

        composeRule.setContent { key(screen) { screen.Content() } }
        composeRule.waitForIdle()
        fillForm(title = newTitle, description = newDescription, name = newName)
        val reentered = FileDetailTestScreen(mocks = FileDetailMocks(file = file))
        composeRule.runOnIdle { screen = reentered }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { reentered.mocks.fetchUseCase(parameter = file.id) }
        composeRule.titleInput().assert(hasText(file.title))
        composeRule.descriptionInput().assert(hasText(file.description))
        composeRule.nameInput().assert(hasText(file.name))
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertDoesNotExist()
        (screen === reentered) shouldBe true
    }

    private fun prefixedValue(prefix: String): String = prefix + fileDetailFixtureMonkey.nonBlankString()

    private fun fillForm(
        title: String,
        description: String,
        name: String,
    ) {
        composeRule.titleInput().performTextReplacement(title)
        composeRule.descriptionInput().performTextReplacement(description)
        composeRule.nameInput().performTextReplacement(name)
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private companion object {
        // 제목, 설명, 이름에 같은 값이 들어가 서로 구분하지 못하는 일이 없게 앞에 붙인다.
        private const val TITLE_PREFIX = "title-"
        private const val DESCRIPTION_PREFIX = "description-"
        private const val NAME_PREFIX = "name-"

        private const val DOCUMENTED_OLD_TITLE = "옛 제목"
        private const val DOCUMENTED_NEW_TITLE = "새 제목"
    }
}
