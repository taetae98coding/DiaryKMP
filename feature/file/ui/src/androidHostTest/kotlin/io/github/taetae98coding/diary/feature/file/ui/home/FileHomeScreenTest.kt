package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.usecase.PageFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PAGE_SIZE = 20

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class FileHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-003 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0

        setFileHomeScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-010 처음 불러오기 실패 후 다시 시도하면 불러오는 중을 표시하고 목록을 처음부터 다시 불러온다`() {
        val file = fixtureMonkey.diaryFile()
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } throws IllegalStateException("load") coAndThen { response.await() }

        setFileHomeScreen(server = server)
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()
        bodyProgress().assertExists()
        response.complete(listOf(file))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(file.name).assertExists()
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-012 목록의 끝으로 이동하면 다음 파일을 이어서 불러와 붙인다`() {
        val fileList = fileList(count = 25)
        val server = server(pageList = fileList.chunked(PAGE_SIZE))

        setFileHomeScreen(server = server)
        scrollListTo(index = PAGE_SIZE - 1)

        fileList.zipWithNext().forEachIndexed { index, (file, nextFile) ->
            scrollListTo(index = index)
            val top = composeRule.onNodeWithText(file.name).getUnclippedBoundsInRoot().top
            val nextTop = composeRule.onNodeWithText(nextFile.name).getUnclippedBoundsInRoot().top

            (top < nextTop) shouldBe true
        }
        coVerify(exactly = 1) { server(1) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-014 이어서 불러오기 실패 후 다시 시도하면 이어서 다시 불러와 기존 목록 뒤에 붙인다`() {
        val fileList = fileList(count = 25)
        val pageList = fileList.chunked(PAGE_SIZE)
        val server = server()
        coEvery { server(0) } returns pageList[0]
        coEvery { server(1) } throws IllegalStateException("append") andThen pageList[1]

        setFileHomeScreen(server = server)
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = PAGE_SIZE)
        composeRule.onNodeWithText(LOAD_MORE_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()
        scrollListTo(index = fileList.lastIndex)

        composeRule.onNodeWithText(fileList.last().name).assertExists()
        coVerify(exactly = 2) { server(1) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-015 처음 불러오는 중에 파일 추가를 선택하면 파일 선택 도구를 연다`() {
        val server = server()
        coEvery { server(0) } coAnswers { awaitCancellation() }

        assertAddOpensPicker(server = server)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-015 처음 불러오기에 실패했을 때 파일 추가를 선택하면 파일 선택 도구를 연다`() {
        val server = server()
        coEvery { server(0) } throws IllegalStateException("load")

        assertAddOpensPicker(server = server)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-015 빈 상태에서 파일 추가를 선택하면 파일 선택 도구를 연다`() {
        assertAddOpensPicker(server = server(pageList = listOf(emptyList())))
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-015 파일 목록이 표시될 때 파일 추가를 선택하면 파일 선택 도구를 연다`() {
        assertAddOpensPicker(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))))
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-016 파일을 고르지 않고 취소하면 아무것도 올리지 않는다`() {
        val file = fixtureMonkey.diaryFile()
        val uploadFileUseCase = mockk<UploadFileUseCase>()

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file))), pickedUri = null, uploadFileUseCase = uploadFileUseCase)
        clickAdd()

        verify(exactly = 1) { screen.filePicker.open() }
        coVerify(exactly = 0) { uploadFileUseCase(parameter = any()) }
        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(file.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-017 파일을 고르면 곧바로 올리고 올리는 동안 다시 추가할 수 없다`() {
        val uri = fixtureMonkey.fileUri()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers { awaitCancellation() }

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))), pickedUri = uri, uploadFileUseCase = uploadFileUseCase)
        clickAdd()
        clickAdd()

        coVerify(exactly = 1) { uploadFileUseCase(parameter = uri) }
        verify(exactly = 1) { screen.filePicker.open() }
        uploadProgress().assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-018 올리기에 성공하면 목록을 처음부터 다시 불러와 처음으로 돌아가 올린 파일을 맨 앞에 보여 준다`() {
        val fileList = fileList(count = 30)
        val uploaded = fixtureMonkey.diaryFile(name = "uploaded.txt")
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThen (listOf(uploaded) + fileList).take(PAGE_SIZE)
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } returns Result.success(uploaded)

        setFileHomeScreen(server = server, uploadFileUseCase = uploadFileUseCase)
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        clickAdd()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(uploaded.name).assertExists()
        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-019 올리기에 실패하면 크기 초과를 구분해 알리고 목록을 유지하며 다시 추가할 수 있다`() {
        val file = fixtureMonkey.diaryFile()
        val server = server(pageList = listOf(listOf(file)))
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } returns
            Result.failure(FileTooLargeException(message = "too large")) andThen Result.failure(IllegalStateException("upload"))

        val screen = setFileHomeScreen(server = server, uploadFileUseCase = uploadFileUseCase)
        clickAdd()
        composeRule.onNodeWithText(UPLOAD_TOO_LARGE_MESSAGE).assertExists()
        clickAdd()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()

        verify(exactly = 2) { screen.filePicker.open() }
        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(file.name).assertExists()
        uploadProgress().assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-021 올리는 동안에도 목록을 확인하고 이동할 수 있다`() {
        val fileList = fileList(count = 30)
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers { awaitCancellation() }

        setFileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)), uploadFileUseCase = uploadFileUseCase)
        clickAdd()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        composeRule.onNodeWithText(fileList.last().name).assertExists()
        uploadProgress().assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-022 올리는 동안 뒤로가면 결과를 기다리지 않고 돌아간다`() {
        var navigateUpCount = 0
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers { awaitCancellation() }

        setFileHomeScreen(uploadFileUseCase = uploadFileUseCase, navigateUp = { navigateUpCount += 1 })
        clickAdd()
        uploadProgress().assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-023 목록이 보이는 상태에서 다시 불러오기에 실패하면 이전 목록을 두고 실패를 알린다`() {
        val file = fixtureMonkey.diaryFile()
        val server = server()
        coEvery { server(0) } returns listOf(file) andThenThrows IllegalStateException("refresh")
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } returns Result.success(fixtureMonkey.diaryFile())

        setFileHomeScreen(server = server, uploadFileUseCase = uploadFileUseCase)
        clickAdd()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(file.name).assertExists()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-024 올리는 동안 계정이 바뀌면 올리기를 중단하고 결과를 알리지 않는다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val accountChanged = CompletableDeferred<Unit>()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers {
            accountChanged.await()
            Result.failure(FileUploadAccountChangedException(message = "changed"))
        }

        val screen = setFileHomeScreen(accountFlow = accountFlow, uploadFileUseCase = uploadFileUseCase)
        clickAdd()
        uploadProgress().assertExists()
        accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
        accountChanged.complete(Unit)
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_TOO_LARGE_MESSAGE).assertDoesNotExist()
        clickAdd()
        verify(exactly = 2) { screen.filePicker.open() }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-024 올리는 동안 게스트가 되면 올리기를 중단하고 파일 추가 대신 로그인 안내를 표시한다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val accountChanged = CompletableDeferred<Unit>()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers {
            accountChanged.await()
            Result.failure(FileUploadAccountChangedException(message = "changed"))
        }

        setFileHomeScreen(accountFlow = accountFlow, uploadFileUseCase = uploadFileUseCase)
        clickAdd()
        uploadProgress().assertExists()
        accountFlow.value = Result.success(Account.Guest)
        accountChanged.complete(Unit)
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(GUEST_TITLE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-002 사용자에서 게스트로 바뀌면 목록 대신 로그인 안내를 표시한다`() {
        val file = fixtureMonkey.diaryFile()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))

        setFileHomeScreen(server = server(pageList = listOf(listOf(file))), accountFlow = accountFlow)
        composeRule.onNodeWithText(file.name).assertExists()
        accountFlow.value = Result.success(Account.Guest)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GUEST_TITLE).assertExists()
        composeRule.onNodeWithText(file.name).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-004 화면이 재생성되어도 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
        val fileList = fileList(count = 30)
        val server = server(pageList = fileList.chunked(PAGE_SIZE))
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertExists()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        coVerify(exactly = 1) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-004 앱이 백그라운드에 다녀와도 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
        val fileList = fileList(count = 30)
        val server = server(pageList = fileList.chunked(PAGE_SIZE))
        val screen = fileHomeScreen(server = server)
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertExists()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        coVerify(exactly = 1) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-006 올리는 동안 화면이 재생성되어도 진행 표시와 결과 반영이 이어진다`() {
        val uploaded = fixtureMonkey.diaryFile(name = "uploaded.txt")
        val server = server()
        coEvery { server(0) } returns listOf(fixtureMonkey.diaryFile()) andThen listOf(uploaded)
        val completion = CompletableDeferred<DiaryFile>()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        coEvery { uploadFileUseCase(parameter = any()) } coAnswers { Result.success(completion.await()) }
        val screen = fileHomeScreen(server = server, uploadFileUseCase = uploadFileUseCase)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        clickAdd()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        uploadProgress().assertExists()
        completion.complete(uploaded)
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(uploaded.name).assertExists()
        coVerify(exactly = 2) { server(0) }
    }

    private fun assertAddOpensPicker(server: suspend (Int) -> List<DiaryFile>) {
        val screen = setFileHomeScreen(server = server, pickedUri = null)

        clickAdd()

        verify(exactly = 1) { screen.filePicker.open() }
    }

    private fun setFileHomeScreen(
        server: suspend (Int) -> List<DiaryFile> = server(pageList = listOf(emptyList())),
        accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(fixtureMonkey.giveMeOne<Account.User>())),
        navigateUp: () -> Unit = {},
        pickedUri: FileUri? = fixtureMonkey.fileUri(),
        uploadFileUseCase: UploadFileUseCase = mockk(),
    ): FileHomeTestScreen {
        val screen =
            fileHomeScreen(
                server = server,
                accountFlow = accountFlow,
                navigateUp = navigateUp,
                pickedUri = pickedUri,
                uploadFileUseCase = uploadFileUseCase,
            )

        composeRule.setContent { screen.Content() }
        composeRule.waitForIdle()

        return screen
    }

    private fun fileHomeScreen(
        server: suspend (Int) -> List<DiaryFile>,
        accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(fixtureMonkey.giveMeOne<Account.User>())),
        navigateUp: () -> Unit = {},
        pickedUri: FileUri? = fixtureMonkey.fileUri(),
        uploadFileUseCase: UploadFileUseCase = mockk(),
    ): FileHomeTestScreen {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow
        val pageFileUseCase = mockk<PageFileUseCase>()
        every { pageFileUseCase(parameter = Unit) } returns
            Pager(
                config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
                pagingSourceFactory = { pagingSource(server = server) },
            ).flow.map { pagingData -> Result.success(pagingData) }
        val uploadViewModel = FileHomeUploadViewModel(uploadFileUseCase = uploadFileUseCase)
        val filePicker = mockk<FilePicker>()
        every { filePicker.open() } answers { pickedUri?.let(uploadViewModel::upload) }

        return FileHomeTestScreen(
            navigateUp = navigateUp,
            filePicker = filePicker,
            fileViewModel = FileHomeViewModel(getAccountUseCase = getAccountUseCase, pageFileUseCase = pageFileUseCase),
            uploadViewModel = uploadViewModel,
        )
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun scrollListTo(index: Int) {
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).performScrollToIndex(index)
        composeRule.waitForIdle()
    }

    private fun uploadProgress() =
        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(ADD_BUTTON_DESCRIPTION))),
            useUnmergedTree = true,
        )

    private fun bodyProgress() =
        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(ADD_BUTTON_DESCRIPTION)).not()),
            useUnmergedTree = true,
        )

    private companion object {
        private const val NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val ADD_BUTTON_DESCRIPTION = "파일 추가"
        private const val GUEST_TITLE = "로그인이 필요합니다"
        private const val LOAD_FAILED_MESSAGE = "파일을 불러오지 못했습니다"
        private const val LOAD_MORE_FAILED_MESSAGE = "더 불러오지 못했습니다"
        private const val RETRY = "다시 시도"
        private const val UPLOAD_TOO_LARGE_MESSAGE = "50MB 이하 파일만 올릴 수 있습니다."
        private const val UPLOAD_FAILED_MESSAGE = "파일을 올리지 못했습니다."
    }
}

