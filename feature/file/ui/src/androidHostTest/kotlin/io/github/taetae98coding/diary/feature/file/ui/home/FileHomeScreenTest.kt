@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.PageFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RefreshFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileHomeUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileHomeUseCase
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePicker
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

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
        coEvery { server(0) } throws failure() coAndThen { response.await() }

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
    fun `TC-FILE-HOME-FEATURE-013 이어서 불러오기에 실패하면 목록 끝에 실패 안내를 표시하고 이미 나타난 파일은 그대로 남는다`() {
        val fileList = fileList(count = PAGE_SIZE)
        val server = server()
        coEvery { server(0) } returns fileList
        coEvery { server(1) } throws failure()

        setFileHomeScreen(server = server)
        scrollListTo(index = PAGE_SIZE)

        composeRule.onNodeWithText(LOAD_MORE_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
        fileList.forEachIndexed { index, file ->
            scrollListTo(index = index)
            composeRule.onNodeWithText(file.name).assertExists()
        }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-014 이어서 불러오기 실패 후 다시 시도하면 이어서 다시 불러와 기존 목록 뒤에 붙인다`() {
        val fileList = fileList(count = 25)
        val pageList = fileList.chunked(PAGE_SIZE)
        val server = server()
        coEvery { server(0) } returns pageList[0]
        coEvery { server(1) } throws failure() andThen pageList[1]

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
    fun `TC-FILE-HOME-FEATURE-020 이어서 불러오는 동안 목록 끝에 불러오는 중을 표시하고 이미 나타난 파일은 그대로 남는다`() {
        val fileList = fileList(count = PAGE_SIZE)
        val server = server()
        coEvery { server(0) } returns fileList
        coEvery { server(1) } coAnswers { awaitCancellation() }

        setFileHomeScreen(server = server)
        scrollListTo(index = PAGE_SIZE)

        composeRule.onNodeWithTag(FILE_HOME_APPEND_LOADING_TEST_TAG).assertExists()
        fileList.forEachIndexed { index, file ->
            scrollListTo(index = index)
            composeRule.onNodeWithText(file.name).assertExists()
        }
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
        coEvery { server(0) } throws failure()

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
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file))), pickedUri = null, upload = upload)
        clickAdd()

        verify(exactly = 1) { screen.filePicker.open() }
        coVerify(exactly = 0) { upload.requestFileUploadUseCase(parameter = any()) }
        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(file.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-017 파일을 고르면 곧바로 올리고 올리는 동안 다시 추가할 수 없다`() {
        val uri = fixtureMonkey.fileUri()
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))), pickedUri = uri, upload = upload)
        clickAdd()
        clickAdd()

        coVerify(exactly = 1) { upload.requestFileUploadUseCase(parameter = uri) }
        verify(exactly = 1) { screen.filePicker.open() }
        uploadProgress().assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-018 올리기에 성공하면 이전 목록을 둔 채 다시 불러와 목록의 처음으로 돌아가 올린 파일을 맨 앞에 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededReturnsToTop(uploaded = uploaded, prefix = listOf(uploaded), expectedFirst = uploaded)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-018 다른 기기에서 더 늦게 올린 파일이 앞에 있어도 올리기에 성공하면 목록의 처음으로 돌아간다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val otherDevice = fixtureMonkey.diaryFile(name = "other-device.txt")

        assertUploadSucceededReturnsToTop(uploaded = uploaded, prefix = listOf(otherDevice, uploaded), expectedFirst = otherDevice)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-019 올리기에 실패하면 크기 초과를 구분해 알리고 목록을 유지하며 다시 추가할 수 있다`() {
        val file = fixtureMonkey.diaryFile()
        val server = server(pageList = listOf(listOf(file)))
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(server = server, upload = upload)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.TooLarge) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_TOO_LARGE_MESSAGE).assertExists()
        uploadProgress().assertDoesNotExist()
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()

        verify(exactly = 2) { screen.filePicker.open() }
        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(file.name).assertExists()
        uploadProgress().assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-021 올리는 동안에도 목록을 확인하고 이동할 수 있다`() {
        val fileList = fileList(count = 30)

        setFileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)))
        clickAdd()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        composeRule.onNodeWithText(fileList.last().name).assertExists()
        uploadProgress().assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-023 목록이 보이는 상태에서 올리기 성공 뒤 다시 불러오기에 실패하면 보던 자리의 이전 목록을 두고 다시 시도와 함께 알린다`() {
        val fileList = fileList(count = 30)
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThenThrows failure()
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        scrollListTo(index = PAGE_SIZE - 1)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertExists()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-023 목록이 보이는 상태에서 당겨서 새로고침하다 실패하면 이전 목록을 두고 다시 시도와 함께 알린다`() {
        val fileList = fileList(count = 30)
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThenThrows failure()
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)

        setFileHomeScreen(server = server)
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = 0)
        pull()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(fileList.first().name).assertExists()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-024 올리는 동안 계정이 바뀌면 올리기를 중단하고 결과를 알리지 않는다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(accountFlow = accountFlow, upload = upload)
        clickAdd()
        uploadProgress().assertExists()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
            upload.finishWithoutEvent()
        }
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
        val upload = FileUploadMocks()

        setFileHomeScreen(accountFlow = accountFlow, upload = upload)
        clickAdd()
        uploadProgress().assertExists()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(Account.Guest)
            upload.finishWithoutEvent()
        }
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(GUEST_TITLE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-025 올리는 동안 뒤로가면 이전 화면으로 돌아가고 올리기는 이어진다`() {
        var navigateUpCount = 0
        val upload = FileUploadMocks()

        setFileHomeScreen(upload = upload, navigateUp = { navigateUpCount += 1 })
        clickAdd()
        uploadProgress().assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-026 다시 불러오기 실패 안내의 다시 시도를 선택하면 이전 목록을 둔 채 처음부터 다시 불러온다`() {
        val fileList = fileList(count = 5)
        val added = fixtureMonkey.diaryFile(name = ADDED_FILE_NAME)
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure() coAndThen { response.await() }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = added.id)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 3) { server(0) }
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(fileList.first().name).assertExists()
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertExists()

        response.complete(listOf(added) + fileList)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(added.name).assertExists()
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-028 파일 목록에서 당겨서 새로고침하면 이전 목록을 둔 채 처음부터 다시 불러온다`() {
        val fileList = fileList(count = 5)
        val added = fixtureMonkey.diaryFile(name = ADDED_FILE_NAME)
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList coAndThen { response.await() }

        setFileHomeScreen(server = server)
        pull()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(fileList.first().name).assertExists()
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertExists()

        response.complete(listOf(added) + fileList)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertDoesNotExist()
        (composeRule.onNodeWithText(added.name).getUnclippedBoundsInRoot().top < composeRule.onNodeWithText(fileList.first().name).getUnclippedBoundsInRoot().top) shouldBe true
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-029 빈 상태에서 당기면 처음 불러오기와 같이 불러오는 중을 표시한다`() {
        val server = server()
        val response = CompletableDeferred<List<DiaryFile>>()
        coEvery { server(0) } returns emptyList<DiaryFile>() coAndThen { response.await() }

        assertPullFromBlankBody(server = server, response = response, blankText = EMPTY_TITLE)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-029 처음 불러오기 실패에서 당기면 처음 불러오기와 같이 불러오는 중을 표시한다`() {
        val server = server()
        val response = CompletableDeferred<List<DiaryFile>>()
        coEvery { server(0) } throws failure() coAndThen { response.await() }

        assertPullFromBlankBody(server = server, response = response, blankText = LOAD_FAILED_MESSAGE)
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-030 목록을 처음 불러오는 중에는 당겨도 다시 요청하지 않는다`() {
        val server = server()
        coEvery { server(0) } coAnswers { awaitCancellation() }

        setFileHomeScreen(server = server)
        pull()

        coVerify(exactly = 1) { server(0) }
        bodyProgress().assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-031 다시 불러오는 동안 다시 당겨도 불러오기는 한 번만 진행된다`() {
        val server = server()
        coEvery { server(0) } returns fileList(count = 5) coAndThen { awaitCancellation() }

        setFileHomeScreen(server = server)
        pull()
        pull()

        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-032 화면에 들어오기 전에 시작한 올리기가 끝나지 않았으면 진행 중 표시를 보이고 다시 추가할 수 없다`() {
        val upload = FileUploadMocks()
        upload.state.value = FileUploadState.Uploading(percent = null)

        val screen = setFileHomeScreen(upload = upload)
        uploadProgress().assertExists()
        clickAdd()

        verify(exactly = 0) { screen.filePicker.open() }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-033 빈 상태에서 올리기에 성공하면 불러오는 중을 표시한 뒤 올린 파일을 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededFromBlankBody(uploaded = uploaded, firstFailure = null, next = Result.success(listOf(uploaded)))
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-033 처음 불러오기 실패에서 올리기에 성공하면 불러오는 중을 표시한 뒤 올린 파일을 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededFromBlankBody(uploaded = uploaded, firstFailure = failure(), next = Result.success(listOf(uploaded)))
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-033 빈 상태에서 올리기 성공 뒤 다시 불러오기에 실패하면 스낵바 대신 본문에 실패 안내를 표시한다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededFromBlankBody(uploaded = uploaded, firstFailure = null, next = Result.failure(failure()))
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-034 목록을 처음 불러오는 중에 올리기에 성공하면 처음부터 다시 불러온다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } coAnswers { awaitCancellation() } coAndThen { response.await() }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        bodyProgress().assertExists()

        response.complete(listOf(uploaded))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(uploaded.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-035 목록의 파일을 선택해도 아무 일도 일어나지 않는다`() {
        val file = fixtureMonkey.diaryFile()
        var navigateUpCount = 0

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file))), navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithText(file.name).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 0
        verify(exactly = 0) { screen.filePicker.open() }
        composeRule.onNodeWithText(file.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-036 화면을 보고 있지 않을 때 끝난 올리기의 결과는 화면에 들어와도 알리지 않는다`() {
        assertFinishedWhileAwayIsNotNotified()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-001 목록의 계정이 바뀌면 이전 계정의 파일 없이 새 계정의 목록을 처음부터 보여 준다`() {
        val accountA = fixtureMonkey.giveMeOne<Account.User>()
        val accountB = fixtureMonkey.giveMeOne<Account.User>().copy(id = fixtureMonkey.giveMeOne<Uuid>())
        val fileListA = fileList(count = 30)
        val fileListB = List(PAGE_SIZE) { index -> fixtureMonkey.diaryFile(name = "b-$index.txt") }
        val responseB = CompletableDeferred<List<DiaryFile>>()
        val serverA = server(pageList = fileListA.chunked(PAGE_SIZE))
        val serverB = server()
        coEvery { serverB(0) } coAnswers { responseB.await() }
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(accountA))
        val paging = MutableStateFlow(pagerFlow(server = serverA))

        setFileHomeScreen(accountFlow = accountFlow, paging = paging)
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.runOnIdle {
            accountFlow.value = Result.success(accountB)
            paging.value = pagerFlow(server = serverB)
        }
        composeRule.waitForIdle()

        fileListA.take(PAGE_SIZE).forEach { file -> composeRule.onNodeWithText(file.name).assertDoesNotExist() }
        bodyProgress().assertExists()

        responseB.complete(fileListB)
        composeRule.waitForIdle()

        coVerify(exactly = 1) { serverB(0) }
        composeRule.onNodeWithText(fileListB.first().name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-002 사용자에서 게스트로 바뀌면 목록 대신 로그인 안내를 표시한다`() {
        val file = fixtureMonkey.diaryFile()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))

        val paging = MutableStateFlow(pagerFlow(server = server(pageList = listOf(listOf(file)))))

        setFileHomeScreen(accountFlow = accountFlow, paging = paging)
        composeRule.onNodeWithText(file.name).assertExists()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(Account.Guest)
            paging.value = flowOf(PagingData.empty())
        }
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
    fun `TC-FILE-HOME-DOMAIN-005 화면을 떠났다가 다시 들어오면 목록을 처음부터 다시 불러와 처음부터 보여 준다`() {
        assertReentryReloadsFromTop(isUploading = false)
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-005 떠나기 전에 시작한 올리기가 끝나지 않았으면 다시 들어와도 진행 중 표시를 보인다`() {
        assertReentryReloadsFromTop(isUploading = true)
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-006 올리는 동안 화면이 재생성되어도 진행 표시와 결과 반영이 이어진다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns listOf(fixtureMonkey.diaryFile()) andThen listOf(uploaded)
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server, upload = upload)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        clickAdd()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        uploadProgress().assertExists()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(uploaded.name).assertExists()
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-006 올리는 동안 앱이 백그라운드에 다녀와도 진행 표시와 결과 반영이 이어진다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns listOf(fixtureMonkey.diaryFile()) andThen listOf(uploaded)
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server, upload = upload)
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        clickAdd()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
        uploadProgress().assertExists()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(uploaded.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-009 계정 상태를 확인하지 못하면 본문을 비우고 따로 알리지 않으며 올리기는 이어진다`() {
        val file = fixtureMonkey.diaryFile()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server(pageList = listOf(listOf(file))), accountFlow = accountFlow, upload = upload)
        clickAdd()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TITLE).assertExists()
        composeRule.onNodeWithText(file.name).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(GUEST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-010 계정 상태를 잠시 확인하지 못했다가 같은 계정으로 돌아오면 목록을 다시 불러오지 않는다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val file = fixtureMonkey.diaryFile()
        val server = server(pageList = listOf(listOf(file)))
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))

        setFileHomeScreen(server = server, accountFlow = accountFlow)
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(file.name).assertDoesNotExist()
        composeRule.runOnIdle { accountFlow.value = Result.success(account) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(file.name).assertExists()
        coVerify(exactly = 1) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-011 게스트에서 사용자로 바뀌면 목록을 불러오고 파일 추가를 제공한다`() {
        val file = fixtureMonkey.diaryFile()
        val server = server(pageList = listOf(listOf(file)))
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(Account.Guest))
        val paging = MutableStateFlow<Flow<PagingData<DiaryFile>>>(flowOf(PagingData.empty()))

        setFileHomeScreen(accountFlow = accountFlow, paging = paging)
        composeRule.onNodeWithText(GUEST_TITLE).assertExists()
        coVerify(exactly = 0) { server(any()) }
        composeRule.runOnIdle {
            accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>())
            paging.value = pagerFlow(server = server)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(GUEST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(file.name).assertExists()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-012 화면이 재생성되면 보이던 안내가 닫히고 다시 나타나지 않는다`() {
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))), upload = upload)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-012 앱이 백그라운드에 다녀와도 보이던 안내가 남는다`() {
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))), upload = upload)
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-013 시스템이 앱을 정리했다가 다시 만들면 목록을 처음부터 다시 불러오고 보던 파일이 앞부분에 있으면 그 자리를 되살린다`() {
        val fileList = fileList(count = 30)
        val restoredServer = server(pageList = fileList.chunked(PAGE_SIZE))
        var screen = fileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)))
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = 10)
        val topBeforeRestore = composeRule.onNodeWithText(fileList[10].name).getUnclippedBoundsInRoot().top
        screen = fileHomeScreen(server = restoredServer)
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { restoredServer(0) }
        composeRule.onNodeWithText(fileList[10].name).getUnclippedBoundsInRoot().top shouldBe topBeforeRestore
        composeRule.onNodeWithText(fileList[8].name).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-013 시스템이 앱을 정리했다가 다시 만들면 보던 파일이 새로 불러온 앞부분에 없을 때 그 끝 부분을 보여 주고 이어서 불러온다`() {
        val fileList = fileList(count = 30)
        val restoredServer = server(pageList = fileList.chunked(PAGE_SIZE))
        var screen = fileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)))
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = 24)
        screen = fileHomeScreen(server = restoredServer)
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { restoredServer(0) }
        coVerify(exactly = 1) { restoredServer(1) }
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertExists()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-DOMAIN-001 앱이 화면 앞에 있고 FileHome을 표시하는 동안만 보고 있다고 알린다`() {
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server(pageList = listOf(emptyList())), upload = upload)
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)
        var isShown by mutableStateOf(true)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                if (isShown) screen.Content()
            }
        }
        composeRule.waitForIdle()
        coVerify(exactly = 1) { upload.startViewingFileHomeUseCase(parameter = Unit) }
        coVerify(exactly = 0) { upload.stopViewingFileHomeUseCase(parameter = Unit) }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.waitForIdle()
        coVerify(exactly = 1) { upload.stopViewingFileHomeUseCase(parameter = Unit) }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
        coVerify(exactly = 2) { upload.startViewingFileHomeUseCase(parameter = Unit) }

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        coVerify(exactly = 2) { upload.stopViewingFileHomeUseCase(parameter = Unit) }
    }

    private fun assertUploadSucceededReturnsToTop(
        uploaded: DiaryFile,
        prefix: List<DiaryFile>,
        expectedFirst: DiaryFile,
    ) {
        val fileList = fileList(count = 30)
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) coAndThen { response.await() }
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        uploadProgress().assertDoesNotExist()
        bodyProgress().assertDoesNotExist()
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertExists()

        response.complete((prefix + fileList).take(PAGE_SIZE))
        composeRule.waitForIdle()

        (composeRule.onNodeWithText(expectedFirst.name).getUnclippedBoundsInRoot().top < composeRule.onNodeWithText(fileList.first().name).getUnclippedBoundsInRoot().top) shouldBe true
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    private fun assertUploadSucceededFromBlankBody(
        uploaded: DiaryFile,
        firstFailure: Throwable?,
        next: Result<List<DiaryFile>>,
    ) {
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        if (firstFailure == null) {
            coEvery { server(0) } returns emptyList<DiaryFile>() coAndThen { response.await() }
        } else {
            coEvery { server(0) } throws firstFailure coAndThen { response.await() }
        }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        bodyProgress().assertExists()
        next.fold(onSuccess = response::complete, onFailure = response::completeExceptionally)
        composeRule.waitForIdle()

        if (next.isSuccess) {
            composeRule.onNodeWithText(uploaded.name).assertExists()
            composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        } else {
            composeRule.onAllNodesWithText(LOAD_FAILED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
            composeRule.onAllNodesWithText(RETRY).fetchSemanticsNodes().size shouldBe 1
        }
    }

    private fun assertFinishedWhileAwayIsNotNotified() {
        val file = fixtureMonkey.diaryFile()
        val upload = FileUploadMocks()
        upload.finishWithoutEvent()

        setFileHomeScreen(server = server(pageList = listOf(listOf(file))), upload = upload)

        composeRule.onNodeWithText(file.name).assertExists()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(UPLOAD_TOO_LARGE_MESSAGE).assertDoesNotExist()
        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertExists()
    }

    private fun assertReentryReloadsFromTop(isUploading: Boolean) {
        val fileList = fileList(count = 30)
        val upload = FileUploadMocks()
        val secondServer = server(pageList = fileList.chunked(PAGE_SIZE))
        var screen by mutableStateOf(fileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)), upload = upload))

        composeRule.setContent { key(screen) { screen.Content() } }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        if (isUploading) clickAdd()
        composeRule.runOnIdle { screen = fileHomeScreen(server = secondServer, upload = upload) }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { secondServer(0) }
        composeRule.onNodeWithText(fileList.first().name).assertExists()
        if (isUploading) uploadProgress().assertExists() else uploadProgress().assertDoesNotExist()
    }

    private fun assertAddOpensPicker(server: suspend (Int) -> List<DiaryFile>) {
        val screen = setFileHomeScreen(server = server, pickedUri = null)

        clickAdd()

        verify(exactly = 1) { screen.filePicker.open() }
    }

    private fun assertPullFromBlankBody(
        server: suspend (Int) -> List<DiaryFile>,
        response: CompletableDeferred<List<DiaryFile>>,
        blankText: String,
    ) {
        val file = fixtureMonkey.diaryFile()

        setFileHomeScreen(server = server)
        composeRule.onNodeWithText(blankText).assertExists()
        pull()

        coVerify(exactly = 2) { server(0) }
        bodyProgress().assertExists()
        composeRule.onNodeWithText(blankText).assertDoesNotExist()

        response.complete(listOf(file))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(file.name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-037 다시 불러오기에 실패한 뒤 당겨서 새로고침으로 다시 시도한다`() {
        val fileList = fileList(count = 5)
        val added = fixtureMonkey.diaryFile(name = ADDED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure() andThen listOf(added) + fileList

        setFileHomeScreen(server = server)
        pull()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        pull()

        coVerify(exactly = 3) { server(0) }
        assertAbove(upper = added, lower = fileList.first())
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-038 다시 불러오기에 성공하면 목록 끝에서 다시 이어서 불러온다`() {
        val fileList = fileList(count = 25)
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThenThrows failure() andThen fileList.take(PAGE_SIZE)
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)

        setFileHomeScreen(server = server)
        pull()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        coVerify(exactly = 3) { server(0) }
        coVerify(atLeast = 1) { server(1) }
        composeRule.onNodeWithText(fileList.last().name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-039 다시 불러오는 동안 올리기에 성공하면 처음부터 다시 불러와 처음으로 돌아간다`() {
        val fileList = fileList(count = 30)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) coAndThen { pending.await() } andThen listOf(uploaded) + fileList.take(PAGE_SIZE - 1)
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        pull()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        coVerify(exactly = 3) { server(0) }
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(uploaded.name).assertIsDisplayed()
        assertAbove(upper = uploaded, lower = fileList.first())
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-040 다시 불러오는 동안 안내의 다시 시도를 선택해도 새로 요청하지 않는다`() {
        val fileList = fileList(count = 5)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure() coAndThen { pending.await() }

        setFileHomeScreen(server = server)
        pull()
        composeRule.onNodeWithText(RETRY).assertExists()
        pull()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 3) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-041 Android에서 붙들어 둘 수 없는 파일을 고르면 올리지 못했다고 알린다`() {
        val file = fixtureMonkey.diaryFile()
        val upload = FileUploadMocks()
        coEvery { upload.requestFileUploadUseCase(parameter = any()) } returns Result.failure(SecurityException(fixtureMonkey.giveMeOne<String>()))

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file))), upload = upload)
        clickAdd()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
        uploadProgress().assertDoesNotExist()
        composeRule.onNodeWithText(file.name).assertExists()
        clickAdd()
        verify(exactly = 2) { screen.filePicker.open() }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-015 게스트가 되었다가 같은 계정으로 다시 로그인하면 목록을 처음부터 보여 준다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val fileList = fileList(count = 30)
        val secondServer = server(pageList = fileList.chunked(PAGE_SIZE))
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
        val paging = MutableStateFlow(pagerFlow(server = server(pageList = fileList.chunked(PAGE_SIZE))))

        setFileHomeScreen(accountFlow = accountFlow, paging = paging)
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(Account.Guest)
            paging.value = flowOf(PagingData.empty())
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(account)
            paging.value = pagerFlow(server = secondServer)
        }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { secondServer(0) }
        composeRule.onNodeWithText(fileList.first().name).assertIsDisplayed()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-012 화면이 재생성되면 다시 불러오기 실패 안내가 닫히고 다시 나타나지 않는다`() {
        val fileList = fileList(count = 1)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure()
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        pull()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(fileList.first().name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-018 다시 불러오기에 실패한 뒤 화면이 재생성되어도 이전 목록이 남는다`() {
        val fileList = fileList(count = 5)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure()
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        pull()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        fileList.forEach { file -> composeRule.onNodeWithText(file.name).assertExists() }
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(RETRY).assertDoesNotExist()
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-042 다시 불러오기에 실패한 뒤 남은 목록의 끝에서도 이어서 불러온다`() {
        val fileList = fileList(count = 25)
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThenThrows failure()
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)

        setFileHomeScreen(server = server)
        pull()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        coVerify(atLeast = 1) { server(1) }
        composeRule.onNodeWithText(fileList.last().name).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-012 앱이 백그라운드에 다녀와도 다시 불러오기 실패 안내가 남는다`() {
        val fileList = fileList(count = 1)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure()
        val screen = fileHomeScreen(server = server)
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        pull()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-016 본문의 처음 불러오기 실패 안내는 화면이 재생성되어도 그대로 보인다`() {
        val server = server()
        coEvery { server(0) } throws failure()
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-016 목록 끝의 이어서 불러오기 실패 안내는 화면이 재생성되어도 그대로 보인다`() {
        val fileList = fileList(count = PAGE_SIZE)
        val server = server()
        coEvery { server(0) } returns fileList
        coEvery { server(1) } throws failure()
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE)
        composeRule.onNodeWithText(LOAD_MORE_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE)

        composeRule.onNodeWithText(LOAD_MORE_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-017 계정 상태를 확정하지 않은 동안 올리기에 실패하면 실패 안내를 표시한다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server(pageList = listOf(fileList(count = 1))), accountFlow = accountFlow, upload = upload)
        clickAdd()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-017 계정 상태를 확정하지 않은 동안 올리기에 성공하면 다시 불러온 목록을 계정이 확인된 뒤 보여 준다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val before = fileList(count = 1)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns before andThen listOf(uploaded) + before
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, accountFlow = accountFlow, upload = upload)
        clickAdd()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(uploaded.name).assertDoesNotExist()
        composeRule.runOnIdle { accountFlow.value = Result.success(account) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(uploaded.name).assertExists()
        assertAbove(upper = uploaded, lower = before.first())
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-043 다시 불러오는 중에 계정이 바뀌면 앞선 계정의 파일이 새 목록에 나타나지 않는다`() {
        val accountA = fixtureMonkey.giveMeOne<Account.User>()
        val accountB = fixtureMonkey.giveMeOne<Account.User>().copy(id = fixtureMonkey.giveMeOne<Uuid>())
        val fileListA = fileList(count = 5)
        val lateListA = List(2) { index -> fixtureMonkey.diaryFile(name = "late-a-$index.txt") }
        val fileListB = List(3) { index -> fixtureMonkey.diaryFile(name = "b-$index.txt") }
        val pendingA = CompletableDeferred<List<DiaryFile>>()
        val serverA = server()
        coEvery { serverA(0) } returns fileListA coAndThen { pendingA.await() }
        val serverB = server(pageList = listOf(fileListB))
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(accountA))
        val paging = MutableStateFlow(pagerFlow(server = serverA))

        setFileHomeScreen(accountFlow = accountFlow, paging = paging)
        pull()
        composeRule.runOnIdle {
            accountFlow.value = Result.success(accountB)
            paging.value = pagerFlow(server = serverB)
        }
        composeRule.waitForIdle()
        pendingA.complete(lateListA)
        composeRule.waitForIdle()

        fileListB.forEach { file -> composeRule.onNodeWithText(file.name).assertExists() }
        (fileListA + lateListA).forEach { file -> composeRule.onNodeWithText(file.name).assertDoesNotExist() }
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-044 올리기 성공으로 다시 불러오는 동안 당겨도 새로 요청하지 않는다`() {
        val fileList = fileList(count = 5)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList coAndThen { pending.await() }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        pull()

        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-045 당겨서 다시 불러오는 동안에도 이전 목록을 끝까지 이동할 수 있다`() {
        val fileList = fileList(count = 30)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) coAndThen { pending.await() }
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)

        setFileHomeScreen(server = server)
        pull()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        composeRule.onNodeWithText(fileList.last().name).assertIsDisplayed()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-017 계정 상태를 확정하지 않은 동안 올리기 뒤 다시 불러오기에 실패하면 알리지 않고 계정이 확인되면 이전 목록을 보여 준다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val before = fileList(count = 1)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns before andThenThrows failure()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, accountFlow = accountFlow, upload = upload)
        clickAdd()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.runOnIdle { accountFlow.value = Result.success(account) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(before.first().name).assertExists()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-017 계정 상태를 확정하지 않은 동안 빈 상태에서 올리기 뒤 다시 불러오기에 실패하면 계정이 확인된 뒤 본문에 실패 안내를 보여 준다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns emptyList<DiaryFile>() andThenThrows failure()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, accountFlow = accountFlow, upload = upload)
        composeRule.onNodeWithText(EMPTY_TITLE).assertExists()
        clickAdd()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.runOnIdle { accountFlow.value = Result.success(account) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
        composeRule.onNodeWithText(EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-019 당겨서 다시 불러오는 중에 화면이 재생성되어도 진행 표시가 보인다`() {
        val fileList = fileList(count = 5)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList coAndThen { pending.await() }
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        pull()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertExists()
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-019 당겨서 다시 불러오는 중에 화면이 재생성된 뒤 실패하면 재생성된 화면에서 알린다`() {
        val fileList = fileList(count = 5)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList coAndThen { pending.await() }
        val screen = fileHomeScreen(server = server)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        pull()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        pending.completeExceptionally(failure())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(RETRY).assertExists()
        fileList.forEach { file -> composeRule.onNodeWithText(file.name).assertExists() }
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-FILE-HOME-DOMAIN-019 올리기 성공으로 다시 불러오는 중에 화면이 재생성되어도 새 목록이 나타나면 처음으로 돌아간다`() {
        val fileList = fileList(count = 30)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val pending = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) coAndThen { pending.await() }
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server, upload = upload)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)
        clickAdd()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        pending.complete(listOf(uploaded) + fileList.take(PAGE_SIZE - 1))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(uploaded.name).assertIsDisplayed()
        coVerify(exactly = 2) { server(0) }
    }

    private fun setFileHomeScreen(
        server: suspend (Int) -> List<DiaryFile> = server(pageList = listOf(emptyList())),
        paging: MutableStateFlow<Flow<PagingData<DiaryFile>>> = MutableStateFlow(pagerFlow(server = server)),
        accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(fixtureMonkey.giveMeOne<Account.User>())),
        navigateUp: () -> Unit = {},
        pickedUri: FileUri? = fixtureMonkey.fileUri(),
        upload: FileUploadMocks = FileUploadMocks(),
    ): FileHomeTestScreen {
        val screen =
            fileHomeScreen(
                server = server,
                paging = paging,
                accountFlow = accountFlow,
                navigateUp = navigateUp,
                pickedUri = pickedUri,
                upload = upload,
            )

        composeRule.setContent { screen.Content() }
        composeRule.waitForIdle()

        return screen
    }

    private fun fileHomeScreen(
        server: suspend (Int) -> List<DiaryFile> = server(pageList = listOf(emptyList())),
        paging: MutableStateFlow<Flow<PagingData<DiaryFile>>> = MutableStateFlow(pagerFlow(server = server)),
        accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(fixtureMonkey.giveMeOne<Account.User>())),
        navigateUp: () -> Unit = {},
        pickedUri: FileUri? = fixtureMonkey.fileUri(),
        upload: FileUploadMocks = FileUploadMocks(),
    ): FileHomeTestScreen {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow
        val uploadViewModel = upload.viewModel()
        val filePicker = mockk<FilePicker>()
        every { filePicker.open() } answers { pickedUri?.let(uploadViewModel::upload) }

        return FileHomeTestScreen(
            navigateUp = navigateUp,
            filePicker = filePicker,
            fileViewModel =
                FileHomeViewModel(
                    getAccountUseCase = getAccountUseCase,
                    pageFileUseCase = pageFileUseCase(paging = paging),
                ),
            uploadViewModel = uploadViewModel,
            refreshViewModel = FileHomeRefreshViewModel(refreshFileUseCase = refreshFileUseCase(server = server, paging = paging)),
        )
    }

    private fun assertAbove(
        upper: DiaryFile,
        lower: DiaryFile,
    ) {
        val upperTop = composeRule.onNodeWithText(upper.name).getUnclippedBoundsInRoot().top
        val lowerTop = composeRule.onNodeWithText(lower.name).getUnclippedBoundsInRoot().top

        (upperTop < lowerTop) shouldBe true
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun pull() {
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
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

    private fun bodyProgress() = composeRule.onNodeWithContentDescription(LOADING_DESCRIPTION, useUnmergedTree = true)

    private companion object {
        private const val TITLE = "파일"
        private const val NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val UPLOADED_FILE_NAME = "uploaded.txt"
        private const val ADDED_FILE_NAME = "added.txt"
        private const val ADD_BUTTON_DESCRIPTION = "파일 추가"
        private const val SNACKBAR_DISMISS_MILLIS = 20_000L
        private const val LOADING_DESCRIPTION = "파일 불러오는 중"
        private const val REFRESHING = "새로고침 중"
        private const val GUEST_TITLE = "로그인이 필요합니다"
        private const val EMPTY_TITLE = "아직 올린 파일이 없습니다"
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
    val refreshViewModel: FileHomeRefreshViewModel,
) {
    @Composable
    fun Content() {
        DiaryTheme {
            FileHomeScreen(
                navigateUp = navigateUp,
                filePicker = filePicker,
                fileViewModel = fileViewModel,
                uploadViewModel = uploadViewModel,
                refreshViewModel = refreshViewModel,
            )
        }
    }
}

// 올리기의 상태와 결과는 mock이 돌려주는 흐름으로, 요청이 받아들여진 뒤의 상태는 요청 stub의 응답으로 제어한다.
private class FileUploadMocks {
    val state = MutableStateFlow<FileUploadState>(FileUploadState.Idle)
    private val eventChannel = Channel<FileUploadEvent>(Channel.UNLIMITED)

    val requestFileUploadUseCase: RequestFileUploadUseCase =
        mockk<RequestFileUploadUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } coAnswers {
                state.value = FileUploadState.Uploading(percent = null)
                Result.success(Unit)
            }
        }

    private val getFileUploadStateUseCase: GetFileUploadStateUseCase =
        mockk<GetFileUploadStateUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns state.map { value -> Result.success(value) }
        }

    private val getFileUploadEventUseCase: GetFileUploadEventUseCase =
        mockk<GetFileUploadEventUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns eventChannel.receiveAsFlow().map { event -> Result.success(event) }
        }

    val startViewingFileHomeUseCase: StartViewingFileHomeUseCase =
        mockk<StartViewingFileHomeUseCase>().also { useCase ->
            coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
        }

    val stopViewingFileHomeUseCase: StopViewingFileHomeUseCase =
        mockk<StopViewingFileHomeUseCase>().also { useCase ->
            coEvery { useCase(parameter = Unit) } returns Result.success(Unit)
        }

    fun finish(event: FileUploadEvent) {
        state.value = FileUploadState.Idle
        eventChannel.trySend(event)
    }

    fun finishWithoutEvent() {
        state.value = FileUploadState.Idle
    }

    fun viewModel(): FileHomeUploadViewModel =
        FileHomeUploadViewModel(
            requestFileUploadUseCase = requestFileUploadUseCase,
            startViewingFileHomeUseCase = startViewingFileHomeUseCase,
            stopViewingFileHomeUseCase = stopViewingFileHomeUseCase,
            getFileUploadStateUseCase = getFileUploadStateUseCase,
            getFileUploadEventUseCase = getFileUploadEventUseCase,
        )
}

// 테스트가 계정을 바꿀 때 보여 줄 목록의 흐름도 함께 바꾼다.
private fun pageFileUseCase(paging: Flow<Flow<PagingData<DiaryFile>>>): PageFileUseCase {
    val useCase = mockk<PageFileUseCase>()
    every { useCase(parameter = Unit) } returns paging.flatMapLatest { pagingData -> pagingData }.map { pagingData -> Result.success(pagingData) }
    return useCase
}

// 다시 불러오기의 결과는 stub이 정한다. 첫 페이지를 받으면 그 페이지로 시작하는 목록 흐름으로 바꾸고, 받지 못하면 실패를 돌려준다.
private fun refreshFileUseCase(
    server: suspend (Int) -> List<DiaryFile>,
    paging: MutableStateFlow<Flow<PagingData<DiaryFile>>>,
): RefreshFileUseCase {
    val useCase = mockk<RefreshFileUseCase>()
    coEvery { useCase(parameter = Unit) } coAnswers {
        try {
            val firstPage = server(0)

            paging.value = pagerFlow(server = { page -> if (page == 0) firstPage else server(page) }, startsWithLoading = false)
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
    return useCase
}

private fun pagerFlow(
    server: suspend (Int) -> List<DiaryFile>,
    startsWithLoading: Boolean = true,
): Flow<PagingData<DiaryFile>> {
    val flow =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { pagingSource(server = server) },
        ).flow

    return if (startsWithLoading) flow.onStart { emit(PagingData.empty(sourceLoadStates = LOADING_LOAD_STATES)) } else flow
}

private fun failure(): IllegalStateException = IllegalStateException(fixtureMonkey.giveMeOne<String>())

private val LOADING_LOAD_STATES: LoadStates =
    LoadStates(
        refresh = LoadState.Loading,
        prepend = LoadState.NotLoading(endOfPaginationReached = false),
        append = LoadState.NotLoading(endOfPaginationReached = false),
    )

private fun server(pageList: List<List<DiaryFile>> = emptyList()): suspend (Int) -> List<DiaryFile> {
    val server = mockk<suspend (Int) -> List<DiaryFile>>()
    coEvery { server(any()) } returns emptyList()
    pageList.forEachIndexed { page, fileList -> coEvery { server(page) } returns fileList }
    return server
}

// Pager는 원천이 무효화되면 새 원천으로 다시 불러오므로, 무효화를 알릴 수 있도록 알림 등록과 무효화를 함께 흉내 낸다.
private fun pagingSource(server: suspend (Int) -> List<DiaryFile>): PagingSource<Int, DiaryFile> {
    val invalidatedCallbackList = mutableListOf<() -> Unit>()
    var isInvalid = false
    val pagingSource = mockk<PagingSource<Int, DiaryFile>>(relaxed = true)
    every { pagingSource.getRefreshKey(any()) } returns null
    every { pagingSource.invalid } answers { isInvalid }
    every { pagingSource.registerInvalidatedCallback(any()) } answers { invalidatedCallbackList += firstArg<() -> Unit>() }
    every { pagingSource.unregisterInvalidatedCallback(any()) } answers { invalidatedCallbackList -= firstArg<() -> Unit>() }
    every { pagingSource.invalidate() } answers {
        if (!isInvalid) {
            isInvalid = true
            invalidatedCallbackList.toList().forEach { callback -> callback() }
        }
    }
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
