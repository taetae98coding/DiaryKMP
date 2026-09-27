package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileNotSelectedException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.usecase.FindFileUploadSourceUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileScreenUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileScreenUseCase
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

internal val fileAddFixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal const val FILE_ADD_TITLE = "파일 추가"
internal const val UPLOAD_BUTTON_DESCRIPTION = "파일 올리기"
internal const val NAVIGATE_UP_DESCRIPTION = "뒤로가기"
internal const val NO_FILE_SELECTED = "고른 파일 없음"
internal const val CHOOSE_FILE = "파일 고르기"
internal const val TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
internal const val FILE_NOT_SELECTED_MESSAGE = "파일을 골라 주세요."
internal const val FILE_UNREADABLE_MESSAGE = "파일을 읽지 못했습니다."
internal const val TOO_LARGE_MESSAGE = "50MB 이하 파일만 올릴 수 있습니다."
internal const val SUCCEEDED_MESSAGE = "파일을 올렸습니다."
internal const val FAILED_MESSAGE = "파일을 올리지 못했습니다."
internal const val INPUT_TAB_DESCRIPTION = "입력"
internal const val PREVIEW_TAB_DESCRIPTION = "미리보기"

private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1

// 올리기의 상태와 결과는 mock이 돌려주는 흐름으로, 요청이 받아들여진 뒤의 상태는 요청 stub의 응답으로 제어한다.
internal class FileAddMocks {
    val state = MutableStateFlow<FileUploadState>(FileUploadState.Idle)
    val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fileAddFixtureMonkey.giveMeOne<Account.User>()))
    private val eventChannel = Channel<FileUploadEvent>(Channel.UNLIMITED)
    private val sourceMap = mutableMapOf<FileUri, Result<FileUploadSource>>()
    val requestList = mutableListOf<RequestFileUploadUseCase.Parameter>()

    // 요청의 성립 조건 판단은 domain이 소유하므로, 여기서는 그 결과만 흉내 낸다.
    var requestResult: (RequestFileUploadUseCase.Parameter) -> Result<Unit> = { parameter ->
        when {
            parameter.title.isBlank() -> Result.failure(FileTitleBlankException())
            parameter.uri == null -> Result.failure(FileNotSelectedException())
            else -> Result.success(Unit)
        }
    }

    val findUseCase: FindFileUploadSourceUseCase =
        mockk<FindFileUploadSourceUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } answers { sourceMap.getValue(firstArg()) }
        }

    val requestUseCase: RequestFileUploadUseCase =
        mockk<RequestFileUploadUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } answers {
                val parameter = firstArg<RequestFileUploadUseCase.Parameter>()

                requestList += parameter
                requestResult(parameter).onSuccess { state.value = FileUploadState.Uploading(percent = null) }
            }
        }

    fun register(
        uri: FileUri,
        result: Result<FileUploadSource>,
    ) {
        sourceMap[uri] = result
    }

    fun finish(event: FileUploadEvent) {
        state.value = FileUploadState.Idle
        eventChannel.trySend(event)
    }

    fun finishWithoutEvent() {
        state.value = FileUploadState.Idle
    }

    fun viewModel(): FileAddViewModel {
        val getStateUseCase = mockk<GetFileUploadStateUseCase>()
        every { getStateUseCase(parameter = Unit) } returns state.map { value -> Result.success(value) }
        val getEventUseCase = mockk<GetFileUploadEventUseCase>()
        every { getEventUseCase(parameter = FileScreen.ADD) } returns eventChannel.receiveAsFlow().map { event -> Result.success(event) }

        return FileAddViewModel(
            findFileUploadSourceUseCase = findUseCase,
            requestFileUploadUseCase = requestUseCase,
            startViewingFileScreenUseCase = mockk<StartViewingFileScreenUseCase>(relaxed = true),
            stopViewingFileScreenUseCase = mockk<StopViewingFileScreenUseCase>(relaxed = true),
            getFileUploadStateUseCase = getStateUseCase,
            getFileUploadEventUseCase = getEventUseCase,
        )
    }

    fun accountViewModel(): FileAddAccountViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return FileAddAccountViewModel(getAccountUseCase = getAccountUseCase)
    }
}

internal class FileAddTestScreen(
    val mocks: FileAddMocks = FileAddMocks(),
    val viewModel: FileAddViewModel = mocks.viewModel(),
    private val accountViewModel: FileAddAccountViewModel = mocks.accountViewModel(),
) {
    // 파일 선택 도구가 돌려줄 파일. null이면 고르지 않고 닫는다.
    var pickedUri: FileUri? = null

    val picker: FilePicker =
        mockk<FilePicker>().also { picker ->
            every { picker.open() } answers { pickedUri?.let(viewModel::select) }
        }

    var navigateUpCount: Int = 0
        private set

    @Composable
    fun Content() {
        DiaryTheme {
            FileAddScreen(
                navigateUp = { navigateUpCount += 1 },
                filePicker = picker,
                viewModel = viewModel,
                accountViewModel = accountViewModel,
            )
        }
    }

    fun pick(source: FileUploadSource) {
        mocks.register(uri = source.uri, result = Result.success(source))
        pickedUri = source.uri
    }
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.clickUpload() {
    onNodeWithContentDescription(UPLOAD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickChooseFile() {
    onNodeWithContentDescription(CHOOSE_FILE).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.snackbarCount(message: String): Int = onAllNodesWithText(message).fetchSemanticsNodes().size
