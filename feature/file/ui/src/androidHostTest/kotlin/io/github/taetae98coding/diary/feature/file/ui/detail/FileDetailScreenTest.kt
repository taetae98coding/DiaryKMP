package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.testing.file.nonBlankDiaryFile
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.domain.file.exception.FileNameTooLongException
import io.github.taetae98coding.diary.domain.file.usecase.UpdateFileUseCase
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.TimeZone
import kotlin.time.Instant

private const val KILOBYTE_SIZE = 1_024L
private const val MESSAGE_TIMEOUT_MILLIS = 5_000L
private const val DOWNLOAD_SUCCEEDED_PREFIX = "파일을 내려받았습니다"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class FileDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var isContentSet = false
    private var currentScreen by mutableStateOf<FileDetailTestScreen?>(null)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-001 불러오는 동안 불러오는 중임을 표시하고 수정과 다운로드를 제공하지 않는다`() {
        val screen = FileDetailTestScreen()
        screen.mocks.fetchAnswer = { awaitCancellation() }

        setScreen(screen = screen)

        composeRule.onNodeWithContentDescription(LOADING_DESCRIPTION, useUnmergedTree = true).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().shouldBeEmpty()
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertDoesNotExist()
        composeRule.textCount(screen.mocks.file.title) shouldBe 0
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-002 불러오기에 실패하면 실패 안내와 다시 시도를 표시한다`() {
        listOf<Throwable>(NoSuchElementException("not found"), IllegalStateException("connection")).forEach { throwable ->
            val screen = FileDetailTestScreen()
            screen.mocks.fetchAnswer = { Result.failure(throwable) }

            setScreen(screen = screen)

            composeRule.onNodeWithText(LOAD_FAILED).assertExists()
            composeRule.onNodeWithText(RETRY).assertExists()
            composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().shouldBeEmpty()
            composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
            composeRule.textCount(screen.mocks.file.title) shouldBe 0
        }
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-003 다시 시도하면 다시 불러오고 그동안 불러오는 중임을 표시한다`() {
        val screen = FileDetailTestScreen()
        val response = CompletableDeferred<Result<DiaryFile>>()
        var count = 0
        screen.mocks.fetchAnswer = {
            count += 1
            if (count == 1) Result.failure(IllegalStateException(FAILURE_MESSAGE)) else response.await()
        }

        setScreen(screen = screen)
        composeRule.onNodeWithText(RETRY).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(LOADING_DESCRIPTION, useUnmergedTree = true).assertExists()
        count shouldBe 2
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-004 불러오기에 성공하면 파일 정보를 표시한다`() {
        val defaultTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
        try {
            val file =
                fileDetailFixtureMonkey.nonBlankDiaryFile().copy(
                    size = KILOBYTE_SIZE,
                    description = fileDetailFixtureMonkey.nonBlankString(),
                    createdAt = Instant.parse("2026-09-26T06:05:00Z"),
                )

            setScreen(screen = FileDetailTestScreen(mocks = FileDetailMocks(file = file)))

            composeRule.titleInput().assert(hasText(file.title))
            composeRule.descriptionInput().assert(hasText(file.description))
            composeRule.nameInput().assert(hasText(file.name))
            composeRule.onNodeWithText("1.0 KB · 2026. 9. 26. 오후 3:05").assertExists()
            composeRule.textCount(file.title) shouldBe 2
        } finally {
            TimeZone.setDefault(defaultTimeZone)
        }
        composeRule.onNodeWithContentDescription(DOWNLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-005 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setScreen()

        composeRule.titleInput().assertIsNotFocused()
        composeRule.descriptionInput().assertIsNotFocused()
        composeRule.nameInput().assertIsNotFocused()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-006 입력이 저장된 값과 다를 때만 수정 동작을 제공한다`() {
        val file = fileDetailFixtureMonkey.nonBlankDiaryFile()
        val otherTitle = fileDetailFixtureMonkey.nonBlankString()
        val otherDescription = fileDetailFixtureMonkey.nonBlankString()
        val otherName = fileDetailFixtureMonkey.nonBlankString()
        val changeList: List<Pair<() -> Unit, Boolean>> =
            listOf(
                {} to false,
                { composeRule.titleInput().performTextReplacement(otherTitle) } to true,
                { composeRule.descriptionInput().performTextReplacement(otherDescription) } to true,
                { composeRule.nameInput().performTextReplacement(otherName) } to true,
                { composeRule.titleInput().performTextReplacement("${file.title} ") } to true,
                {
                    composeRule.nameInput().performTextReplacement(otherName)
                    composeRule.nameInput().performTextReplacement(file.name)
                } to false,
            )

        changeList.forEach { (change, isVisible) ->
            setScreen(screen = FileDetailTestScreen(mocks = FileDetailMocks(file = file)))
            change()
            composeRule.waitForIdle()

            if (isVisible) {
                composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
            } else {
                composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-007 수정 성립 조건을 어기면 안내하고 초점을 옮기며 입력을 지우지 않는다`() {
        assertInvalid(change = { composeRule.titleInput().performTextClearance() }, message = TITLE_BLANK, focused = { composeRule.titleInput() })
        assertInvalid(change = { composeRule.nameInput().performTextClearance() }, message = NAME_BLANK, focused = { composeRule.nameInput() })
        assertInvalid(
            change = { composeRule.nameInput().performTextReplacement("a".repeat(256)) },
            message = NAME_TOO_LONG,
            focused = { composeRule.nameInput() },
            updateAnswer = { Result.failure(FileNameTooLongException()) },
        )
    }

    @Test
    fun `TC-FILE-DETAIL-DOMAIN-001 제목과 파일 이름을 모두 비우면 제목 안내를 먼저 한다`() {
        assertInvalid(
            change = {
                composeRule.titleInput().performTextClearance()
                composeRule.nameInput().performTextClearance()
            },
            message = TITLE_BLANK,
            focused = { composeRule.titleInput() },
        )
    }

    @Test
    fun `TC-FILE-DETAIL-DOMAIN-002 공백만 있는 제목과 파일 이름은 없는 것으로 보고 안내한다`() {
        assertInvalid(change = { composeRule.titleInput().performTextReplacement("   ") }, message = TITLE_BLANK, focused = { composeRule.titleInput() })
        assertInvalid(change = { composeRule.nameInput().performTextReplacement("   ") }, message = NAME_BLANK, focused = { composeRule.nameInput() })
    }

    @Test
    fun `TC-FILE-DETAIL-DOMAIN-004 입력한 값을 앞뒤 공백을 포함해 그대로 보내고 설명은 비워 둘 수 있다`() {
        val screen = FileDetailTestScreen()

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(" 보고서 ")
        composeRule.nameInput().performTextReplacement(" report.pdf")
        composeRule.descriptionInput().performTextClearance()
        composeRule.clickUpdate()

        screen.mocks.updateList.map { parameter -> parameter.detail } shouldBe listOf(DiaryFileDetail(name = " report.pdf", title = " 보고서 ", description = ""))
        screen.mocks.updateList
            .single()
            .id shouldBe screen.mocks.file.id
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-008 수정에 성공하면 성공을 알리고 화면 제목을 바꾼다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()

        composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertExists()
        composeRule.textCount(newTitle) shouldBe 2
        composeRule.titleInput().assert(hasText(newTitle))
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-009 수정하는 동안 진행 중임을 표시하고 다시 실행해도 한 번만 보낸다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()
        screen.mocks.updateAnswer = { awaitCancellation() }

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()
        composeRule.clickUpdate()

        composeRule.progressIn(UPDATE_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { screen.mocks.updateUseCase(parameter = any()) }
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-010 수정하는 동안 더 고친 입력은 성공 뒤에도 남아 수정 동작이 다시 보인다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val moreDescription = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()
        val response = CompletableDeferred<Unit>()
        val updateAnswer = screen.mocks.updateAnswer
        screen.mocks.updateAnswer = { parameter ->
            response.await()
            updateAnswer(parameter)
        }

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()
        composeRule.descriptionInput().performTextReplacement(moreDescription)
        composeRule.runOnIdle { response.complete(Unit) }
        composeRule.waitForIdle()

        composeRule.descriptionInput().assert(hasText(moreDescription))
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-011 수정에 실패하면 실패를 알리고 입력을 그대로 둔다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()
        screen.mocks.updateAnswer = { Result.failure(IllegalStateException(FAILURE_MESSAGE)) }

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()

        composeRule.onNodeWithText(UPDATE_FAILED).assertExists()
        composeRule.titleInput().assert(hasText(newTitle))
        composeRule.textCount(screen.mocks.file.title) shouldBe 1
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-012 다운로드는 입력 중인 이름이 아니라 저장된 파일 이름으로 내려받는다`() {
        val file = fileDetailFixtureMonkey.nonBlankDiaryFile().copy(name = "report.pdf")
        val screen = FileDetailTestScreen(mocks = FileDetailMocks(file = file))

        setScreen(screen = screen)
        composeRule.nameInput().performTextReplacement("memo.txt")
        composeRule.clickDownload()

        screen.mocks.downloadList.map { target -> target.name } shouldBe listOf("report.pdf")
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-013 내려받는 동안 진행 중임을 표시하고 다시 실행해도 한 번만 내려받는다`() {
        val screen = FileDetailTestScreen()
        screen.mocks.downloadAnswer = { awaitCancellation() }

        setScreen(screen = screen)
        composeRule.clickDownload()
        composeRule.clickDownload()

        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertExists()
        screen.mocks.downloadList.size shouldBe 1
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-014 다운로드 결과를 알린다`() {
        listOf<Pair<Result<FileDownloadResult>, String>>(
            Result.success<FileDownloadResult>(FileDownloadResult.Saved(name = "report (1).pdf")) to "파일을 내려받았습니다: report (1).pdf",
            Result.failure<FileDownloadResult>(FileDownloadDirectoryUnavailableException()) to DOWNLOAD_DIRECTORY_UNAVAILABLE,
            Result.failure<FileDownloadResult>(IllegalStateException(FAILURE_MESSAGE)) to DOWNLOAD_FAILED,
        ).forEach { (result, message) ->
            val screen = FileDetailTestScreen()
            screen.mocks.downloadAnswer = { result }

            setScreen(screen = screen)
            composeRule.clickDownload()

            // 저장한 이름을 넣은 안내는 문구를 읽어 들인 뒤에 표시되므로 나타날 때까지 기다린다.
            composeRule.waitUntil(timeoutMillis = MESSAGE_TIMEOUT_MILLIS) {
                composeRule.onAllNodesWithText(message, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-015 브라우저에 파일을 넘겨 내려받기에 성공하면 진행 중 표시만 사라지고 안내는 없다`() {
        val screen = FileDetailTestScreen()
        val response = CompletableDeferred<Unit>()
        screen.mocks.downloadAnswer = {
            response.await()
            Result.success(FileDownloadResult.HandedToBrowser)
        }

        setScreen(screen = screen)
        composeRule.clickDownload()
        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertExists()
        composeRule.runOnIdle { response.complete(Unit) }
        composeRule.waitForIdle()

        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertDoesNotExist()
        listOf(DOWNLOAD_FAILED, DOWNLOAD_DIRECTORY_UNAVAILABLE).forEach { message -> composeRule.textCount(message) shouldBe 0 }
        composeRule.onAllNodes(hasText(DOWNLOAD_SUCCEEDED_PREFIX, substring = true), useUnmergedTree = true).fetchSemanticsNodes().size shouldBe 0
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-016 수정과 다운로드는 서로 막지 않는다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()
        screen.mocks.updateAnswer = { awaitCancellation() }
        screen.mocks.downloadAnswer = { awaitCancellation() }

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.clickUpdate()
        composeRule.clickDownload()

        composeRule.progressIn(UPDATE_DESCRIPTION).assertExists()
        composeRule.progressIn(DOWNLOAD_DESCRIPTION).assertExists()
        screen.mocks.updateList.size shouldBe 1
        screen.mocks.downloadList.size shouldBe 1
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-017 계정이 게스트나 다른 사용자로 바뀌면 화면을 닫는다`() {
        listOf<Account>(Account.Guest, fileDetailFixtureMonkey.giveMeOne<Account.User>()).forEach { changed ->
            val screen = FileDetailTestScreen()

            setScreen(screen = screen)
            composeRule.runOnIdle { screen.mocks.accountFlow.value = Result.success(changed) }
            composeRule.waitForIdle()

            screen.navigateUpCount shouldBe 1
        }
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-018 계정을 확정하지 않은 동안에는 화면을 그대로 두고 수정과 다운로드를 실패로 알린다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.runOnIdle { screen.mocks.accountFlow.value = Result.failure(IllegalStateException("account")) }
        composeRule.waitForIdle()
        composeRule.clickUpdate()

        composeRule.onNodeWithText(UPDATE_FAILED).assertExists()
        composeRule.titleInput().assert(hasText(newTitle))

        composeRule.clickDownload()

        composeRule.onNodeWithText(DOWNLOAD_FAILED).assertExists()
        screen.navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-FILE-DETAIL-FEATURE-019 뒤로가면 FileHome으로 돌아간다`() {
        val newTitle = fileDetailFixtureMonkey.nonBlankString()
        val moreDescription = fileDetailFixtureMonkey.nonBlankString()
        val screen = FileDetailTestScreen()

        setScreen(screen = screen)
        composeRule.titleInput().performTextReplacement(newTitle)
        composeRule.onNodeWithContentDescription(NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        screen.navigateUpCount shouldBe 1
    }

    // 테스트 데이터를 순회할 때는 한 번 정한 내용에서 화면만 바꿔 끼워, 새 화면처럼 처음부터 시작하게 한다.
    private fun setScreen(screen: FileDetailTestScreen = FileDetailTestScreen()) {
        if (isContentSet) {
            composeRule.runOnIdle { currentScreen = screen }
        } else {
            isContentSet = true
            currentScreen = screen
            composeRule.setContent { currentScreen?.let { value -> key(value) { value.Content() } } }
        }
        composeRule.waitForIdle()
    }

    private fun assertInvalid(
        change: () -> Unit,
        message: String,
        focused: () -> SemanticsNodeInteraction,
        updateAnswer: (suspend (UpdateFileUseCase.Parameter) -> Result<DiaryFile>)? = null,
    ) {
        val screen = FileDetailTestScreen()
        if (updateAnswer != null) screen.mocks.updateAnswer = updateAnswer

        setScreen(screen = screen)
        change()
        composeRule.waitForIdle()
        val inputBefore = listOf(composeRule.titleInput(), composeRule.descriptionInput(), composeRule.nameInput()).map { node -> node.fetchText() }
        composeRule.clickUpdate()

        composeRule.onNodeWithText(message).assertExists()
        focused().assertIsFocused()
        listOf(composeRule.titleInput(), composeRule.descriptionInput(), composeRule.nameInput()).map { node -> node.fetchText() } shouldBe inputBefore
    }
}

private fun SemanticsNodeInteraction.fetchText(): String =
    fetchSemanticsNode()
        .config
        .getOrElseNullable(SemanticsProperties.EditableText) { null }
        ?.text
        .orEmpty()
