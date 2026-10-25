package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private const val FILE_NAME_A = "a.txt"
private const val FILE_NAME_B = "b.txt"
private const val ACCOUNT_FAILURE_MESSAGE = "account"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class FileAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var currentScreen by mutableStateOf<FileAddTestScreen?>(null)
    private var isContentSet = false

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-b2c327f8-dfbf-4980-a61a-2ac6582903b6 처음 진입하면 비어 있는 입력으로 시작하고 제목에 초점을 둔다`() {
        setFileAddScreen()

        composeRule.onNodeWithText(FILE_ADD_TITLE).assertExists()
        composeRule.titleInput().assert(hasText("")).assertIsFocused()
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
    }

    @Test
    fun `TC-0751a03c-734b-4264-adcb-ece99ed4ed1b 파일을 고르면 이름과 크기를 표시한다`() {
        mapOf(
            "보고서.pdf" to (1_024L to "1.0 KB"),
            "빈 파일.txt" to (0L to "0 B"),
        ).forEach { (name, sizeToText) ->
            val screen = FileAddTestScreen()
            val source = fileAddFixtureMonkey.fileUploadSource(size = sizeToText.first, name = name)
            screen.pick(source = source)
            setFileAddScreen(screen = screen)

            composeRule.clickChooseFile()

            composeRule.onNode(hasText(name).and(hasText(sizeToText.second))).assertExists()
            composeRule.onNodeWithText(NO_FILE_SELECTED).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-0c908f3c-7a9d-4a76-82c6-2c23c2dbc398 이미 고른 파일이 있어도 다시 고르면 새 파일로 바뀐다`() {
        val screen = FileAddTestScreen()
        val first = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A)
        val second = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_B)
        setFileAddScreen(screen = screen)

        screen.pick(source = first)
        composeRule.clickChooseFile()
        screen.pick(source = second)
        composeRule.clickChooseFile()

        composeRule.onNodeWithText(FILE_NAME_B).assertExists()
        composeRule.onNodeWithText(FILE_NAME_A).assertDoesNotExist()
    }

    @Test
    fun `TC-07e85d14-736c-44dc-b1bf-c808dd89738f 파일을 고르지 않고 선택 도구를 닫으면 고른 파일이 바뀌지 않는다`() {
        val screen = FileAddTestScreen()
        setFileAddScreen(screen = screen)
        composeRule.clickChooseFile()
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()

        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
        composeRule.clickChooseFile()
        screen.pickedUri = null
        composeRule.clickChooseFile()

        verify(exactly = 3) { screen.picker.open() }
        composeRule.onNodeWithText(FILE_NAME_A).assertExists()
        listOf(FILE_UNREADABLE_MESSAGE, TOO_LARGE_MESSAGE).forEach { message -> composeRule.snackbarCount(message) shouldBe 0 }
    }

    @Test
    fun `TC-c89e24fb-6233-4119-80c9-9444a789c6d6 고를 수 없는 파일을 고르면 이유를 알리고 이전 선택을 둔다`() {
        val screen = FileAddTestScreen()
        setFileAddScreen(screen = screen)
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
        composeRule.clickChooseFile()

        mapOf(
            FileUnreadableException(name = "", cause = IllegalStateException("read")) to FILE_UNREADABLE_MESSAGE,
            FileTooLargeException() to TOO_LARGE_MESSAGE,
        ).forEach { (exception, message) ->
            val uri = FileUri("file:///${fileAddFixtureMonkey.giveMeOne<Int>()}")
            screen.mocks.register(uri = uri, result = Result.failure(exception))
            screen.pickedUri = uri
            composeRule.clickChooseFile()

            composeRule.onNodeWithText(message).assertExists()
            composeRule.onNodeWithText(FILE_NAME_A).assertExists()
        }
    }

    @Test
    fun `TC-13958209-1811-4ea0-b20a-56f57c8a9f75 제목 없이 올리기를 실행하면 제목을 요청하고 초점을 옮긴다`() {
        listOf("", "  ").forEach { title ->
            val screen = FileAddTestScreen()
            screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
            setFileAddScreen(screen = screen)
            composeRule.titleInput().performTextInput(title)
            composeRule.descriptionInput().performTextInput(DESCRIPTION)
            composeRule.clickChooseFile()
            composeRule.descriptionInput().performClick()

            composeRule.clickUpload()

            composeRule.onNodeWithText(TITLE_BLANK_MESSAGE).assertExists()
            composeRule.titleInput().assert(hasText(title)).assertIsFocused()
            composeRule.descriptionInput().assert(hasText(DESCRIPTION))
            composeRule.onNodeWithText(FILE_NAME_A).assertExists()
            screen.mocks.state.value shouldBe FileUploadState.Idle
        }
    }

    @Test
    fun `TC-3dc2f4fc-8d3e-4a5b-9348-d5c731e2e72e 파일 없이 올리기를 실행하면 파일을 요청한다`() {
        val screen = FileAddTestScreen()
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput(DESCRIPTION)

        composeRule.clickUpload()

        composeRule.onNodeWithText(FILE_NOT_SELECTED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(TITLE))
        composeRule.descriptionInput().assert(hasText(DESCRIPTION))
        screen.mocks.state.value shouldBe FileUploadState.Idle
    }

    @Test
    fun `TC-e9ed04a3-cd3b-48fd-900c-a41bcca35a4a 제목도 파일도 없으면 제목을 먼저 요청한다`() {
        setFileAddScreen()

        composeRule.clickUpload()

        composeRule.onNodeWithText(TITLE_BLANK_MESSAGE).assertExists()
        composeRule.onNodeWithText(FILE_NOT_SELECTED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-4c8c007d-7359-4fa6-9caf-57f28b16a319 올리기를 시작하면 입력을 비우고 제목에 초점을 옮기며 화면을 유지한다`() {
        val screen = FileAddTestScreen()
        val source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A)
        screen.pick(source = source)
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput(DESCRIPTION)
        composeRule.clickChooseFile()
        composeRule.descriptionInput().performClick()

        composeRule.clickUpload()

        screen.mocks.requestList shouldBe listOf(RequestFileUploadUseCase.Parameter(uri = source.uri, title = TITLE, description = DESCRIPTION))
        screen.navigateUpCount shouldBe 0
        composeRule.titleInput().assert(hasText("")).assertIsFocused()
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
        listOf(TITLE_BLANK_MESSAGE, FILE_NOT_SELECTED_MESSAGE, SUCCEEDED_MESSAGE, FAILED_MESSAGE).forEach { message -> composeRule.snackbarCount(message) shouldBe 0 }
    }

    @Test
    fun `TC-ba87c78b-1adf-4f51-9ad1-f0b76eeb699c 설명을 미리보기로 보던 중 올리기를 시작하면 설명 입력 상태로 돌아간다`() {
        val screen = FileAddTestScreen()
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput("# 메모")
        composeRule.clickChooseFile()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()

        composeRule.clickUpload()

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
        composeRule.descriptionInput().assert(hasText(""))
    }

    @Test
    fun `TC-50400477-91a5-4e1e-ad53-9b488e23801c 올리는 중에는 진행 표시를 보이고 올리기를 실행할 수 없다`() {
        listOf(true, false).forEach { isStartedHere ->
            val screen = FileAddTestScreen()
            if (!isStartedHere) screen.mocks.state.value = FileUploadState.Uploading(percent = null)
            setFileAddScreen(screen = screen)
            if (isStartedHere) {
                screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
                composeRule.clickChooseFile()
                composeRule.titleInput().performTextInput(TITLE)
                composeRule.clickUpload()
            }
            val requestCountBefore = screen.mocks.requestList.size
            screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_B))
            composeRule.clickChooseFile()
            composeRule.titleInput().performTextInput(NEXT_TITLE)

            composeRule.clickUpload()

            uploadProgress().assertExists()
            screen.mocks.requestList.size shouldBe requestCountBefore
            composeRule.titleInput().assert(hasText(NEXT_TITLE))
            composeRule.onNodeWithText(FILE_NAME_B).assertExists()
        }
    }

    @Test
    fun `TC-2bdb00f3-e9db-459e-a33a-82f72bcf4a92 올리는 중에도 입력하고 파일을 고를 수 있다`() {
        val screen = FileAddTestScreen()
        screen.mocks.state.value = FileUploadState.Uploading(percent = null)
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_B))
        setFileAddScreen(screen = screen)

        composeRule.titleInput().performTextInput(NEXT_TITLE)
        composeRule.clickChooseFile()

        composeRule.titleInput().assert(hasText(NEXT_TITLE))
        composeRule.onNodeWithText(FILE_NAME_B).assertExists()
    }

    @Test
    fun `TC-f33a3f46-e611-4fd6-b6a4-453ca777497a 화면을 보고 있는 동안 올리기가 끝나면 결과를 알리고 적어 둔 내용은 둔다`() {
        mapOf(
            FileUploadEvent.Succeeded(fileId = fileAddFixtureMonkey.giveMeOne<Uuid>()) to SUCCEEDED_MESSAGE,
            FileUploadEvent.TooLarge to TOO_LARGE_MESSAGE,
            FileUploadEvent.Failed to FAILED_MESSAGE,
        ).forEach { (event, message) ->
            val screen = FileAddTestScreen()
            screen.mocks.state.value = FileUploadState.Uploading(percent = null)
            screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_B))
            setFileAddScreen(screen = screen)
            composeRule.titleInput().performTextInput(NEXT_TITLE)
            composeRule.clickChooseFile()

            composeRule.runOnIdle { screen.mocks.finish(event = event) }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(message).assertExists()
            uploadProgress().assertDoesNotExist()
            composeRule.titleInput().assert(hasText(NEXT_TITLE))
            composeRule.onNodeWithText(FILE_NAME_B).assertExists()
        }
    }

    @Test
    fun `TC-47674d67-ab95-4534-bfb2-97d394f22ecf 올리는 동안 계정이 바뀌어 중단되면 결과를 알리지 않는다`() {
        val screen = FileAddTestScreen()
        screen.mocks.state.value = FileUploadState.Uploading(percent = null)
        setFileAddScreen(screen = screen)
        uploadProgress().assertExists()

        composeRule.runOnIdle {
            screen.mocks.accountFlow.value = Result.success(fileAddFixtureMonkey.giveMeOne<Account.User>())
            screen.mocks.finishWithoutEvent()
        }
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        listOf(SUCCEEDED_MESSAGE, TOO_LARGE_MESSAGE, FAILED_MESSAGE).forEach { message -> composeRule.snackbarCount(message) shouldBe 0 }
    }

    @Test
    fun `TC-ce898c39-e03d-472d-9099-57ac371ab343 게스트가 되면 이전 화면으로 돌아간다`() {
        val screen = FileAddTestScreen()
        setFileAddScreen(screen = screen)

        composeRule.runOnIdle { screen.mocks.accountFlow.value = Result.success(Account.Guest) }
        composeRule.waitForIdle()

        screen.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-ce898c39-e03d-472d-9099-57ac371ab343 다른 사용자가 되거나 계정을 확정하지 않으면 화면과 적어 둔 내용을 유지한다`() {
        listOf(
            Result.success(fileAddFixtureMonkey.giveMeOne<Account.User>()),
            Result.failure(IllegalStateException(ACCOUNT_FAILURE_MESSAGE)),
        ).forEach { account ->
            val screen = FileAddTestScreen()
            screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
            setFileAddScreen(screen = screen)
            composeRule.titleInput().performTextInput(TITLE)
            composeRule.clickChooseFile()

            composeRule.runOnIdle { screen.mocks.accountFlow.value = account }
            composeRule.waitForIdle()

            screen.navigateUpCount shouldBe 0
            composeRule.titleInput().assert(hasText(TITLE))
            composeRule.onNodeWithText(FILE_NAME_A).assertExists()
        }
    }

    @Test
    fun `TC-a1099bb4-240a-42f7-9476-8fc329821611 계정을 확정하지 않은 동안 올리기를 실행하면 올리지 못했다고 알린다`() {
        val screen = FileAddTestScreen()
        screen.mocks.accountFlow.value = Result.failure(IllegalStateException(ACCOUNT_FAILURE_MESSAGE))
        screen.mocks.requestResult = { Result.failure(IllegalStateException(ACCOUNT_FAILURE_MESSAGE)) }
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.clickChooseFile()

        composeRule.clickUpload()

        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(TITLE))
        composeRule.onNodeWithText(FILE_NAME_A).assertExists()
        uploadProgress().assertDoesNotExist()
    }

    @Test
    fun `TC-26553d6e-f05b-4921-89da-6f9eaaee0fc5 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        listOf(FileUploadState.Idle, FileUploadState.Uploading(percent = null)).forEach { state ->
            val screen = FileAddTestScreen()
            screen.mocks.state.value = state
            setFileAddScreen(screen = screen)

            composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            screen.navigateUpCount shouldBe 1
        }
    }

    @Test
    fun `TC-4828e410-703c-4644-a5aa-6bc44c948b6b Android에서 붙들어 둘 수 없는 파일을 올리면 올리지 못했다고 알린다`() {
        val screen = FileAddTestScreen()
        screen.mocks.requestResult = { Result.failure(SecurityException("persist")) }
        screen.pick(source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A))
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.clickChooseFile()

        composeRule.clickUpload()

        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
        uploadProgress().assertDoesNotExist()
    }

    @Test
    fun `TC-a25d66ad-88de-44be-a1e9-eac0dee184fe 제목과 설명을 입력한 그대로 보내고 설명이 없으면 빈 설명으로 보낸다`() {
        mapOf(
            " 회의록 " to "첫 줄\n둘째 줄",
            "회의록" to "",
        ).forEach { (title, description) ->
            val screen = FileAddTestScreen()
            val source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A)
            screen.pick(source = source)
            setFileAddScreen(screen = screen)
            composeRule.titleInput().performTextInput(title)
            if (description.isNotEmpty()) composeRule.descriptionInput().performTextInput(description)
            composeRule.clickChooseFile()

            composeRule.clickUpload()

            screen.mocks.requestList shouldBe listOf(RequestFileUploadUseCase.Parameter(uri = source.uri, title = title, description = description))
        }
    }

    @Test
    fun `TC-97f6812a-c3ef-465e-a7be-ec026dfa4fbe 올리기를 시작한 뒤 입력을 바꿔도 시작할 때의 제목과 설명으로 올린다`() {
        val screen = FileAddTestScreen()
        val source = fileAddFixtureMonkey.fileUploadSource(name = FILE_NAME_A)
        screen.pick(source = source)
        setFileAddScreen(screen = screen)
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput(DESCRIPTION)
        composeRule.clickChooseFile()
        composeRule.clickUpload()

        composeRule.titleInput().performTextInput("다른 제목")
        composeRule.waitForIdle()

        screen.mocks.requestList shouldBe listOf(RequestFileUploadUseCase.Parameter(uri = source.uri, title = TITLE, description = DESCRIPTION))
    }

    // 한 테스트에서 여러 데이터를 순회할 수 있게, 처음에만 내용을 두고 이후에는 화면을 새로 바꿔 넣는다.
    private fun setFileAddScreen(screen: FileAddTestScreen = FileAddTestScreen()): FileAddTestScreen {
        if (isContentSet) {
            composeRule.runOnIdle { currentScreen = screen }
        } else {
            currentScreen = screen
            composeRule.setContent { currentScreen?.let { value -> key(value) { value.Content() } } }
            isContentSet = true
        }
        composeRule.waitForIdle()

        return screen
    }

    private fun uploadProgress() =
        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(UPLOAD_BUTTON_DESCRIPTION))),
            useUnmergedTree = true,
        )

    private companion object {
        private const val TITLE = "회의록"
        private const val NEXT_TITLE = "다음"
        private const val DESCRIPTION = "메모"
    }
}
