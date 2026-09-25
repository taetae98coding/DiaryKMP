package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.feature.file.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setFileHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setFileHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-005 게스트 상태에서는 로그인 안내만 표시하고 파일 추가를 두지 않는다`() {
        setFileHomeScaffold(uiState = FileHomeUiState.Guest)

        composeRule.onNodeWithText(KOREAN_GUEST_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_GUEST_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-006 계정 상태를 확정하지 않은 동안에는 제목과 뒤로가기만 표시한다`() {
        setFileHomeScaffold(uiState = FileHomeUiState.Loading, pagingData = PagingData.from(listOf(fixtureMonkey.diaryFile())))

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_GUEST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-007 목록의 각 파일에 이름, 크기, 올린 날짜와 시각을 표시한다`() {
        val fileList =
            listOf(
                fixtureMonkey.diaryFile(name = "memo.txt", size = 512),
                fixtureMonkey.diaryFile(name = "photo.jpg", size = 1_024),
                fixtureMonkey.diaryFile(name = "video.mp4", size = 24_536_679),
            )

        setFileHomeScaffold(uiState = FileHomeUiState.User, pagingData = PagingData.from(fileList))

        mapOf("memo.txt" to "512 B", "photo.jpg" to "1.0 KB", "video.mp4" to "23.4 MB").forEach { (name, sizeText) ->
            composeRule.onNodeWithText(name).assertExists()
            composeRule.onNodeWithText("$sizeText · ", substring = true).assertExists()
        }
        composeRule.onAllNodes(hasTextMatching(Regex("""\d{4}\. \d{1,2}\. \d{1,2}\. (오전|오후) \d{1,2}:\d{2}"""))).fetchSemanticsNodes().size shouldBe fileList.size
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-008 목록을 처음 불러오는 동안 불러오는 중임을 표시한다`() {
        setFileHomeScaffold(
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(emptyList(), sourceLoadStates = loadStates(refresh = LoadState.Loading)),
        )

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_LOAD_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-009 처음 불러오기에 실패하면 실패 안내와 다시 시도를 표시한다`() {
        setFileHomeScaffold(
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(emptyList(), sourceLoadStates = loadStates(refresh = LoadState.Error(IllegalStateException("load")))),
        )

        composeRule.onNodeWithText(KOREAN_LOAD_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_RETRY).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-011 올린 파일이 하나도 없으면 빈 상태 안내를 표시하고 파일 추가를 그대로 둔다`() {
        setFileHomeScaffold(
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(emptyList(), sourceLoadStates = loadStates(refresh = LoadState.NotLoading(endOfPaginationReached = true))),
        )

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-FILE-HOME-FEATURE-013 이어서 불러오기에 실패하면 목록 끝에 실패 안내와 다시 시도를 표시하고 파일은 그대로 남는다`() {
        val fileList = List(PAGE_SIZE) { index -> fixtureMonkey.diaryFile(name = "file-$index.txt") }

        setFileHomeScaffold(
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(fileList, sourceLoadStates = loadStates(append = LoadState.Error(IllegalStateException("load")))),
        )
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).performScrollToIndex(PAGE_SIZE)

        composeRule.onNodeWithText(KOREAN_LOAD_MORE_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_RETRY).assertExists()
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).performScrollToIndex(0)
        composeRule.onNodeWithText("file-0.txt").assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-020 이어서 불러오는 동안 목록 끝에 불러오는 중임을 표시한다`() {
        val fileList = List(PAGE_SIZE) { index -> fixtureMonkey.diaryFile(name = "file-$index.txt") }

        setFileHomeScaffold(
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(fileList, sourceLoadStates = loadStates(append = LoadState.Loading)),
        )
        composeRule.onNodeWithTag(FILE_HOME_LIST_TEST_TAG).performScrollToIndex(PAGE_SIZE)

        composeRule.onNodeWithTag(FILE_HOME_APPEND_LOADING_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-FILE-HOME-FEATURE-017 올리는 동안 파일 추가 자리에 진행 중 표시를 둔다`() {
        setFileHomeScaffold(uiState = FileHomeUiState.User, uploadUiState = FileHomeUploadUiState(isUploading = true))

        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION))),
                useUnmergedTree = true,
            ).assertExists()
    }

    @Test
    fun `파일 추가와 다시 시도와 뒤로가기를 누르면 각 이벤트를 한 번씩 내보낸다`() {
        val eventList = mutableListOf<FileHomeScaffoldEvent>()

        setFileHomeScaffold(
            onEvent = { event -> eventList += event },
            uiState = FileHomeUiState.User,
            pagingData = PagingData.from(emptyList(), sourceLoadStates = loadStates(refresh = LoadState.Error(IllegalStateException("load")))),
        )
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RETRY).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(FileHomeScaffoldEvent.ClickAdd, FileHomeScaffoldEvent.ClickRetry, FileHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기와 파일 추가 접근성 이름을 제공한다`() {
        setFileHomeScaffold(uiState = FileHomeUiState.User)

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assertExists()
    }

    private fun setFileHomeScaffold(
        onEvent: (FileHomeScaffoldEvent) -> Unit = {},
        uiState: FileHomeUiState = FileHomeUiState.Loading,
        uploadUiState: FileHomeUploadUiState = FileHomeUploadUiState(),
        pagingData: PagingData<DiaryFile> = PagingData.empty(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                FileHomeScaffold(
                    onEvent = onEvent,
                    filePagingItems = flowOf(pagingData).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                    uploadUiStateProvider = { uploadUiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun loadStates(
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    ): LoadStates =
        LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = append,
        )

    public companion object {
        private const val PAGE_SIZE = 20
        private const val KOREAN_TITLE = "파일"
        private const val DEFAULT_TITLE = "Files"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "파일 추가"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add file"
        private const val KOREAN_GUEST_TITLE = "로그인이 필요합니다"
        private const val KOREAN_GUEST_DESCRIPTION = "로그인하면 파일을 올리고 확인할 수 있습니다"
        private const val KOREAN_EMPTY_TITLE = "아직 올린 파일이 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 파일을 올릴 수 있습니다"
        private const val KOREAN_LOAD_FAILED_MESSAGE = "파일을 불러오지 못했습니다"
        private const val KOREAN_LOAD_MORE_FAILED_MESSAGE = "더 불러오지 못했습니다"
        private const val KOREAN_RETRY = "다시 시도"
        private const val DEFAULT_RETRY = "Retry"
    }
}

private fun hasTextMatching(regex: Regex): SemanticsMatcher =
    SemanticsMatcher("text matches $regex") { node ->
        node.config
            .getOrElseNullable(SemanticsProperties.Text) { null }
            .orEmpty()
            .any { text -> regex.containsMatchIn(text.text) }
    }
