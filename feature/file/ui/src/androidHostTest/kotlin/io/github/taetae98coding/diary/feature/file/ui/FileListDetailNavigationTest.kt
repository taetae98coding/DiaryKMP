@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.file.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.nonBlankDiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileScreenUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileScreenUseCase
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddAccountViewModel
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddMocks
import io.github.taetae98coding.diary.feature.file.ui.add.FileAddViewModel
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailAccountViewModel
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailMocks
import io.github.taetae98coding.diary.feature.file.ui.detail.FileDetailViewModel
import io.github.taetae98coding.diary.feature.file.ui.home.FILE_HOME_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeRefreshUiState
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeRefreshViewModel
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeUiState
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeUploadViewModel
import io.github.taetae98coding.diary.feature.file.ui.home.FileHomeViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 fileEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w1280dp-h1600dp")
class FileListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(NavigationTestMoreNavKey, FileHomeNavKey)
    private val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
    private val homeUiState = MutableStateFlow<FileHomeUiState>(FileHomeUiState.User(accountId = fixtureMonkey.giveMeOne<Uuid>()))
    private val uploadEventChannel = Channel<FileUploadEvent>(Channel.UNLIMITED)
    private val startViewingFileScreenUseCase =
        mockk<StartViewingFileScreenUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }
    private val stopViewingFileScreenUseCase =
        mockk<StopViewingFileScreenUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }
    private val refreshViewModelList = mutableListOf<FileHomeRefreshViewModel>()

    // null이면 테스트 창 크기로 정한 배치를 쓰고, 값을 두면 창 너비가 바뀐 것처럼 배치를 바꾼다.
    private var directiveOverride by mutableStateOf<PaneScaffoldDirective?>(null)
    private val addMocks = FileAddMocks()
    private val addViewModelList = mutableListOf<FileAddViewModel>()
    private val homePagingData = MutableStateFlow(PagingData.empty<DiaryFile>())

    // 목록을 다시 불러올 때 서버가 돌려줄 목록. null이면 목록을 바꾸지 않는다.
    private var refreshedFileList: List<DiaryFile>? = null

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-7689a1a6-3e58-4736-b647-7db6963633b2 목록과 상세를 함께 쓰는 환경에서는 파일 목록과 FileAdd를 함께 표시한다`() {
        val file = testFile()

        setFileNavDisplay(fileList = listOf(file))

        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko-w411dp-h891dp")
    fun `TC-7a418dcf-96fc-4eb2-851d-494e31e91387 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        listItem(file).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-7e1e3678-bffe-4341-9b07-82c444317948 상세 영역에 FileAdd가 놓이면 목록에서 파일 추가 버튼을 제공하지 않는다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertDoesNotExist()

        openAddOnDetail(file = file)

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-e8b1d116-f857-4329-acb8-5fd73bad2f05 상세 영역에 FileDetail이 놓이면 목록에서 파일 추가 버튼을 제공한다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        openDetail(file = file)

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-68250e6a-ee8d-434b-8f3e-c3e66a787f4b 목록과 함께 표시되면 상세 영역의 FileAdd와 FileDetail에서 뒤로가기 버튼을 제공하지 않는다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)

        openDetail(file = file)

        composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "ko-w411dp-h891dp")
    fun `TC-b3a7960f-8eef-4979-814a-c3d29c8ee0e4 단독으로 표시되면 FileAdd와 FileDetail에서 뒤로가기 버튼을 제공한다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
        composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        openDetail(file = file)

        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
        composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-e3e8c3de-6912-43b5-a584-e0623ecdc26a 목록에서 파일을 선택하면 목록을 유지한 채 상세 영역이 그 파일의 FileDetail로 바뀐다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        openDetail(file = file)

        backStack.last() shouldBe FileDetailNavKey(id = file.id)
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-e810d0ab-2004-4514-8d74-590ba541f912 상세 영역에서 뒤로가면 FileAdd로 되돌아간다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openDetail(file = file)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-c93a7a45-a0f0-4805-84e0-8fe72a3c63c1 FileDetail이 놓인 동안 파일 추가를 실행하면 상세 영역이 FileAdd로 바뀐다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        openAddOnDetail(file = file)

        backStack.last() shouldBe FileAddNavKey
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-91d24273-310d-44e6-bfc0-9f03c8df9c23 FileDetail 위에서 연 FileAdd에서 뒤로가면 이전 FileDetail로 돌아간다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openAddOnDetail(file = file)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey, FileDetailNavKey(id = file.id))
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-872d944e-87d5-468c-a55c-4d855f6e4d4b FileDetail 위에서 연 FileAdd는 빈 입력과 고른 파일 없음으로 시작한다`() {
        val file = testFile()
        val source = fixtureMonkey.fileUploadSource()
        setFileNavDisplay(fileList = listOf(file))
        fillPlaceholder(title = typedTitle(), description = typedDescription())
        selectPlaceholderFile(source = source)

        openAddOnDetail(file = file)

        backStack.last() shouldBe FileAddNavKey
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        titleInput().assert(hasText(""))
        descriptionInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
        composeRule.onNodeWithText(source.name).assertDoesNotExist()
    }

    @Test
    fun `TC-2fd4f0fe-865d-4c52-95e8-e8054c97dc8d 선택 전 FileAdd에서 올리기를 시작해도 상세 영역은 FileAdd로 남는다`() {
        val file = testFile()
        val source = fixtureMonkey.fileUploadSource()
        setFileNavDisplay(fileList = listOf(file))
        fillPlaceholder(title = typedTitle(), description = typedDescription())
        selectPlaceholderFile(source = source)

        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        addMocks.requestList shouldHaveSize 1
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
        titleInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
    }

    @Test
    fun `TC-9c8360d6-d01d-4e46-92e8-97ae6585cfea 선택 전 FileAdd에서 올린 파일은 성공하면 같은 화면의 목록에 나타난다`() {
        val file = testFile()
        val uploaded = testFile()
        refreshedFileList = listOf(uploaded, file)
        setFileNavDisplay(fileList = listOf(file))
        composeRule.runOnIdle { addMocks.state.value = FileUploadState.Uploading(percent = null) }
        composeRule.waitForIdle()
        listItem(uploaded).assertDoesNotExist()

        composeRule.runOnIdle {
            addMocks.finish(event = FileUploadEvent.Succeeded(fileId = uploaded.id))
            uploadEventChannel.trySend(FileUploadEvent.SucceededOnFileAdd(fileId = uploaded.id))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        listItem(uploaded).assertExists()
    }

    @Test
    fun `TC-57f0766c-de93-4842-95fb-78154c61fa71 올리는 중에도 목록에서 파일을 선택해 상세 영역에 FileDetail을 연다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        composeRule.runOnIdle { addMocks.state.value = FileUploadState.Uploading(percent = null) }
        composeRule.waitForIdle()

        openDetail(file = file)

        backStack.last() shouldBe FileDetailNavKey(id = file.id)
        listItem(file).assertExists()
    }

    @Test
    fun `TC-28f6f7bf-80cc-467a-8f3a-b5a40ddda245 목록에서 연 FileAdd를 뒤로가기로 닫아도 선택 전 FileAdd의 작성 내용이 남는다`() {
        val file = testFile()
        val input = fillPlaceholderWithFile(fileList = listOf(file))
        openAddOnDetail(file = file)

        pressBack()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
        pressBack()

        assertPlaceholderKept(input = input)
    }

    @Test
    fun `TC-28f6f7bf-80cc-467a-8f3a-b5a40ddda245 목록에서 연 FileAdd를 다른 파일을 골라 걷어내도 선택 전 FileAdd의 작성 내용이 남는다`() {
        val first = testFile()
        val second = testFile()
        val input = fillPlaceholderWithFile(fileList = listOf(first, second))
        openAddOnDetail(file = first)

        openDetail(file = second)
        pressBack()

        assertPlaceholderKept(input = input)
    }

    @Test
    fun `TC-3138af23-fb7b-408c-8a86-92eead539d42 FileDetail이 놓인 동안 다른 파일을 고르면 교체하고 뒤로가면 FileAdd로 돌아간다`() {
        val first = testFile()
        val second = testFile()
        setFileNavDisplay(fileList = listOf(first, second))

        openDetail(file = first)
        openDetail(file = second)

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey, FileDetailNavKey(id = second.id))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-d314030a-4b36-4de7-b07b-eb59d6d8cd8b FileDetail 위에서 연 FileAdd가 놓인 동안 다른 파일을 고르면 FileAdd를 걷어내고 그 FileDetail로 바꾼다`() {
        val first = testFile()
        val second = testFile()
        val title = typedTitle()
        setFileNavDisplay(fileList = listOf(first, second))
        openAddOnDetail(file = first)
        titleInput().performTextInput(title)
        composeRule.waitForIdle()

        openDetail(file = second)

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey, FileDetailNavKey(id = second.id))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(title).assertDoesNotExist()
        titleInput().assert(hasText(""))
    }

    @Test
    fun `TC-1e76486e-2f0c-4a6a-bbce-5f40539596f1 파일을 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        listItem(file).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0af65ecc-01ad-4693-83bb-945d52fa4fb7 상세 영역에 FileDetail이 놓인 동안 끝난 올리기의 실패는 목록에서 알린다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openDetail(file = file)

        composeRule.runOnIdle { uploadEventChannel.trySend(FileUploadEvent.Failed) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPLOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-1bd00936-52b0-4d58-b4d4-f84c9fe30902 상세 영역의 FileDetail이 계정 변경으로 닫히면 FileAdd로 되돌아간다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openDetail(file = file)

        composeRule.runOnIdle { accountFlow.value = Result.success(fixtureMonkey.giveMeOne<Account.User>()) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-efa25862-1b55-4fd6-ad3f-ab31b4149ca3 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openDetail(file = file)

        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        reenterFileHome()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-dbd90688-c4d1-468e-a939-728e3263703b 더보기로 돌아갔다가 다시 진입하면 상세 영역의 FileAdd는 빈 상태로 시작한다`() {
        val source = fixtureMonkey.fileUploadSource()
        setFileNavDisplay(fileList = listOf(testFile()))
        fillPlaceholder(title = typedTitle(), description = typedDescription())
        selectPlaceholderFile(source = source)

        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        reenterFileHome()

        titleInput().assert(hasText(""))
        descriptionInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
        composeRule.onNodeWithText(source.name).assertDoesNotExist()
        addViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-49230831-151f-434d-b14a-22fef676ed93 배치를 떠나기 전에는 상세 영역에서 FileDetail을 보고 돌아와도 FileAdd의 작성 내용이 남는다`() {
        val file = testFile()
        val title = typedTitle()
        val description = typedDescription()
        val source = fixtureMonkey.fileUploadSource()
        setFileNavDisplay(fileList = listOf(file))
        fillPlaceholder(title = title, description = description)
        selectPlaceholderFile(source = source)

        openDetail(file = file)
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()
        pressBack()

        titleInput().assert(hasText(title))
        descriptionInput().assert(hasText(description))
        composeRule.onNodeWithText(source.name).assertExists()
        addViewModelList shouldHaveSize 1
    }

    @Test
    fun `TC-86d19c58-91c6-426c-bc18-470661404a39 게스트가 되면 선택한 파일이 없을 때의 상세 영역을 비우고 작성 내용을 남기지 않는다`() {
        val source = fixtureMonkey.fileUploadSource()
        setFileNavDisplay(fileList = listOf(testFile()))
        fillPlaceholder(title = typedTitle(), description = typedDescription())
        selectPlaceholderFile(source = source)

        changeAccount(account = Account.Guest, homeState = FileHomeUiState.Guest)

        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        val user = fixtureMonkey.giveMeOne<Account.User>()
        changeAccount(account = user, homeState = FileHomeUiState.User(accountId = user.id))

        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
        titleInput().assert(hasText(""))
        composeRule.onNodeWithText(NO_FILE_SELECTED).assertExists()
        composeRule.onNodeWithText(source.name).assertDoesNotExist()
    }

    @Test
    fun `TC-dde99079-94ce-4220-93cd-88b9078538ac 계정 상태를 확정하지 않은 동안에는 상세 영역의 FileAdd와 작성 내용을 그대로 둔다`() {
        val title = typedTitle()
        setFileNavDisplay(fileList = listOf(testFile()))
        fillPlaceholder(title = title, description = typedDescription())

        changeAccount(result = Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())), homeState = FileHomeUiState.Loading)

        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
        titleInput().assert(hasText(title))
    }

    @Test
    @Config(qualifiers = "ko-w411dp-h891dp")
    fun `TC-e8b1d116-f857-4329-acb8-5fd73bad2f05 상세 영역을 함께 표시하지 않으면 목록에서 파일 추가 버튼을 제공한다`() {
        setFileNavDisplay(fileList = listOf(testFile()))

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-d314030a-4b36-4de7-b07b-eb59d6d8cd8b 좁은 창에서 연 FileAdd가 창이 넓어져 상세 영역에 놓인 동안 다른 파일을 고르면 FileAdd를 걷어내고 그 FileDetail로 바꾼다`() {
        val file = testFile()
        val title = typedTitle()
        directiveOverride = singlePaneDirective()
        setFileNavDisplay(fileList = listOf(file))
        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        listItem(file).assertDoesNotExist()
        titleInput().performTextInput(title)
        composeRule.waitForIdle()

        composeRule.runOnIdle { directiveOverride = null }
        composeRule.waitForIdle()
        listItem(file).assertExists()
        openDetail(file = file)

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey, FileDetailNavKey(id = file.id))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithText(title).assertDoesNotExist()
        titleInput().assert(hasText(""))
    }

    @Test
    fun `TC-1e76486e-2f0c-4a6a-bbce-5f40539596f1 게스트 상태라 상세 영역이 비어 있을 때 뒤로가면 더보기로 돌아간다`() {
        setFileNavDisplay(fileList = listOf(testFile()))
        changeAccount(account = Account.Guest, homeState = FileHomeUiState.Guest)
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-1bd00936-52b0-4d58-b4d4-f84c9fe30902 FileDetail 위에서 연 FileAdd가 놓인 동안 게스트가 되면 FileAdd와 FileDetail이 모두 닫히고 상세 영역이 빈다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        openAddOnDetail(file = file)

        changeAccount(account = Account.Guest, homeState = FileHomeUiState.Guest)

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-c5b955f9-725d-406e-8813-5798ce866445 FileDetail 위에서 연 FileAdd가 놓인 동안 다른 사용자로 바뀌면 FileAdd는 남고 뒤로가면 FileDetail이 닫힌다`() {
        val file = testFile()
        val title = typedTitle()
        setFileNavDisplay(fileList = listOf(file))
        openAddOnDetail(file = file)
        titleInput().performTextInput(title)
        composeRule.waitForIdle()

        val user = fixtureMonkey.giveMeOne<Account.User>()
        changeAccount(account = user, homeState = FileHomeUiState.User(accountId = user.id))

        backStack.last() shouldBe FileAddNavKey
        titleInput().assert(hasText(title))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-93ef7888-533c-419e-ab80-d0ca6304a6f8 파일 목록에서 진입하지 않은 FileAdd와 FileDetail은 단독으로 표시한다`() {
        val file = testFile()
        val caseList =
            listOf(
                FileAddNavKey to UPLOAD_DESCRIPTION,
                FileDetailNavKey(id = file.id) to DOWNLOAD_DESCRIPTION,
            )

        caseList.forEachIndexed { index, (key, description) ->
            if (index == 0) {
                backStack.removeLastOrNull()
                backStack.add(key)
                setFileNavDisplay(fileList = listOf(file))
            } else {
                composeRule.runOnIdle {
                    backStack.removeLastOrNull()
                    backStack.add(key)
                }
                composeRule.waitForIdle()
            }

            composeRule.onNodeWithContentDescription(description).assertExists()
            composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).assertDoesNotExist()
            composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
        }
    }

    @Test
    fun `TC-675a9714-031d-420a-8df3-639645c0a8c6 파일 목록에서 이어 진입한 FileAdd와 FileDetail은 목록과 함께 표시한다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))

        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()

        openDetail(file = file)
        listItem(file).assertExists()

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        listItem(file).assertExists()
        composeRule.onNodeWithContentDescription(UPLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-f460776c-ceec-4447-8e4b-c1260ee8047a 상세 영역의 화면이 바뀌어도 목록은 보던 자리를 유지하고 다시 불러오지 않는다`() {
        val fileList = List(30) { testFile() }
        setFileNavDisplay(fileList = fileList)
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).performScrollToIndex(25)
        composeRule.waitForIdle()
        listItem(fileList.first()).assertDoesNotExist()

        openDetail(file = fileList[25])
        pressBack()

        listItem(fileList[25]).assertExists()
        listItem(fileList.first()).assertDoesNotExist()
        refreshViewModelList shouldHaveSize 1
        verify(exactly = 0) { refreshViewModelList.single().refresh() }
    }

    @Test
    fun `TC-ab621b74-2356-4469-b0e5-d54403ff5e52 목록과 함께 상세 영역에 FileDetail을 두면 FileHome을 계속 보고 있다고 알린다`() {
        val file = testFile()
        setFileNavDisplay(fileList = listOf(file))
        coVerify(exactly = 1) { startViewingFileScreenUseCase(parameter = FileScreen.HOME) }

        openDetail(file = file)

        coVerify(exactly = 1) { startViewingFileScreenUseCase(parameter = FileScreen.HOME) }
        coVerify(exactly = 0) { stopViewingFileScreenUseCase(parameter = FileScreen.HOME) }
    }

    private fun singlePaneDirective(): PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 1)

    private fun openDetail(file: DiaryFile) {
        listItem(file).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
    }

    private fun openAddOnDetail(file: DiaryFile) {
        openDetail(file = file)

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun fillPlaceholderWithFile(fileList: List<DiaryFile>): PlaceholderInput {
        val input = PlaceholderInput(title = typedTitle(), description = typedDescription(), source = fixtureMonkey.fileUploadSource())
        setFileNavDisplay(fileList = fileList)
        fillPlaceholder(title = input.title, description = input.description)
        selectPlaceholderFile(source = input.source)

        return input
    }

    private fun assertPlaceholderKept(input: PlaceholderInput) {
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, FileHomeNavKey)
        titleInput().assert(hasText(input.title))
        descriptionInput().assert(hasText(input.description))
        composeRule.onNodeWithText(input.source.name).assertExists()
    }

    private data class PlaceholderInput(
        val title: String,
        val description: String,
        val source: FileUploadSource,
    )

    private fun fillPlaceholder(
        title: String,
        description: String,
    ) {
        titleInput().performTextInput(title)
        descriptionInput().performTextInput(description)
        composeRule.waitForIdle()
        titleInput().assert(hasText(title))
    }

    // 플랫폼 파일 선택 도구는 테스트에서 띄울 수 없으므로, 도구가 돌려준 파일을 FileAdd가 받은 것과 같이 고른다.
    private fun selectPlaceholderFile(source: FileUploadSource) {
        addMocks.register(uri = source.uri, result = Result.success(source))
        composeRule.runOnIdle { addViewModelList.last().select(uri = source.uri) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(source.name).assertExists()
    }

    private fun changeAccount(
        account: Account,
        homeState: FileHomeUiState,
    ) {
        changeAccount(result = Result.success(account), homeState = homeState)
    }

    private fun changeAccount(
        result: Result<Account>,
        homeState: FileHomeUiState,
    ) {
        composeRule.runOnIdle {
            accountFlow.value = result
            homeUiState.value = homeState
        }
        composeRule.waitForIdle()
    }

    private fun reenterFileHome() {
        composeRule.runOnIdle { backStack.add(FileHomeNavKey) }
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun listItem(file: DiaryFile): SemanticsNodeInteraction = composeRule.onNode(hasText(file.name) and hasAnyAncestor(hasTestTag(FILE_HOME_LIST_TEST_TAG)))

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[0]

    private fun descriptionInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[1]

    private fun testFile(): DiaryFile = fixtureMonkey.nonBlankDiaryFile().copy(name = "file-${fixtureMonkey.giveMeOne<Int>()}.txt")

    private fun typedTitle(): String = "title-${fixtureMonkey.giveMeOne<Int>()}"

    private fun typedDescription(): String = "description-${fixtureMonkey.giveMeOne<Int>()}"

    private fun setFileNavDisplay(fileList: List<DiaryFile>) {
        val module = viewModelModule(fileList = fileList)
        composeRule.setContent {
            FileNavigationTestHost(module = module) {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies =
                            listOf(
                                rememberDiaryListDetailSceneStrategy(
                                    directive = directiveOverride ?: calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
                                ),
                            ),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<NavigationTestMoreNavKey> { Text(text = MORE_CONTENT) }
                                fileEntry(backStack = backStack)
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun FileNavigationTestHost(
        module: Module,
        content: @Composable () -> Unit,
    ) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }
        val resultEventBus = remember { ResultEventBus() }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalResultEventBus provides resultEventBus,
        ) {
            KoinApplication(configuration = koinConfiguration { modules(module) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun viewModelModule(fileList: List<DiaryFile>): Module {
        val getAccountUseCase = mockk<GetAccountUseCase>()
        every { getAccountUseCase(parameter = Unit) } returns accountFlow

        homePagingData.value = PagingData.from(fileList)

        return module {
            factory<FileHomeViewModel> {
                mockk(relaxed = true) {
                    every { filePagingData } returns homePagingData
                    every { uiState } returns homeUiState
                }
            }
            factory<FileHomeUploadViewModel> {
                val getFileUploadStateUseCase = mockk<GetFileUploadStateUseCase>()
                every { getFileUploadStateUseCase(parameter = Unit) } returns addMocks.state.map { state -> Result.success(state) }
                val getFileUploadEventUseCase = mockk<GetFileUploadEventUseCase>()
                every { getFileUploadEventUseCase(parameter = FileScreen.HOME) } returns uploadEventChannel.receiveAsFlow().map { event -> Result.success(event) }

                FileHomeUploadViewModel(
                    startViewingFileScreenUseCase = startViewingFileScreenUseCase,
                    stopViewingFileScreenUseCase = stopViewingFileScreenUseCase,
                    getFileUploadStateUseCase = getFileUploadStateUseCase,
                    getFileUploadEventUseCase = getFileUploadEventUseCase,
                )
            }
            factory<FileHomeRefreshViewModel> {
                mockk<FileHomeRefreshViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(FileHomeRefreshUiState())
                    every { effect } returns emptyFlow()
                    every { refreshAfterUploadOnFileAdd(firstFileIdBefore = any()) } answers {
                        refreshedFileList?.let { fileList -> homePagingData.value = PagingData.from(fileList) }
                    }
                }.also { viewModel -> refreshViewModelList += viewModel }
            }
            factory<FileAddViewModel> { addMocks.viewModel().also { viewModel -> addViewModelList += viewModel } }
            factory<FileAddAccountViewModel> { FileAddAccountViewModel(getAccountUseCase = getAccountUseCase) }
            factory<FileDetailViewModel> { parameters ->
                val id: Uuid = parameters.get()

                FileDetailMocks(file = fileList.first { file -> file.id == id }).viewModel()
            }
            factory<FileDetailAccountViewModel> { FileDetailAccountViewModel(getAccountUseCase = getAccountUseCase) }
        }
    }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val ADD_DESCRIPTION = "파일 추가"
        const val UPLOAD_DESCRIPTION = "파일 올리기"
        const val DOWNLOAD_DESCRIPTION = "다운로드"
        const val NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val NO_FILE_SELECTED = "고른 파일 없음"
        const val UPLOAD_FAILED_MESSAGE = "파일을 올리지 못했습니다."
        const val UPLOAD_SUCCEEDED_MESSAGE = "파일을 올렸습니다."
    }
}

// 파일 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object NavigationTestMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
