package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.testing.file.nonBlankDiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileNameBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.usecase.DownloadFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.FetchFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UpdateFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal val fileDetailFixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal const val DOWNLOAD_DESCRIPTION = "다운로드"
internal const val UPDATE_DESCRIPTION = "파일 수정"
internal const val NAVIGATE_UP = "뒤로가기"
internal const val LOADING_DESCRIPTION = "파일 불러오는 중"
internal const val LOAD_FAILED = "파일을 불러오지 못했습니다"
internal const val RETRY = "다시 시도"
internal const val TITLE_BLANK = "제목을 입력해 주세요."
internal const val NAME_BLANK = "파일 이름을 입력해 주세요."
internal const val NAME_TOO_LONG = "파일 이름은 255자까지 입력할 수 있습니다."
internal const val UPDATE_SUCCEEDED = "파일이 수정되었습니다."
internal const val UPDATE_FAILED = "파일을 수정하지 못했습니다."
internal const val DOWNLOAD_DIRECTORY_UNAVAILABLE = "다운로드 위치에 저장하지 못했습니다. 설정에서 다운로드 위치를 확인해 주세요."
internal const val FAILURE_MESSAGE = "failure"
internal const val PREVIEW_TAB_DESCRIPTION = "미리보기"
internal const val DOWNLOAD_FAILED = "파일을 내려받지 못했습니다."

private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1
private const val NAME_INPUT_INDEX = 2

// 서버와 저장 위치의 응답은 mock이 돌려주는 값으로 제어한다. 수정 성립 조건의 판단은 domain이 소유하므로 여기서는 그 결과만 흉내 낸다.
internal class FileDetailMocks(
    file: DiaryFile = fileDetailFixtureMonkey.nonBlankDiaryFile(),
) {
    var file: DiaryFile = file
        private set

    val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fileDetailFixtureMonkey.giveMeOne<Account.User>()))
    val updateList = mutableListOf<UpdateFileUseCase.Parameter>()
    val downloadList = mutableListOf<DiaryFile>()

    var fetchAnswer: suspend () -> Result<DiaryFile> = { Result.success(this.file) }
    var updateAnswer: suspend (UpdateFileUseCase.Parameter) -> Result<DiaryFile> = { parameter ->
        when {
            accountFlow.value.isFailure -> Result.failure(IllegalStateException("account"))
            parameter.detail.title.isBlank() -> Result.failure(FileTitleBlankException())
            parameter.detail.name.isBlank() -> Result.failure(FileNameBlankException())
            else -> Result.success(this.file.copy(name = parameter.detail.name, title = parameter.detail.title, description = parameter.detail.description))
        }
    }
    var downloadAnswer: suspend (DiaryFile) -> Result<FileDownloadResult> = { target ->
        if (accountFlow.value.isFailure) Result.failure(IllegalStateException("account")) else Result.success(FileDownloadResult.Saved(name = target.name))
    }

    val fetchUseCase: FetchFileUseCase =
        mockk<FetchFileUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } coAnswers { fetchAnswer() }
        }

    val updateUseCase: UpdateFileUseCase =
        mockk<UpdateFileUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } coAnswers {
                val parameter = firstArg<UpdateFileUseCase.Parameter>()

                updateList += parameter
                updateAnswer(parameter)
            }
        }

    val downloadUseCase: DownloadFileUseCase =
        mockk<DownloadFileUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } coAnswers {
                val target = firstArg<DiaryFile>()

                downloadList += target
                downloadAnswer(target)
            }
        }

    fun viewModel(): FileDetailViewModel =
        FileDetailViewModel(
            id = file.id,
            fetchFileUseCase = fetchUseCase,
            updateFileUseCase = updateUseCase,
            downloadFileUseCase = downloadUseCase,
        )

    fun accountViewModel(): FileDetailAccountViewModel {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        return FileDetailAccountViewModel(getAccountUseCase = getAccountUseCase)
    }

    fun changeSavedFile(file: DiaryFile) {
        this.file = file
    }
}

internal class FileDetailTestScreen(
    val mocks: FileDetailMocks = FileDetailMocks(),
    val viewModel: FileDetailViewModel = mocks.viewModel(),
    private val accountViewModel: FileDetailAccountViewModel = mocks.accountViewModel(),
) {
    var navigateUpCount: Int = 0
        private set

    @Composable
    fun Content() {
        DiaryTheme {
            FileDetailScreen(
                navigateUp = { navigateUpCount += 1 },
                componentVisibleProvider = { FileDetailScaffoldComponentVisible() },
                detailViewModel = viewModel,
                accountViewModel = accountViewModel,
            )
        }
    }
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.nameInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[NAME_INPUT_INDEX]

internal fun ComposeContentTestRule.clickUpdate() {
    onNodeWithContentDescription(UPDATE_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickDownload() {
    onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.progressIn(contentDescription: String): SemanticsNodeInteraction =
    onNode(
        hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(contentDescription))),
        useUnmergedTree = true,
    )

internal fun ComposeContentTestRule.textCount(text: String): Int = onAllNodesWithText(text).fetchSemanticsNodes().size
