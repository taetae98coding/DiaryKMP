package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreen
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreenTestTheme
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddScreenViewModels
import io.github.taetae98coding.diary.feature.memo.ui.add.screenTestRealViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-DOMAIN-004 반영은 태그, 대표 태그, 웹, 장소, 연락처 선택을 바꾸지 않는다`() {
        val tag = testTag(title = "태그 ${fixtureMonkey.giveMeOne<String>()}")
        val web = testWeb(title = "웹 ${fixtureMonkey.giveMeOne<String>()}")
        val contact = testContact(name = "연락처 ${fixtureMonkey.giveMeOne<String>()}")
        val place = testPlace(title = "장소 ${fixtureMonkey.giveMeOne<String>()}")
        val viewModels =
            screenTestRealViewModel(
                initialPrimaryTagId = tag.id,
                initialWebId = web.id,
                initialContactId = contact.id,
                initialPlaceId = place.id,
                tagList = listOf(tag),
                webList = listOf(web),
                contactList = listOf(contact),
                placeList = listOf(place),
            )
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        val geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState)
        setMemoAddScreen(viewModels = viewModels, geminiViewModel = geminiViewModel)
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = createDraft())
        composeRule.waitForIdle()
        val tagSelection = composeRule.runOnIdle { viewModels.tagViewModel.selection.value }

        listOf(TITLE_LABEL, DESCRIPTION_LABEL, DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription("Apply $label").performClick()
            composeRule.waitForIdle()
        }

        composeRule.runOnIdle {
            viewModels.tagViewModel.selection.value shouldBe tagSelection
            viewModels.tagViewModel.selection.value.primaryTagId shouldBe tag.id
            viewModels.webViewModel.webIdSet.value shouldBe setOf(web.id)
            viewModels.contactViewModel.contactIdSet.value shouldBe setOf(contact.id)
            viewModels.placeViewModel.placeIdSet.value shouldBe setOf(place.id)
        }
        verify(exactly = 3) { geminiViewModel.markApplied(any()) }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-029 도우미가 열려 있는 동안 뒤로가기를 하면 화면을 떠나지 않고 도우미만 닫힌다`() {
        var navigateUpCount = 0
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        val geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState)
        setMemoAddScreen(
            viewModels = screenTestViewModel(),
            geminiViewModel = geminiViewModel,
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT)
        composeRule.waitForIdle()

        composeRule.onNode(hasText(PROMPT_LABEL) and hasAnyAncestor(isDialog())).assertExists()

        composeRule.closeDialogByBack()

        verify(exactly = 1) { geminiViewModel.close() }
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-008 취소하면 적었던 프롬프트가 남고 결과와 메모 입력은 바뀌지 않는다`() {
        val memoText = memoText()
        val prompt = promptText()
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        setMemoAddScreen(viewModels = screenTestViewModel(), geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState))
        writePrompt(geminiUiState = geminiUiState, memoText = memoText, prompt = prompt)

        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.GENERATING)
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT)
        composeRule.waitForIdle()

        composeRule.onNode(promptInput).assert(hasText(prompt))
        composeRule.onNodeWithContentDescription("Apply $TITLE_LABEL").assertDoesNotExist()
        composeRule.onAllNodes(memoInput)[0].assert(hasText(memoText))
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-010 생성에 실패하면 원인을 알리고 적었던 프롬프트와 메모 입력을 유지한다`() {
        val memoText = memoText()
        val prompt = promptText()
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        setMemoAddScreen(viewModels = screenTestViewModel(), geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState))
        writePrompt(geminiUiState = geminiUiState, memoText = memoText, prompt = prompt)

        mapOf(
            MemoGeminiFailure.INVALID_API_KEY to INVALID_API_KEY_MESSAGE,
            MemoGeminiFailure.UNKNOWN to FAILED_MESSAGE,
        ).forEach { (failure, message) ->
            geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.GENERATING)
            composeRule.waitForIdle()
            geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = failure)
            composeRule.waitForIdle()

            composeRule.onNode(hasText(message) and hasAnyAncestor(isDialog())).assertExists()
            composeRule.onNode(promptInput).assert(hasText(prompt))
            composeRule.onAllNodes(memoInput)[0].assert(hasText(memoText))
        }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-019 닫았다가 다시 열면 프롬프트가 비어 있고 결과가 표시되지 않는다`() {
        val prompt = promptText()
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        setMemoAddScreen(viewModels = screenTestViewModel(), geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState))
        writePrompt(geminiUiState = geminiUiState, memoText = memoText(), prompt = prompt)
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = createDraft())
        composeRule.waitForIdle()

        geminiUiState.value = MemoGeminiUiState()
        composeRule.waitForIdle()
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT)
        composeRule.waitForIdle()

        composeRule.onNode(promptInput).assert(hasText(prompt).not())
        composeRule.onNodeWithText(prompt).assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Apply $TITLE_LABEL").assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-020 생성 중에 닫으면 도우미가 닫히고 메모 입력은 바뀌지 않는다`() {
        val memoText = memoText()
        val geminiUiState = MutableStateFlow(MemoGeminiUiState())
        val geminiViewModel = screenTestGeminiViewModel(uiState = geminiUiState)
        setMemoAddScreen(viewModels = screenTestViewModel(), geminiViewModel = geminiViewModel)
        writePrompt(geminiUiState = geminiUiState, memoText = memoText, prompt = promptText())
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.GENERATING)
        composeRule.waitForIdle()

        composeRule.closeDialogByBack()
        verify(exactly = 1) { geminiViewModel.close() }
        geminiUiState.value = MemoGeminiUiState()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onAllNodes(memoInput)[0].assert(hasText(memoText))
    }

    private fun writePrompt(
        geminiUiState: MutableStateFlow<MemoGeminiUiState>,
        memoText: String,
        prompt: String,
    ) {
        composeRule.waitForIdle()
        composeRule.onAllNodes(memoInput)[0].performTextInput(memoText)
        geminiUiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT)
        composeRule.waitForIdle()
        composeRule.onNode(promptInput).performTextInput(prompt)
        composeRule.waitForIdle()
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        geminiViewModel: MemoGeminiViewModel,
        navigateUp: () -> Unit = {},
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
                    navigateUp = navigateUp,
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

    private fun createDraft(): MemoDraft =
        MemoDraft(
            title = "제목 ${fixtureMonkey.giveMeOne<String>()}",
            description = "설명 ${fixtureMonkey.giveMeOne<String>()}",
            dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
        )

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private const val PROMPT_LABEL = "Prompt"
        private const val TITLE_LABEL = "Title"
        private const val DESCRIPTION_LABEL = "Description"
        private const val DATE_TIME_LABEL = "Date & time"
        private const val INVALID_API_KEY_MESSAGE = "Check your API key"
        private const val FAILED_MESSAGE = "Couldn't generate"

        private val promptInput = hasSetTextAction() and hasAnyAncestor(isDialog())
        private val memoInput = hasSetTextAction() and !hasAnyAncestor(isDialog())

        private fun memoText(): String = "메모 ${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun promptText(): String = "프롬프트 ${fixtureMonkey.giveMeOne<Uuid>()}"
    }
}
