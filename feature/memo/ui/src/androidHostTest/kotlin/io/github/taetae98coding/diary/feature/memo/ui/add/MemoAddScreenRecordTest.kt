package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiStep
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.awaitTagPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenRecordTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-ADD-FEATURE-069 작성 도우미로 반영한 내용으로 메모 추가를 실행한다`() {
        val start = LocalDate.fromEpochDays(fixtureMonkey.giveMeOne<Int>() and DAY_RANGE_MASK)
        val draft =
            MemoDraft(
                title = randomText(prefix = TITLE_PREFIX),
                description = randomText(prefix = DESCRIPTION_PREFIX),
                dateTime = MemoDateTime.AllDay(dateRange = start..start.plus(1, DateTimeUnit.DAY)),
            )
        val detailSlot = slot<MemoDetail>()
        val viewModels = screenTestViewModel()
        every { viewModels.viewModel.add(detail = capture(detailSlot), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } returns Unit
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        setMemoAddScreen(viewModels = viewModels, geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState))
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = draft)
        composeRule.waitForIdle()

        listOf(TITLE_LABEL, DESCRIPTION_LABEL, DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription("Apply $label").performClick()
            composeRule.waitForIdle()
        }
        geminiUiState.value = MemoGeminiUiState()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        detailSlot.captured.title shouldBe draft.title
        detailSlot.captured.description shouldBe draft.description
        detailSlot.captured.dateTime shouldBe draft.dateTime
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-070 작성 도우미의 결과는 반영하기 전에는 메모 추가 성립 조건에 쓰이지 않는다`() {
        val draft =
            MemoDraft(
                title = randomText(prefix = TITLE_PREFIX),
                description = "",
                dateTime = null,
            )
        val effect = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val detailList = mutableListOf<MemoDetail>()
        val viewModels = screenTestViewModel(effect = effect.receiveAsFlow())
        // 추가 판정은 제목 입력 칸의 내용으로만 이루어지므로, 전달된 제목이 비어 있으면 제목 미입력으로 응답한다.
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers {
            val detail = firstArg<MemoDetail>()
            detailList.add(detail)
            if (detail.title.isBlank()) effect.trySend(MemoAddEffect.TitleBlank).getOrThrow()
        }
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        setMemoAddScreen(viewModels = viewModels, geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState))
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = draft)
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(draft = draft)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        detailList.map { detail -> detail.title } shouldBe listOf("")
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertExists()
    }

    @Test
    fun `TC-MEMO-ADD-DATA-026 태그를 선택하는 것만으로는 저장된 메모가 바뀌지 않는다`() {
        val tag = testTag(title = randomText(prefix = TAG_PREFIX))
        val addMemoUseCase = mockk<AddMemoUseCase>()
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(tag), addMemoUseCase = addMemoUseCase))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(tag.detail.title).performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.closeDialogByBack()
        composeRule.waitForIdle()

        coVerify(exactly = 0) { addMemoUseCase(any<AddMemoUseCase.Parameter>()) }
    }

    @Test
    fun `TC-MEMO-ADD-DATA-027 설명을 입력하지 않고 컬러를 바꾸지 않으면 빈 설명과 처음 제시한 컬러가 기록된다`() {
        val title = randomText(prefix = TITLE_PREFIX)
        val detailSlot = slot<MemoDetail>()
        val viewModels = screenTestViewModel()
        every { viewModels.viewModel.add(detail = capture(detailSlot), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } returns Unit
        setMemoAddScreen(viewModels = viewModels)
        composeRule.waitForIdle()
        val initialColor = composeRule.colorHexText()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(title)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        detailSlot.captured.title shouldBe title
        detailSlot.captured.description shouldBe ""
        detailSlot.captured.color
            .toColor()
            .toHexString() shouldBe initialColor
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        geminiViewModel: MemoGeminiViewModel = screenTestGeminiViewModel(),
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = geminiViewModel,
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        // 1970년부터 약 90년 안의 날짜로 한정해 날짜 입력이 표현할 수 있는 범위를 벗어나지 않게 한다.
        private const val DAY_RANGE_MASK = 0x7FFF
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private const val DEFAULT_PRIMARY_SET_DESCRIPTION = "Set as primary tag"
        private const val TITLE_LABEL = "Title"
        private const val DESCRIPTION_LABEL = "Description"
        private const val DATE_TIME_LABEL = "Date & time"
        private const val TITLE_PREFIX = "title-"
        private const val DESCRIPTION_PREFIX = "description-"
        private const val TAG_PREFIX = "tag-"

        // 생성한 문자열은 비어 있을 수 있으므로 앞에 고정 문자열을 붙여 빈 값이 되지 않게 한다.
        private fun randomText(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>()
    }
}