private class FileHomeTestScreen(
    val navigateUp: () -> Unit,
    val filePicker: FilePicker,
    val fileViewModel: FileHomeViewModel,
    val uploadViewModel: FileHomeUploadViewModel,
) {
    @Composable
    fun Content() {
        DiaryTheme {
            FileHomeScreen(
                navigateUp = navigateUp,
                filePicker = filePicker,
                fileViewModel = fileViewModel,
                uploadViewModel = uploadViewModel,
            )
        }
    }
}

private fun server(pageList: List<List<DiaryFile>> = emptyList()): suspend (Int) -> List<DiaryFile> {
    val server = mockk<suspend (Int) -> List<DiaryFile>>()
    coEvery { server(any()) } returns emptyList()
    pageList.forEachIndexed { page, fileList -> coEvery { server(page) } returns fileList }
    return server
}

private fun pagingSource(server: suspend (Int) -> List<DiaryFile>): PagingSource<Int, DiaryFile> {
    val pagingSource = mockk<PagingSource<Int, DiaryFile>>(relaxed = true)
    every { pagingSource.getRefreshKey(any()) } returns null
    coEvery { pagingSource.load(any()) } coAnswers {
        val page = firstArg<PagingSource.LoadParams<Int>>().key ?: 0

        runCatching { server(page) }.fold(
            onSuccess = { fileList ->
                PagingSource.LoadResult.Page(
                    data = fileList,
                    prevKey = null,
                    nextKey = if (fileList.size < PAGE_SIZE) null else page + 1,
                )
            },
            onFailure = { throwable -> PagingSource.LoadResult.Error(throwable) },
        )
    }
    return pagingSource
}

private fun fileList(count: Int): List<DiaryFile> = List(count) { index -> fixtureMonkey.diaryFile(name = "file-$index.txt") }
