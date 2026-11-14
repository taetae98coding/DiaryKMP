package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-85f0763d-7a1d-4056-a218-9b036ecc4aaa 조회 중에는 공유 버튼이 없다`() {
        val uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
        setMemoDetailScreen(viewModel = screenTestViewModel(uiState = uiState), sharer = screenTestContentSharer(isTextSupported = true))

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).assertDoesNotExist()

        uiState.value = memoDetailUiState(detail = memoDetail(MEMO_TITLE))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).assertIsEnabled()
    }

    @Test
    fun `TC-b1fda7b7-a97f-4d91-86fc-e7d9d6681738 공유를 제공하지 않으면 공유 버튼이 없다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = false))

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-2ed845cb-b896-4cd4-a347-89cbed7f93f4 기간이 없으면 누르는 즉시 글로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-a677b5b4-751c-451e-a689-6c9b52a36d46 제목 입력이 비면 저장된 제목으로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = sharer)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextClearance()
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-a677b5b4-751c-451e-a689-6c9b52a36d46 제목 입력이 공백뿐이면 저장된 제목으로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = sharer)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextReplacement("   ")
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-d1acc93b-c1b3-4530-85f2-d594d841866b 반영하지 않은 입력으로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        val viewModel = loadedViewModel(detail = memoDetail(MEMO_TITLE))
        setMemoDetailScreen(viewModel = viewModel, sharer = sharer)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(EDITED_DESCRIPTION)
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        sharer.sharedContent() shouldBe ShareContent.Text(text = "$MEMO_TITLE$EDIT_SUFFIX\n$EDITED_DESCRIPTION", title = "$MEMO_TITLE$EDIT_SUFFIX")
        verify(exactly = 0) { viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-b507df2c-69c6-40e3-9822-3c1e0fe528f4 미리보기 중에도 설명 원문을 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(detail = memoDetail(MEMO_TITLE).copy(description = MARKDOWN_DESCRIPTION)), sharer = sharer)
        composeRule.waitUntil(timeoutMillis = MARKDOWN_TIMEOUT_MILLIS) { composeRule.onAllNodesWithText(MARKDOWN_RENDERED_TEXT, substring = true).fetchSemanticsNodes().isNotEmpty() }

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        sharer.sharedContent() shouldBe ShareContent.Text(text = "$MEMO_TITLE\n$MARKDOWN_DESCRIPTION", title = MEMO_TITLE)
    }

    @Test
    fun `TC-a57cd1b8-7861-406a-9f85-d478a5fb769c 기간이 있으면 공유 형식을 고르게 한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertExists()
        coVerify(exactly = 0) { sharer.share(content = any()) }
    }

    @Test
    fun `TC-e987373a-c976-4ac1-83ca-a42f8215931e 글을 고르면 제목, 기간, 설명을 이어 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SHARE_TEXT_ROW).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_PERIOD_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-d71901e5-576b-4e64-8818-ad23e10fb118 기간 줄은 메모 카드의 기간 표기를 따른다`() {
        val caseList =
            listOf(
                MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 20)..LocalDate(2026, 11, 20)) to "Nov 20, 2026",
                MemoDateTime.AllDay(dateRange = LocalDate(2026, 11, 20)..LocalDate(2026, 11, 22)) to "Nov 20, 2026 ~ Nov 22, 2026",
                SAME_DAY_DATE_TIME to SAME_DAY_PERIOD_TEXT,
                MemoDateTime.DateTime(start = LocalDateTime(2026, 11, 20, 23, 0), endInclusive = LocalDateTime(2026, 11, 21, 1, 0)) to "Nov 20, 2026 11:00 PM ~ Nov 21, 2026 1:00 AM",
            )
        val uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
        val sharer = screenTestContentSharer(isTextSupported = true, isCalendarFileSupported = false)
        setMemoDetailScreen(viewModel = screenTestViewModel(uiState = uiState), sharer = sharer)

        caseList.forEach { (dateTime, periodText) ->
            uiState.value = memoDetailUiState(id = Arb.kotlinUuid().next(), detail = memoDetail(MEMO_TITLE).copy(dateTime = dateTime))
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            (sharer.sharedContentList().last() as ShareContent.Text).text.lines()[1] shouldBe periodText
        }
    }

    @Test
    fun `TC-121f4d10-5ab2-4f66-86fc-f06e1386b158 일정 파일을 고르면 메모 제목 이름의 일정 파일을 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertDoesNotExist()
        val content = sharer.sharedContent().shouldBeInstanceOf<ShareContent.File>()
        content.type shouldBe ShareFileType.CALENDAR
        content.name shouldBe MEMO_TITLE
        content.title shouldBe MEMO_TITLE
        content.bytes.decodeToString() shouldContain "\r\nSUMMARY:$MEMO_TITLE\r\n"
    }

    @Test
    fun `TC-d367cad0-1f77-4063-8cb1-0b802cc61a8e 형식을 고르지 않고 닫으면 공유하지 않는다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).performSemanticsAction(SemanticsActions.Dismiss)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        coVerify(exactly = 0) { sharer.share(content = any()) }
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-8b6589d3-86c3-427b-adad-213deead39c0 일정 파일을 공유할 수 없으면 바로 글로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true, isCalendarFileSupported = false)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_PERIOD_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-a9faa51e-0106-4bed-afb8-1808b142d2a7 공유 시트를 열지 못하면 안내를 표시한다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = true, result = ShareResult.FAILED))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_FAILED_MESSAGE).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-cfc70a80-a936-4db1-bb5c-c5e9ca812da6 복사로 대신하면 복사 안내를 표시한다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = true, result = ShareResult.COPIED))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_COPIED_MESSAGE).assertExists()
        composeRule.onNodeWithText(SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-f0c699bc-44ba-4f66-8cf1-23155d89ed03 공유를 마치면 안내를 표시하지 않는다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = true, result = ShareResult.COMPLETED))

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(SHARE_COPIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-f0c699bc-44ba-4f66-8cf1-23155d89ed03 공유 시트를 닫으면 안내를 표시하지 않는다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = true, result = ShareResult.DISMISSED))

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(SHARE_COPIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-d366f5f4-013b-4635-ad4d-621d8aa15908 공유해도 입력과 수정 동작은 그대로다`() {
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = screenTestContentSharer(isTextSupported = true))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Test
    fun `TC-9882761c-b242-4bc8-bf0f-50a28eb70bcd 일정 파일 없이 복사로 대신하면 바로 복사한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true, isCalendarFileSupported = false, result = ShareResult.COPIED)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_PERIOD_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
        composeRule.onNodeWithText(SHARE_COPIED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-3bbe2855-7f42-4640-8082-48bc9263f4c6 기간을 지우고 공유하면 글로 공유한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME), sharer = sharer)
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        sharer.sharedContent() shouldBe ShareContent.Text(text = TITLE_AND_DESCRIPTION_TEXT, title = MEMO_TITLE)
    }

    @Test
    fun `TC-53bc4247-3c13-4f15-b55c-393470b3c3a9 기간을 넣고 공유하면 형식을 고르게 한다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = loadedViewModel(), sharer = sharer)
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertExists()
        coVerify(exactly = 0) { sharer.share(content = any()) }
    }

    @Test
    fun `TC-dfa70b06-85e6-4c0b-95a2-236520de3f48 수정 중에도 공유한다`() {
        assertShareDuringProgress(uiState = memoDetailUiState(detail = memoDetail(MEMO_TITLE), isInProgress = true))
    }

    @Test
    fun `TC-dfa70b06-85e6-4c0b-95a2-236520de3f48 완료 중에도 공유한다`() {
        assertShareDuringProgress(uiState = memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinishInProgress = true))
    }

    @Test
    fun `TC-dfa70b06-85e6-4c0b-95a2-236520de3f48 다시 시작 중에도 공유한다`() {
        assertShareDuringProgress(uiState = memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = true, isFinishInProgress = true))
    }

    @Test
    fun `TC-dfa70b06-85e6-4c0b-95a2-236520de3f48 복사 중에도 공유한다`() {
        assertShareDuringProgress(uiState = memoDetailUiState(detail = memoDetail(MEMO_TITLE), isCopyInProgress = true))
    }

    @Test
    fun `TC-dfa70b06-85e6-4c0b-95a2-236520de3f48 삭제 중에도 공유한다`() {
        assertShareDuringProgress(uiState = memoDetailUiState(detail = memoDetail(MEMO_TITLE), isDeleteInProgress = true))
    }

    @Test
    fun `TC-1c25880b-c06c-4d62-a498-666875f1b58a 재생성되어도 공유를 다시 시작하지 않는다`() {
        val sharer = screenTestContentSharer(isTextSupported = true)
        coEvery { sharer.share(content = any()) } coAnswers { awaitCancellation() }
        val viewModel = loadedViewModel(detail = Arb.memoDetail().next())
        val factory = screenTestContentSharerFactory(sharer = sharer)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ShareScreen(viewModel = viewModel, contentSharerFactory = factory) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        coVerify(exactly = 1) { sharer.share(content = any()) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).assertIsEnabled()
    }

    @Test
    fun `TC-a9bbd712-38cb-44fc-bb45-9c2f14a4ec05 형식을 고르는 동안 재생성되어도 선택지가 남는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = loadedViewModel(dateTime = SAME_DAY_DATE_TIME)
        val factory = screenTestContentSharerFactory(sharer = screenTestContentSharer(isTextSupported = true))
        restorationTester.setContent { ShareScreen(viewModel = viewModel, contentSharerFactory = factory) }
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertExists()
    }

    @Test
    fun `TC-9a9e1b4e-a5ac-4371-8b3e-89916f46613b 대상이 바뀌면 선택지가 닫힌다`() {
        val uiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(detail = memoDetail(MEMO_TITLE).copy(dateTime = SAME_DAY_DATE_TIME)))
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = screenTestViewModel(uiState = uiState), sharer = sharer)
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()

        uiState.value = memoDetailUiState(id = Arb.kotlinUuid().next(), detail = memoDetail(OTHER_MEMO_TITLE).copy(dateTime = SAME_DAY_DATE_TIME))
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(OTHER_MEMO_TITLE))
        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertDoesNotExist()
        coVerify(exactly = 0) { sharer.share(content = any()) }
    }

    @Test
    fun `TC-4f682245-e58f-4b9b-abd1-f0c9eb108b3f 새로 진입하면 선택지가 닫혀 있다`() {
        var launchCount by mutableIntStateOf(0)
        val savedDetail = memoDetail(MEMO_TITLE).copy(dateTime = SAME_DAY_DATE_TIME)
        val factory = screenTestContentSharerFactory(sharer = screenTestContentSharer(isTextSupported = true))
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 메모를 다시 조회하는 새 인스턴스가 만들어지므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                val viewModel = remember { screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = savedDetail))) }

                ShareScreen(viewModel = viewModel, contentSharerFactory = factory)
            }
        }
        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertExists()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE))
        composeRule.onNodeWithText(SHARE_TEXT_ROW).assertDoesNotExist()
        composeRule.onNodeWithText(SHARE_CALENDAR_FILE_ROW).assertDoesNotExist()
    }

    private fun setMemoDetailScreen(
        viewModel: MemoDetailViewModel,
        sharer: ContentSharer,
    ) {
        composeRule.setContent { ShareScreen(viewModel = viewModel, contentSharerFactory = screenTestContentSharerFactory(sharer = sharer)) }
        composeRule.waitForIdle()
    }

    private fun assertShareDuringProgress(uiState: MemoDetailUiState.Content) {
        val sharer = screenTestContentSharer(isTextSupported = true)
        setMemoDetailScreen(viewModel = screenTestViewModel(uiState = MutableStateFlow(uiState)), sharer = sharer)

        composeRule.onNodeWithContentDescription(SHARE_BUTTON_DESCRIPTION).assertIsEnabled().performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { sharer.share(content = any()) }
    }

    @Composable
    private fun ShareScreen(
        viewModel: MemoDetailViewModel,
        contentSharerFactory: ContentSharerFactory,
    ) {
        MemoDetailScreenTestTheme {
            MemoDetailScreen(
                addRequestKey = TEST_ADD_REQUEST_KEY,
                detailViewModel = viewModel,
                tagViewModel = screenTestTagViewModel(),
                webViewModel = screenTestWebViewModel(),
                contactViewModel = screenTestContactViewModel(),
                placeViewModel = screenTestPlaceViewModel(),
                placeMapViewModel = screenTestPlaceMapViewModel(),
                placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
                contentSharerFactory = contentSharerFactory,
                navigateUp = {},
                navigateToCopiedMemo = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToWebAdd = {},
                navigateToWebDetail = {},
                navigateToContactAdd = {},
                navigateToContactDetail = {},
                navigateToPlaceAdd = {},
                navigateToPlaceDetail = {},
                navigateToGeminiSetting = {},
                componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }

    private companion object {
        const val SHARE_BUTTON_DESCRIPTION = "Share"
        const val SHARE_TEXT_ROW = "Share as text"
        const val SHARE_CALENDAR_FILE_ROW = "Share as calendar file"
        const val SHARE_FAILED_MESSAGE = "Couldn't share."
        const val SHARE_COPIED_MESSAGE = "Copied to clipboard."
        const val MEMO_DESCRIPTION = "MemoShareDescription"
        const val OTHER_MEMO_TITLE = "OtherShareMemoTitle"
        const val EDITED_DESCRIPTION = "EditedShareDescription"
        const val MARKDOWN_DESCRIPTION = "**Bold** shared"
        const val MARKDOWN_RENDERED_TEXT = "Bold shared"
        const val MARKDOWN_TIMEOUT_MILLIS = 5_000L
        const val SAME_DAY_PERIOD_TEXT = "Nov 20, 2026 7:00 PM ~ 9:00 PM"
        const val TITLE_AND_DESCRIPTION_TEXT = "$MEMO_TITLE\n$MEMO_DESCRIPTION"
        const val TITLE_PERIOD_AND_DESCRIPTION_TEXT = "$MEMO_TITLE\n$SAME_DAY_PERIOD_TEXT\n$MEMO_DESCRIPTION"
        val SAME_DAY_DATE_TIME: MemoDateTime = MemoDateTime.DateTime(start = LocalDateTime(2026, 11, 20, 19, 0), endInclusive = LocalDateTime(2026, 11, 20, 21, 0))

        fun loadedViewModel(
            detail: MemoDetail = memoDetail(MEMO_TITLE).copy(description = MEMO_DESCRIPTION),
            dateTime: MemoDateTime? = null,
        ): MemoDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = detail.copy(dateTime = dateTime))))

        fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

        fun ContentSharer.sharedContentList(): List<ShareContent> {
            val captured = mutableListOf<ShareContent>()
            coVerify { share(content = capture(captured)) }
            return captured
        }

        fun ContentSharer.sharedContent(): ShareContent {
            val captured = sharedContentList()
            captured.size shouldBe 1
            return captured.single()
        }
    }
}
