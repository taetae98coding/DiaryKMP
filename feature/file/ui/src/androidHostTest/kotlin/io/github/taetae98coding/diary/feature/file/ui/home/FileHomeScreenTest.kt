@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
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
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadSucceededEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetUpdatedFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.PageFileUseCase
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    private var lastUpload: FileUploadMocks? = null

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-265cd00c-e425-422d-8afc-8e7fbbd9c701 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0

        setFileHomeScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-1c5350e0-97bf-485b-adac-25069bde7bd8 처음 불러오기 실패 후 다시 시도하면 불러오는 중을 표시하고 목록을 처음부터 다시 불러온다`() {
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
    fun `TC-5bc8c5b1-a167-4dc8-947e-8800be9555df 목록의 끝으로 이동하면 다음 파일을 이어서 불러와 붙인다`() {
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
    fun `TC-a487c7b4-8ae1-40cf-8b98-84f6003dc4db 이어서 불러오기에 실패하면 목록 끝에 실패 안내를 표시하고 이미 나타난 파일은 그대로 남는다`() {
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
    fun `TC-e1268149-3bf6-4a84-8b06-aaeaa625dfe5 이어서 불러오기 실패 후 다시 시도하면 이어서 다시 불러와 기존 목록 뒤에 붙인다`() {
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
    fun `TC-37eee6d1-c7dd-42b2-aa50-28681089396d 이어서 불러오는 동안 목록 끝에 불러오는 중을 표시하고 이미 나타난 파일은 그대로 남는다`() {
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
    fun `TC-136eb686-fefb-4b3a-9cde-fa8e6f8a5a29 처음 불러오는 중에 파일 추가를 선택하면 FileAdd로 이동한다`() {
        val server = server()
        coEvery { server(0) } coAnswers { awaitCancellation() }

        assertAddNavigatesToFileAdd(server = server)
    }

    @Test
    fun `TC-136eb686-fefb-4b3a-9cde-fa8e6f8a5a29 처음 불러오기에 실패했을 때 파일 추가를 선택하면 FileAdd로 이동한다`() {
        val server = server()
        coEvery { server(0) } throws failure()

        assertAddNavigatesToFileAdd(server = server)
    }

    @Test
    fun `TC-136eb686-fefb-4b3a-9cde-fa8e6f8a5a29 빈 상태에서 파일 추가를 선택하면 FileAdd로 이동한다`() {
        assertAddNavigatesToFileAdd(server = server(pageList = listOf(emptyList())))
    }

    @Test
    fun `TC-136eb686-fefb-4b3a-9cde-fa8e6f8a5a29 파일 목록이 표시될 때 파일 추가를 선택하면 FileAdd로 이동한다`() {
        assertAddNavigatesToFileAdd(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))))
    }

    @Test
    fun `TC-9707e7f6-f68f-4a28-8701-f2759b92ad40 올리는 중에는 파일 추가 자리에 진행 중 표시를 보이고 FileAdd로 이동하지 않는다`() {
        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))))
        startUpload()
        clickAdd()

        uploadProgress().assertExists()
        screen.navigateToAddCount shouldBe 0
    }

    @Test
    fun `TC-7ef172cd-3ed6-454e-8f51-2c50f9ee0589 FileAdd를 보는 동안 올리기에 성공하면 돌아왔을 때 목록을 다시 불러와 처음부터 보인다`() {
        assertSucceededWhileHiddenReturnsToTop()
    }

    @Test
    fun `TC-c776256b-8864-4d45-8cdf-11baa7f6a735 FileAdd에서 돌아오면 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
        val fileList = fileList(count = 30)
        val server = server(pageList = fileList.chunked(PAGE_SIZE))
        var isFileHomeVisible by mutableStateOf(true)
        val screen = fileHomeScreen(server = server)

        setBehindFileAddContent(screen = screen, isFileHomeVisibleProvider = { isFileHomeVisible })
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.runOnIdle { isFileHomeVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isFileHomeVisible = true }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertIsDisplayed()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
    }

    @Test
    fun `TC-479329a4-daad-47b5-8490-2cfab581c160 FileDetail에서 돌아오면 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
        val fileList = fileList(count = 30)
        val server = server(pageList = fileList.chunked(PAGE_SIZE))
        var isFileHomeVisible by mutableStateOf(true)
        val screen = fileHomeScreen(server = server)

        setBehindFileAddContent(screen = screen, isFileHomeVisibleProvider = { isFileHomeVisible })
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).performClick()
        composeRule.runOnIdle { isFileHomeVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isFileHomeVisible = true }
        composeRule.waitForIdle()

        screen.navigateToDetailIdList shouldBe listOf(fileList[PAGE_SIZE - 1].id)
        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(fileList[PAGE_SIZE - 1].name).assertIsDisplayed()
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
    }

    @Test
    fun `TC-7ef172cd-3ed6-454e-8f51-2c50f9ee0589 FileDetail을 보는 동안 올리기에 성공하면 돌아왔을 때 목록을 다시 불러와 처음부터 보인다`() {
        assertSucceededWhileHiddenReturnsToTop()
    }

    @Test
    fun `TC-5dc9ca97-e9c5-4415-bc0b-72c34aa9dc5c 파일 목록에서 올리기에 성공하면 이전 목록을 둔 채 진행 중임을 표시한다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededReturnsToTop(uploaded = uploaded, prefix = listOf(uploaded), expectedFirst = uploaded)
    }

    @Test
    fun `TC-ccb81390-a936-4a22-8ce9-4fe3de62a730 올리기에 성공하면 이전 목록을 둔 채 다시 불러와 목록의 처음으로 돌아가 올린 파일을 맨 앞에 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededReturnsToTop(uploaded = uploaded, prefix = listOf(uploaded), expectedFirst = uploaded)
    }

    @Test
    fun `TC-ccb81390-a936-4a22-8ce9-4fe3de62a730 다른 기기에서 더 늦게 올린 파일이 앞에 있어도 올리기에 성공하면 목록의 처음으로 돌아간다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val otherDevice = fixtureMonkey.diaryFile(name = "other-device.txt")

        assertUploadSucceededReturnsToTop(uploaded = uploaded, prefix = listOf(otherDevice, uploaded), expectedFirst = otherDevice)
    }

    @Test
    fun `TC-f0d423c5-b463-4cef-9f6c-b17e5e1946d9 올리기에 실패하면 크기 초과를 구분해 알리고 목록을 유지하며 다시 추가할 수 있다`() {
        val file = fixtureMonkey.diaryFile()
        val server = server(pageList = listOf(listOf(file)))
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(server = server, upload = upload)
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.TooLarge) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_TOO_LARGE_MESSAGE).assertExists()
        uploadProgress().assertDoesNotExist()
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()

        clickAdd()
        screen.navigateToAddCount shouldBe 1
        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(file.name).assertExists()
        uploadProgress().assertDoesNotExist()
    }

    @Test
    fun `TC-ac8f8f6c-d3c0-4919-b527-181082f86ddb 올리는 동안에도 목록을 확인하고 이동할 수 있다`() {
        val fileList = fileList(count = 30)

        setFileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)))
        startUpload()
        scrollListTo(index = PAGE_SIZE - 1)
        scrollListTo(index = fileList.lastIndex)

        composeRule.onNodeWithText(fileList.last().name).assertExists()
        uploadProgress().assertExists()
    }

    @Test
    fun `TC-cf9aee73-db43-485e-b452-d2760c2f657b 목록이 보일 때 올리기 뒤 다시 불러오기에 실패하면 이전 목록 대신 본문에 실패 안내와 다시 시도를 표시한다`() {
        val fileList = fileList(count = 5)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure()
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        assertBodyLoadFailed(server = server, fileList = fileList)
    }

    @Test
    fun `TC-cf9aee73-db43-485e-b452-d2760c2f657b 목록이 보이는 상태에서 당겨서 새로고침하다 실패하면 이전 목록 대신 본문에 실패 안내와 다시 시도를 표시한다`() {
        val fileList = fileList(count = 5)
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure()

        setFileHomeScreen(server = server)
        pull()

        assertBodyLoadFailed(server = server, fileList = fileList)
    }

    @Test
    fun `TC-cf9aee73-db43-485e-b452-d2760c2f657b 다시 불러오기에 실패한 뒤 다시 시도하면 이전 목록 대신 불러오는 중을 표시하고 새 목록을 보여 준다`() {
        val fileList = fileList(count = 5)
        val added = fixtureMonkey.diaryFile(name = ADDED_FILE_NAME)
        val response = CompletableDeferred<List<DiaryFile>>()
        val server = server()
        coEvery { server(0) } returns fileList andThenThrows failure() coAndThen { response.await() }

        setFileHomeScreen(server = server)
        pull()
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()

        bodyProgress().assertExists()
        fileList.forEach { file -> composeRule.onNodeWithText(file.name).assertDoesNotExist() }

        response.complete(listOf(added) + fileList)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(added.name).assertExists()
        coVerify(exactly = 3) { server(0) }
    }

    @Test
    fun `TC-e0b5afbf-cf0a-4ab6-b8bf-fe786afb2e7a 올리는 동안 계정이 바뀌면 올리기를 중단하고 결과를 알리지 않는다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        val screen = setFileHomeScreen(accountFlow = accountFlow, upload = upload)
        startUpload()
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
        screen.navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-e0b5afbf-cf0a-4ab6-b8bf-fe786afb2e7a 올리는 동안 게스트가 되면 올리기를 중단하고 파일 추가 대신 로그인 안내를 표시한다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        setFileHomeScreen(accountFlow = accountFlow, upload = upload)
        startUpload()
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
    fun `TC-39aeb1ba-bc6f-407b-8b72-cd9755f4f19c 올리는 동안 뒤로가면 이전 화면으로 돌아가고 올리기는 이어진다`() {
        var navigateUpCount = 0
        val upload = FileUploadMocks()

        setFileHomeScreen(upload = upload, navigateUp = { navigateUpCount += 1 })
        startUpload()
        uploadProgress().assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-5dc9ca97-e9c5-4415-bc0b-72c34aa9dc5c 파일 목록에서 당겨서 새로고침하면 이전 목록을 둔 채 진행 중임을 표시한다`() {
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
    fun `TC-5e12ca14-d82e-45cb-b939-dd7c6971c7ac 빈 상태에서 당기면 처음 불러오기와 같이 불러오는 중을 표시한다`() {
        val server = server()
        val response = CompletableDeferred<List<DiaryFile>>()
        coEvery { server(0) } returns emptyList<DiaryFile>() coAndThen { response.await() }

        assertPullFromBlankBody(server = server, response = response, blankText = EMPTY_TITLE)
    }

    @Test
    fun `TC-5e12ca14-d82e-45cb-b939-dd7c6971c7ac 처음 불러오기 실패에서 당기면 처음 불러오기와 같이 불러오는 중을 표시한다`() {
        val server = server()
        val response = CompletableDeferred<List<DiaryFile>>()
        coEvery { server(0) } throws failure() coAndThen { response.await() }

        assertPullFromBlankBody(server = server, response = response, blankText = LOAD_FAILED_MESSAGE)
    }

    @Test
    fun `TC-c1ac5f0a-f179-41ac-bea3-879844a2b02f 목록을 처음 불러오는 중에는 당겨도 다시 요청하지 않는다`() {
        val server = server()
        coEvery { server(0) } coAnswers { awaitCancellation() }

        setFileHomeScreen(server = server)
        pull()

        coVerify(exactly = 1) { server(0) }
        bodyProgress().assertExists()
    }

    @Test
    fun `TC-8d5f4d26-8a00-45ab-abdc-4bc9e109bf42 다시 불러오는 동안 다시 당겨도 불러오기는 한 번만 진행된다`() {
        val server = server()
        coEvery { server(0) } returns fileList(count = 5) coAndThen { awaitCancellation() }

        setFileHomeScreen(server = server)
        pull()
        pull()

        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-36571a73-b36c-45e1-bde6-3929ed3f2b6e 화면에 들어오기 전에 시작한 올리기가 끝나지 않았으면 진행 중 표시를 보이고 다시 추가할 수 없다`() {
        val upload = FileUploadMocks()
        upload.state.value = FileUploadState.Uploading(percent = null)

        val screen = setFileHomeScreen(upload = upload)
        uploadProgress().assertExists()
        clickAdd()

        screen.navigateToAddCount shouldBe 0
    }

    @Test
    fun `TC-5e12ca14-d82e-45cb-b939-dd7c6971c7ac 빈 상태에서 올리기에 성공하면 불러오는 중을 표시한 뒤 올린 파일을 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededFromBlankBody(uploaded = uploaded, firstFailure = null)
    }

    @Test
    fun `TC-5e12ca14-d82e-45cb-b939-dd7c6971c7ac 불러오기 실패에서 올리기에 성공하면 불러오는 중을 표시한 뒤 올린 파일을 보여 준다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)

        assertUploadSucceededFromBlankBody(uploaded = uploaded, firstFailure = failure())
    }

    @Test
    fun `TC-1a8c9a93-df0e-4773-81ef-ed56e332e3d6 목록을 처음 불러오는 중에 올리기에 성공하면 진행 중인 불러오기를 멈추고 처음부터 다시 불러온다`() {
        var cancelledCount = 0
        val server = server()
        coEvery { server(0) } coAnswers {
            try {
                awaitCancellation()
            } finally {
                cancelledCount++
            }
        }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        cancelledCount shouldBe 1
        bodyProgress().assertExists()
    }

    @Test
    fun `TC-4bdd8bc6-139d-4990-8f2f-9f1419454924 목록의 파일을 선택하면 그 파일의 FileDetail 화면으로 이동한다`() {
        val file = fixtureMonkey.diaryFile()

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file, fixtureMonkey.diaryFile()))))
        composeRule.onNodeWithText(file.name).performClick()
        composeRule.waitForIdle()

        screen.navigateToDetailIdList shouldBe listOf(file.id)
        screen.navigateToAddCount shouldBe 0
    }

    @Test
    fun `TC-4bdd8bc6-139d-4990-8f2f-9f1419454924 올리는 중에도 목록의 파일을 선택하면 FileDetail 화면으로 이동한다`() {
        val file = fixtureMonkey.diaryFile()

        val screen = setFileHomeScreen(server = server(pageList = listOf(listOf(file))))
        startUpload()
        composeRule.onNodeWithText(file.name).performClick()
        composeRule.waitForIdle()

        screen.navigateToDetailIdList shouldBe listOf(file.id)
    }

    @Test
    fun `TC-7062b208-3784-4e82-b622-75fc8ac830db FileDetail에서 고친 제목과 파일 이름이 목록에 다시 불러오지 않고 보인다`() {
        val fileList = fileList(count = 3)
        val target = fileList[1].copy(title = OLD_TITLE, name = OLD_NAME)
        val shownList = listOf(fileList[0], target, fileList[2])
        val server = server(pageList = listOf(shownList))
        val updated = MutableStateFlow<Map<Uuid, DiaryFile>>(emptyMap())

        setFileHomeScreen(server = server, updated = updated)
        composeRule.onNodeWithText(OLD_TITLE).assertExists()
        composeRule.runOnIdle { updated.value = mapOf(target.id to target.copy(title = NEW_TITLE, name = NEW_NAME)) }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { server(0) }
        composeRule.onNodeWithText(NEW_TITLE).assertExists()
        composeRule.onNodeWithText(NEW_NAME).assertExists()
        composeRule.onNodeWithText(OLD_TITLE).assertDoesNotExist()
        assertAbove(upper = fileList[0], lower = fileList[2])
        val newTop = composeRule.onNodeWithText(NEW_NAME).getUnclippedBoundsInRoot().top
        (composeRule.onNodeWithText(fileList[0].name).getUnclippedBoundsInRoot().top < newTop) shouldBe true
        (newTop < composeRule.onNodeWithText(fileList[2].name).getUnclippedBoundsInRoot().top) shouldBe true
    }

    @Test
    fun `TC-44fcf1ca-d962-4253-9a4a-e633d0d1588c 고친 뒤 목록을 처음부터 다시 불러오면 서버가 돌려준 값으로 보인다`() {
        val target = fixtureMonkey.diaryFile()
        val updated = MutableStateFlow(mapOf(target.id to target.copy(title = NEW_TITLE)))
        val server = server()
        var requestCount = 0
        // 고친 뒤 시작한 불러오기가 그 파일을 받아 오면 저장소가 덧씌운 값을 걷는다.
        coEvery { server(0) } coAnswers {
            requestCount += 1
            if (requestCount == 1) {
                listOf(target)
            } else {
                updated.value = emptyMap()
                listOf(target.copy(title = OTHER_DEVICE_TITLE))
            }
        }

        setFileHomeScreen(server = server, updated = updated)
        composeRule.onNodeWithText(NEW_TITLE).assertExists()
        pull()

        composeRule.onNodeWithText(OTHER_DEVICE_TITLE).assertExists()
        composeRule.onNodeWithText(NEW_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-d144a188-a4a5-4b91-8cd3-9c558f8d8d3d 화면을 보고 있지 않을 때 끝난 올리기의 결과는 화면에 들어와도 알리지 않는다`() {
        assertFinishedWhileAwayIsNotNotified()
    }

    @Test
    fun `TC-093a77f7-1e9e-4ea6-9d9d-f418f6fee728 목록의 계정이 바뀌면 이전 계정의 파일 없이 새 계정의 목록을 처음부터 보여 준다`() {
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
    fun `TC-5c4fce25-16e8-4b9b-b877-7be680796430 사용자에서 게스트로 바뀌면 목록 대신 로그인 안내를 표시한다`() {
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
    fun `TC-b8d761da-c33c-4c46-8e54-220f2c264da6 화면이 재생성되어도 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
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
    fun `TC-b8d761da-c33c-4c46-8e54-220f2c264da6 앱이 백그라운드에 다녀와도 목록을 다시 불러오지 않고 보던 자리를 유지한다`() {
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
    fun `TC-2ccaa189-1939-4b2f-8e70-7e1723e3e24c 화면을 떠났다가 다시 들어오면 목록을 처음부터 다시 불러와 처음부터 보여 준다`() {
        assertReentryReloadsFromTop(isUploading = false)
    }

    @Test
    fun `TC-2ccaa189-1939-4b2f-8e70-7e1723e3e24c 떠나기 전에 시작한 올리기가 끝나지 않았으면 다시 들어와도 진행 중 표시를 보인다`() {
        assertReentryReloadsFromTop(isUploading = true)
    }

    @Test
    fun `TC-02575c48-f965-4cac-bb7b-8ead46ecc395 앱을 다시 실행하면 목록을 처음부터 다시 불러와 처음부터 보여 준다`() {
        val fileList = fileList(count = 30)
        val relaunchedServer = server(pageList = fileList.chunked(PAGE_SIZE))
        var launchCount by mutableIntStateOf(0)
        var nextScreen = fileHomeScreen(server = server(pageList = fileList.chunked(PAGE_SIZE)))
        // 앱을 다시 실행하면 저장된 화면 상태와 화면 상태 홀더가 모두 사라지므로, 새 화면으로 저장 상태 없이 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                val screen = remember { nextScreen }
                screen.Content()
            }
        }
        composeRule.waitForIdle()
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.onNodeWithText(fileList.first().name).assertDoesNotExist()
        nextScreen = fileHomeScreen(server = relaunchedServer)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { relaunchedServer(0) }
        composeRule.onNodeWithText(fileList.first().name).assertIsDisplayed()
    }

    @Test
    fun `TC-7770e14f-351e-4847-aa5f-5dc29decb9c6 올리는 동안 화면이 재생성되어도 진행 표시와 결과 반영이 이어진다`() {
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns listOf(fixtureMonkey.diaryFile()) andThen listOf(uploaded)
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server, upload = upload)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        startUpload()
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
    fun `TC-7770e14f-351e-4847-aa5f-5dc29decb9c6 올리는 동안 앱이 백그라운드에 다녀와도 진행 표시와 결과 반영이 이어진다`() {
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
        startUpload()
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
    fun `TC-cfdfb082-926f-421d-82cb-0dc590b0b2bd 계정 상태를 확인하지 못하면 본문을 비우고 따로 알리지 않으며 올리기는 이어진다`() {
        val file = fixtureMonkey.diaryFile()
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server(pageList = listOf(listOf(file))), accountFlow = accountFlow, upload = upload)
        startUpload()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TITLE).assertExists()
        composeRule.onNodeWithText(file.name).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(GUEST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-ebdf6ffc-9d03-4d2f-8ca0-0bd02cf8e9b6 계정 상태를 잠시 확인하지 못했다가 같은 계정으로 돌아오면 목록을 다시 불러오지 않는다`() {
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
    fun `TC-cce0fe3f-3e69-41d7-906b-c03ce2110e67 게스트에서 사용자로 바뀌면 목록을 불러오고 파일 추가를 제공한다`() {
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
    fun `TC-e3b11a13-f682-43d8-ab6e-e76754b09b59 화면이 재생성되면 보이던 안내가 닫히고 다시 나타나지 않는다`() {
        val upload = FileUploadMocks()
        val screen = fileHomeScreen(server = server(pageList = listOf(listOf(fixtureMonkey.diaryFile()))), upload = upload)
        val tester = StateRestorationTester(composeRule)

        tester.setContent { screen.Content() }
        composeRule.waitForIdle()
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
        tester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-e3b11a13-f682-43d8-ab6e-e76754b09b59 앱이 백그라운드에 다녀와도 보이던 안내가 남는다`() {
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
        startUpload()
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
    fun `TC-75eedda4-ed45-448b-878c-f4dbc5d4c991 시스템이 앱을 정리했다가 다시 만들면 목록을 처음부터 다시 불러오고 보던 파일이 앞부분에 있으면 그 자리를 되살린다`() {
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
    fun `TC-75eedda4-ed45-448b-878c-f4dbc5d4c991 시스템이 앱을 정리했다가 다시 만들면 보던 파일이 새로 불러온 앞부분에 없을 때 그 끝 부분을 보여 주고 이어서 불러온다`() {
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
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        uploadProgress().assertDoesNotExist()
        bodyProgress().assertDoesNotExist()
        composeRule.onNodeWithContentDescription(REFRESHING, useUnmergedTree = true).assertExists()
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
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.waitForIdle()
        bodyProgress().assertExists()
        response.complete(listOf(uploaded))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(uploaded.name).assertExists()
        composeRule.onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    private fun assertBodyLoadFailed(
        server: suspend (Int) -> List<DiaryFile>,
        fileList: List<DiaryFile>,
    ) {
        coVerify(exactly = 2) { server(0) }
        composeRule.onAllNodesWithText(LOAD_FAILED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(RETRY).assertExists()
        fileList.forEach { file -> composeRule.onNodeWithText(file.name).assertDoesNotExist() }
    }

    // FileAdd나 FileDetail이 위에 열리면 FileHome은 화면에서 내려가 결과를 받지 못하고, 돌아왔을 때 쌓여 있던 성공을 받는다.
    private fun assertSucceededWhileHiddenReturnsToTop() {
        val fileList = fileList(count = 30)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns fileList.take(PAGE_SIZE) andThen (listOf(uploaded) + fileList).take(PAGE_SIZE)
        coEvery { server(1) } returns fileList.drop(PAGE_SIZE)
        val upload = FileUploadMocks()
        var isFileHomeVisible by mutableStateOf(true)
        val screen = fileHomeScreen(server = server, upload = upload)

        setBehindFileAddContent(screen = screen, isFileHomeVisibleProvider = { isFileHomeVisible })
        startUpload()
        scrollListTo(index = PAGE_SIZE - 1)
        composeRule.runOnIdle { isFileHomeVisible = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id)) }
        composeRule.runOnIdle { isFileHomeVisible = true }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { server(0) }
        composeRule.onNodeWithText(uploaded.name).assertIsDisplayed()
        assertAbove(upper = uploaded, lower = fileList.first())
        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertDoesNotExist()
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
        if (isUploading) startUpload()
        composeRule.runOnIdle { screen = fileHomeScreen(server = secondServer, upload = upload) }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { secondServer(0) }
        composeRule.onNodeWithText(fileList.first().name).assertExists()
        if (isUploading) uploadProgress().assertExists() else uploadProgress().assertDoesNotExist()
    }

    private fun assertAddNavigatesToFileAdd(server: suspend (Int) -> List<DiaryFile>) {
        val screen = setFileHomeScreen(server = server)

        clickAdd()

        screen.navigateToAddCount shouldBe 1
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
    fun `TC-1a8c9a93-df0e-4773-81ef-ed56e332e3d6 당겨서 다시 불러오는 동안 올리기에 성공하면 진행 중인 불러오기를 멈추고 처음부터 다시 불러온다`() {
        var cancelledCount = 0
        val server = server()
        coEvery { server(0) } returns fileList(count = 5) coAndThen {
            try {
                awaitCancellation()
            } finally {
                cancelledCount++
            }
        }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        pull()
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        coVerify(exactly = 3) { server(0) }
        cancelledCount shouldBe 1
    }

    @Test
    fun `TC-8179e973-096f-4660-8c93-3c27ca946b5d 게스트가 되었다가 같은 계정으로 다시 로그인하면 목록을 처음부터 보여 준다`() {
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
    fun `TC-2570a905-6ff4-4ab4-a0f0-7fb2c60d2e87 본문의 처음 불러오기 실패 안내는 화면이 재생성되어도 그대로 보인다`() {
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
    fun `TC-2570a905-6ff4-4ab4-a0f0-7fb2c60d2e87 목록 끝의 이어서 불러오기 실패 안내는 화면이 재생성되어도 그대로 보인다`() {
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
    fun `TC-a942b087-a4cd-4b43-bf84-db74cb01678e 계정 상태를 확정하지 않은 동안 올리기에 실패하면 실패 안내를 표시한다`() {
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server(pageList = listOf(fileList(count = 1))), accountFlow = accountFlow, upload = upload)
        startUpload()
        composeRule.runOnIdle { accountFlow.value = Result.failure(failure()) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Failed) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-a942b087-a4cd-4b43-bf84-db74cb01678e 계정 상태를 확정하지 않은 동안 올리기에 성공하면 다시 불러온 목록을 계정이 확인된 뒤 보여 준다`() {
        val account = fixtureMonkey.giveMeOne<Account.User>()
        val before = fileList(count = 1)
        val uploaded = fixtureMonkey.diaryFile(name = UPLOADED_FILE_NAME)
        val server = server()
        coEvery { server(0) } returns before andThen listOf(uploaded) + before
        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, accountFlow = accountFlow, upload = upload)
        startUpload()
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
    fun `TC-00615cba-60ba-4905-9cea-bd710accd007 다시 불러오는 중에 계정이 바뀌면 앞선 계정의 파일이 새 목록에 나타나지 않는다`() {
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
    fun `TC-8d5f4d26-8a00-45ab-abdc-4bc9e109bf42 올리기 성공으로 다시 불러오는 동안 당겨도 불러오기는 한 번만 진행된다`() {
        val server = server()
        coEvery { server(0) } returns fileList(count = 5) coAndThen { awaitCancellation() }
        val upload = FileUploadMocks()

        setFileHomeScreen(server = server, upload = upload)
        startUpload()
        composeRule.runOnIdle { upload.finish(event = FileUploadEvent.Succeeded(fileId = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        pull()

        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-99ab8ac1-35ca-4ea4-abcb-85912746b65f 당겨서 다시 불러오는 동안에도 이전 목록을 끝까지 이동할 수 있다`() {
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
    fun `TC-00baa3e3-1e9a-4e68-815f-9ca9d93bcc87 당겨서 다시 불러오는 중에 화면이 재생성되면 본문에 불러오는 중을 표시한다`() {
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

        bodyProgress().assertExists()
        coVerify(exactly = 2) { server(0) }
    }

    @Test
    fun `TC-00baa3e3-1e9a-4e68-815f-9ca9d93bcc87 당겨서 다시 불러오는 중에 화면이 재생성된 뒤 실패하면 본문에 실패 안내를 표시한다`() {
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

        assertBodyLoadFailed(server = server, fileList = fileList)
    }

    @Test
    fun `TC-00baa3e3-1e9a-4e68-815f-9ca9d93bcc87 올리기 성공으로 다시 불러오는 중에 화면이 재생성되어도 새 목록의 처음부터 보인다`() {
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
        startUpload()
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
        upload: FileUploadMocks = FileUploadMocks(),
        updated: MutableStateFlow<Map<Uuid, DiaryFile>> = MutableStateFlow(emptyMap()),
    ): FileHomeTestScreen {
        val screen =
            fileHomeScreen(
                server = server,
                paging = paging,
                accountFlow = accountFlow,
                navigateUp = navigateUp,
                upload = upload,
                updated = updated,
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
        upload: FileUploadMocks = FileUploadMocks(),
        updated: MutableStateFlow<Map<Uuid, DiaryFile>> = MutableStateFlow(emptyMap()),
    ): FileHomeTestScreen {
        val getUpdatedFileUseCase = mockk<GetUpdatedFileUseCase>()
        every { getUpdatedFileUseCase(parameter = Unit) } returns updated.map { fileMap -> Result.success(fileMap) }
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow
        val uploadViewModel = upload.viewModel()

        lastUpload = upload

        return FileHomeTestScreen(
            navigateUp = navigateUp,
            fileViewModel =
                FileHomeViewModel(
                    getAccountUseCase = getAccountUseCase,
                    pageFileUseCase = pageFileUseCase(paging = paging),
                    getUpdatedFileUseCase = getUpdatedFileUseCase,
                ),
            uploadViewModel = uploadViewModel,
        )
    }

    // FileAdd가 위에 열리면 FileHome은 화면에서 내려가지만, 내비게이션이 저장해 둔 상태로 돌아온다.
    private fun setBehindFileAddContent(
        screen: FileHomeTestScreen,
        isFileHomeVisibleProvider: () -> Boolean,
    ) {
        composeRule.setContent {
            val holder = rememberSaveableStateHolder()

            if (isFileHomeVisibleProvider()) {
                holder.SaveableStateProvider(key = FILE_HOME_KEY) { screen.Content() }
            }
        }
        composeRule.waitForIdle()
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

    // 올리기는 FileAdd에서 시작하므로, 이 화면의 테스트는 올리기가 시작된 상태를 직접 만든다.
    private fun startUpload() {
        composeRule.runOnIdle { checkNotNull(lastUpload).start() }
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
        private const val FILE_HOME_KEY = "FileHome"
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
        private const val OLD_TITLE = "옛 제목"
        private const val NEW_TITLE = "새 제목"
        private const val OLD_NAME = "old.txt"
        private const val NEW_NAME = "new.txt"
        private const val OTHER_DEVICE_TITLE = "다른 기기 제목"
    }
}

private class FileHomeTestScreen(
    val navigateUp: () -> Unit,
    val fileViewModel: FileHomeViewModel,
    val uploadViewModel: FileHomeUploadViewModel,
) {
    var navigateToAddCount: Int = 0
        private set

    val navigateToDetailIdList: MutableList<Uuid> = mutableListOf()

    @Composable
    fun Content() {
        DiaryTheme {
            FileHomeScreen(
                navigateUp = navigateUp,
                navigateToAdd = { navigateToAddCount += 1 },
                navigateToDetail = { id -> navigateToDetailIdList += id },
                componentVisibleProvider = { FileHomeScaffoldComponentVisible() },
                fileViewModel = fileViewModel,
                uploadViewModel = uploadViewModel,
            )
        }
    }
}

// 올리기의 상태와 결과는 mock이 돌려주는 흐름으로 제어한다. 결과는 그때 받는 화면에만 가고, 성공은 FileHome이 받을 때까지 쌓인다.
private class FileUploadMocks {
    val state = MutableStateFlow<FileUploadState>(FileUploadState.Idle)
    private val eventFlow = MutableSharedFlow<FileUploadEvent>(extraBufferCapacity = 16)
    private val succeededChannel = Channel<FileUploadEvent.Succeeded>(Channel.UNLIMITED)

    private val getFileUploadStateUseCase: GetFileUploadStateUseCase =
        mockk<GetFileUploadStateUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns state.map { value -> Result.success(value) }
        }

    private val getFileUploadEventUseCase: GetFileUploadEventUseCase =
        mockk<GetFileUploadEventUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns eventFlow.map { event -> Result.success(event) }
        }

    private val getFileUploadSucceededEventUseCase: GetFileUploadSucceededEventUseCase =
        mockk<GetFileUploadSucceededEventUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns succeededChannel.receiveAsFlow().map { event -> Result.success(event) }
        }

    fun start() {
        state.value = FileUploadState.Uploading(percent = null)
    }

    fun finish(event: FileUploadEvent) {
        state.value = FileUploadState.Idle
        eventFlow.tryEmit(event)
        if (event is FileUploadEvent.Succeeded) succeededChannel.trySend(event)
    }

    fun finishWithoutEvent() {
        state.value = FileUploadState.Idle
    }

    fun viewModel(): FileHomeUploadViewModel =
        FileHomeUploadViewModel(
            getFileUploadStateUseCase = getFileUploadStateUseCase,
            getFileUploadEventUseCase = getFileUploadEventUseCase,
            getFileUploadSucceededEventUseCase = getFileUploadSucceededEventUseCase,
        )
}

// 테스트가 계정을 바꿀 때 보여 줄 목록의 흐름도 함께 바꾼다.
private fun pageFileUseCase(paging: Flow<Flow<PagingData<DiaryFile>>>): PageFileUseCase {
    val useCase = mockk<PageFileUseCase>()
    every { useCase(parameter = Unit) } returns paging.flatMapLatest { pagingData -> pagingData }.map { pagingData -> Result.success(pagingData) }
    return useCase
}

private fun pagerFlow(server: suspend (Int) -> List<DiaryFile>): Flow<PagingData<DiaryFile>> {
    val flow =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { pagingSource(server = server) },
        ).flow

    return flow.onStart { emit(PagingData.empty(sourceLoadStates = LOADING_LOAD_STATES)) }
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
