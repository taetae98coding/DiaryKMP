package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_EMPTY_UNIT
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_DAILY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_DATE_PICKER_CANCEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_DATE_PICKER_CONFIRM
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_DESCRIPTION_INPUT_TAB
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_DESCRIPTION_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_DESCRIPTION_PREVIEW_TAB
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_TITLE_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_UNIT_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_UNIT_MILLILITER
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormActionNameList
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormColor
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDescriptionInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDisplayText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDisplayedColorHex
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputAmountPerRecord
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputGoalAmount
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormOpenDatePicker
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormPickDate
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormPickerSelection
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormProgressIndicator
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTitleInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTypePickerDate
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddScreenAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    // 무작위 컬러가 내는 값을 테스트가 정한다. 화면은 다시 그릴 때마다 무작위 컬러를 다시 꺼내므로 호출 순서가 아니라 지금 내도록 정한 값을 돌려준다.
    private var nextRandomColor: Color = Color.Unspecified

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        nextRandomColor = routineFormColor()
        mockkStatic(RANDOM_COLOR_FILE)
        every { randomColor(random = any()) } answers { nextRandomColor }
    }

    @After
    fun tearDown() {
        unmockkStatic(RANDOM_COLOR_FILE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-a8496341-e665-458a-9367-d96d315c84d1 한국어 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(KOREAN_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-a8496341-e665-458a-9367-d96d315c84d1 기본 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(DEFAULT_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-9ed1c752-2494-4356-ae23-8aee911ca5cc 화면에는 정해진 입력과 동작만 둔다`() {
        setScreen()
        // 한 번에 기록할 양의 처음 값을 지워, 값이 있을 때만 나타나는 입력 지우기 버튼을 빼고 센다.
        composeRule.routineFormInputAmountPerRecord("")
        val color = composeRule.routineFormDisplayedColorHex()

        val actionNameList = composeRule.routineFormActionNameList().map { name -> if (name.startsWith(color)) COLOR_INPUT else name }

        actionNameList.sorted() shouldBe
            listOf(
                ROUTINE_FORM_DEFAULT_NAVIGATE_UP,
                ROUTINE_FORM_DEFAULT_TITLE_LABEL,
                ROUTINE_FORM_DEFAULT_DESCRIPTION_LABEL,
                ROUTINE_FORM_DEFAULT_DESCRIPTION_INPUT_TAB,
                ROUTINE_FORM_DEFAULT_DESCRIPTION_PREVIEW_TAB,
                routineFormDisplayText(today()),
                ROUTINE_FORM_DEFAULT_CYCLE_DAILY,
                ROUTINE_FORM_DEFAULT_UNIT_LABEL,
                ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_LABEL,
                ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_LABEL,
                COLOR_INPUT,
                ROUTINE_FORM_DEFAULT_TAG_LABEL,
                DEFAULT_ADD,
            ).sorted()
    }

    @Test
    fun `TC-e5c91190-da3d-4b88-9c99-77cd327d36e7 진입할 때마다 컬러는 무작위로 제시된다`() {
        val colorA = nextRandomColor
        val colorB = otherColor(colorA)
        var isEntered by mutableStateOf(true)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            if (isEntered) {
                RoutineTestTheme {
                    RoutineAddScreen(
                        navigateUp = { isEntered = false },
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                        addViewModel = remember { relaxedViewModel() },
                        tagViewModel = remember { routineFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.routineFormDisplayedColorHex() shouldBe colorA.toHexString()

        composeRule.onNodeWithContentDescription(ROUTINE_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()
        nextRandomColor = colorB
        composeRule.runOnIdle { isEntered = true }
        composeRule.waitForIdle()

        composeRule.routineFormDisplayedColorHex() shouldBe colorB.toHexString()
    }

    @Test
    fun `TC-81afe219-36ea-43b0-89c5-e04122c6fdcd 추가에 성공하면 화면에 머문 채 입력을 처음 상태로 되돌리고 성공 피드백을 제공한다`() {
        val colorA = nextRandomColor
        val colorB = otherColor(colorA)
        val viewModel = effectViewModel(effect = RoutineAddEffect.AddSucceeded)
        var navigateUpCount = 0
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })
        fillAllInput()
        nextRandomColor = colorB

        clickAdd()

        verify(exactly = 1) {
            viewModel.add(
                input =
                    RoutineInput(
                        title = DRINK_WATER,
                        description = TWO_LITERS,
                        color = colorA.toColorLong(),
                        startDate = FILLED_START_DATE,
                        cycle = RoutineCycle.WEEKLY,
                        unit = "ml",
                        goalAmount = 2000,
                        amountPerRecord = 250,
                    ),
                tagIdSet = emptySet(),
            )
        }
        assertInitialInput()
        composeRule.routineFormDisplayedColorHex() shouldBe colorB.toHexString()
        composeRule.routineFormTitleInput().assertIsFocused()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
        navigateUpCount shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-81afe219-36ea-43b0-89c5-e04122c6fdcd 한국어 환경에서 추가 성공 피드백을 제공한다`() {
        assertKoreanFeedback(effect = RoutineAddEffect.AddSucceeded, message = KOREAN_ADD_SUCCEEDED)
    }

    @Test
    fun `TC-4a4d3211-4f99-401b-9c45-4d424331a656 추가를 처리하는 동안 진행 상태를 표시한다`() {
        val gate = CompletableDeferred<Unit>()
        val useCase = mockk<AddRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            gate.await()
            Result.success(Arb.kotlinUuid().next())
        }
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = useCase))
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)

        clickAdd()

        composeRule.onNode(routineFormProgressIndicator(), useUnmergedTree = true).assertExists()
        gate.complete(Unit)
        composeRule.waitForIdle()
        composeRule.onNode(routineFormProgressIndicator(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-73fc3bf2-1850-45f0-8332-cfeec6c06d3c 저장에 실패하면 안내 없이 작성 내용을 유지하고 진행 상태만 해제한다`() {
        val useCase = mockk<AddRoutineUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
        val tag = Arb.tag(title = TAG_TITLE).next()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = useCase), tagViewModel = routineFormAddTagViewModel(tagList = listOf(tag)))
        fillAllInput()

        clickAdd()

        assertAllInputFilled()
        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNode(routineFormProgressIndicator(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertDoesNotExist()

        clickAdd()

        coVerify(exactly = 2) { useCase(parameter = any()) }
    }

    @Test
    fun `TC-f71ab5c3-e335-4369-a9fb-42f80556a83c 제목이 비어 있으면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "")
    }

    @Test
    fun `TC-f71ab5c3-e335-4369-a9fb-42f80556a83c 제목이 공백뿐이면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "   ")
    }

    private fun assertTitleBlank(title: String) {
        val inputList = mutableListOf<RoutineInput>()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = recordingUseCase(inputList)))
        fillAllInput()
        composeRule.routineFormTitleInput().performTextReplacement(title)
        composeRule.routineFormDescriptionInput().performClick()
        composeRule.waitForIdle()

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertExists()
        composeRule.routineFormTitleInput().assertIsFocused()
        composeRule.routineFormDescriptionInput().routineFormEditableText() shouldBe TWO_LITERS
        assertRuleAndAmountFilled()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        inputList.size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-f71ab5c3-e335-4369-a9fb-42f80556a83c 한국어 환경에서 제목 미입력 안내를 제공한다`() {
        assertKoreanFeedback(effect = RoutineAddEffect.TitleBlank, message = KOREAN_TITLE_BLANK)
    }

    @Test
    fun `TC-2c47d77d-860b-4a65-a38d-fcf76ae11be1 목표량이 0이면 목표량 입력을 알리고 초점을 옮긴다`() {
        assertGoalAmountInvalid(goalAmount = "0")
    }

    @Test
    fun `TC-2c47d77d-860b-4a65-a38d-fcf76ae11be1 목표량이 000이면 목표량 입력을 알리고 초점을 옮긴다`() {
        assertGoalAmountInvalid(goalAmount = "000")
    }

    private fun assertGoalAmountInvalid(goalAmount: String) {
        val inputList = mutableListOf<RoutineInput>()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = recordingUseCase(inputList)))
        composeRule.routineFormInputUnit(ROUTINE_FORM_UNIT_MILLILITER)
        composeRule.routineFormInputGoalAmount(goalAmount)
        composeRule.routineFormInputAmountPerRecord("250")
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.waitForIdle()
        composeRule.routineFormTitleInput().assertIsFocused()

        clickAdd()

        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID).assertExists()
        composeRule.routineFormGoalAmountInput().assertIsFocused()
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe DRINK_WATER
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_FORM_UNIT_MILLILITER
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe goalAmount
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "250"
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        inputList.size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-2c47d77d-860b-4a65-a38d-fcf76ae11be1 한국어 환경에서 목표량 미입력 안내를 제공한다`() {
        assertKoreanFeedback(effect = RoutineAddEffect.GoalAmountInvalid, message = KOREAN_GOAL_AMOUNT_INVALID)
    }

    @Test
    fun `TC-764a1601-2477-44b8-ae53-b60733174d12 목표량 미입력 안내 뒤 목표량을 2000으로 고치면 그 목표량으로 추가한다`() {
        assertGoalAmountFixed(fixedGoalAmount = "2000", expected = 2000)
    }

    @Test
    fun `TC-764a1601-2477-44b8-ae53-b60733174d12 목표량 미입력 안내 뒤 목표량을 모두 지우면 목표량 없이 추가한다`() {
        assertGoalAmountFixed(fixedGoalAmount = "", expected = null)
    }

    private fun assertGoalAmountFixed(
        fixedGoalAmount: String,
        expected: Int?,
    ) {
        val inputList = mutableListOf<RoutineInput>()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = recordingUseCase(inputList)))
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.routineFormInputGoalAmount("0")
        clickAdd()
        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID).assertExists()

        composeRule.routineFormInputGoalAmount(fixedGoalAmount)
        clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
        inputList.size shouldBe 2
        inputList.last().title shouldBe DRINK_WATER
        inputList.last().goalAmount shouldBe expected
    }

    @Test
    fun `TC-335277a4-90f6-4feb-a974-6cca8254ecfb 한 번에 기록할 양이 비어 있으면 그 입력을 알리고 초점을 옮긴다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "")
    }

    @Test
    fun `TC-335277a4-90f6-4feb-a974-6cca8254ecfb 한 번에 기록할 양이 0이면 그 입력을 알리고 초점을 옮긴다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "0")
    }

    @Test
    fun `TC-335277a4-90f6-4feb-a974-6cca8254ecfb 한 번에 기록할 양이 00이면 그 입력을 알리고 초점을 옮긴다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "00")
    }

    private fun assertAmountPerRecordInvalid(amountPerRecord: String) {
        val inputList = mutableListOf<RoutineInput>()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = recordingUseCase(inputList)))
        composeRule.routineFormInputUnit(ROUTINE_FORM_UNIT_MILLILITER)
        composeRule.routineFormInputGoalAmount("2000")
        composeRule.routineFormInputAmountPerRecord(amountPerRecord)
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.waitForIdle()
        composeRule.routineFormTitleInput().assertIsFocused()

        clickAdd()

        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertExists()
        composeRule.routineFormAmountPerRecordInput().assertIsFocused()
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe DRINK_WATER
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_FORM_UNIT_MILLILITER
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe "2000"
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe amountPerRecord
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        inputList.size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-335277a4-90f6-4feb-a974-6cca8254ecfb 한국어 환경에서 한 번에 기록할 양 미입력 안내를 제공한다`() {
        assertKoreanFeedback(effect = RoutineAddEffect.AmountPerRecordInvalid, message = KOREAN_AMOUNT_PER_RECORD_INVALID)
    }

    @Test
    fun `TC-b4487a5b-6621-4291-b209-c65aaa32b7fa 제목, 목표량, 한 번에 기록할 양이 모두 성립하지 않으면 제목 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "0", amountPerRecord = "", message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-b4487a5b-6621-4291-b209-c65aaa32b7fa 제목과 한 번에 기록할 양이 성립하지 않으면 제목 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "2000", amountPerRecord = "0", message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-b4487a5b-6621-4291-b209-c65aaa32b7fa 제목과 목표량이 성립하지 않으면 제목 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "0", amountPerRecord = "250", message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-b4487a5b-6621-4291-b209-c65aaa32b7fa 목표량이 0이고 한 번에 기록할 양이 비어 있으면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = DRINK_WATER, goalAmount = "0", amountPerRecord = "", message = ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID)
    }

    @Test
    fun `TC-b4487a5b-6621-4291-b209-c65aaa32b7fa 목표량과 한 번에 기록할 양이 모두 0이면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = DRINK_WATER, goalAmount = "0", amountPerRecord = "0", message = ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID)
    }

    private fun assertFirstInvalidOnly(
        title: String,
        goalAmount: String,
        amountPerRecord: String,
        message: String,
    ) {
        val inputList = mutableListOf<RoutineInput>()
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = recordingUseCase(inputList)))
        composeRule.routineFormTitleInput().performTextReplacement(title)
        composeRule.routineFormInputGoalAmount(goalAmount)
        composeRule.routineFormInputAmountPerRecord(amountPerRecord)
        composeRule.routineFormDescriptionInput().performClick()
        composeRule.waitForIdle()

        clickAdd()

        listOf(DEFAULT_TITLE_BLANK, ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID, ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID).forEach { candidate ->
            if (candidate == message) composeRule.onNodeWithText(candidate).assertExists() else composeRule.onNodeWithText(candidate).assertDoesNotExist()
        }
        if (message == DEFAULT_TITLE_BLANK) composeRule.routineFormTitleInput().assertIsFocused() else composeRule.routineFormGoalAmountInput().assertIsFocused()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        inputList.size shouldBe 1
    }

    @Test
    fun `TC-55a4d05a-9913-48af-9b1b-db600320f17c 시작 날짜를 고르는 중에 화면이 재생성되어도 고르기가 열린 채 남는다`() {
        assertPickerRestored(isRecreate = true)
    }

    @Test
    fun `TC-55a4d05a-9913-48af-9b1b-db600320f17c 시작 날짜를 고르는 중에 백그라운드에 다녀와도 고르기가 열린 채 남는다`() {
        assertPickerRestored(isRecreate = false)
    }

    private fun assertPickerRestored(isRecreate: Boolean) {
        val startDate = LocalDate(2026, 1, 15)
        val unconfirmedDate = LocalDate(2026, 6, 1)
        val stateRestorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val viewModel = relaxedViewModel()
        val tagAddRequestKey = Arb.kotlinUuid().next()
        stateRestorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RoutineTestTheme {
                    RoutineAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                        addViewModel = viewModel,
                        tagViewModel = remember { routineFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.routineFormPickDate(from = today(), to = startDate)
        composeRule.routineFormOpenDatePicker(startDate)
        composeRule.routineFormPickerSelection(startDate).assertExists()
        composeRule.routineFormTypePickerDate(unconfirmedDate)

        if (isRecreate) {
            stateRestorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_DATE_PICKER_CONFIRM).assertExists()
        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_DATE_PICKER_CANCEL).performClick()
        composeRule.waitForIdle()
        composeRule.routineFormDateButton(startDate).assertExists()
        composeRule.routineFormDateButton(unconfirmedDate).assertDoesNotExist()
    }

    @Test
    fun `TC-5fc38b35-ce80-44cf-bf31-d9dbd02db98e 화면이 재생성되면 표시 중이던 제목 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineAddEffect.TitleBlank, message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-5fc38b35-ce80-44cf-bf31-d9dbd02db98e 화면이 재생성되면 표시 중이던 목표량 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineAddEffect.GoalAmountInvalid, message = ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID)
    }

    @Test
    fun `TC-5fc38b35-ce80-44cf-bf31-d9dbd02db98e 화면이 재생성되면 표시 중이던 한 번에 기록할 양 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineAddEffect.AmountPerRecordInvalid, message = ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID)
    }

    @Test
    fun `TC-5fc38b35-ce80-44cf-bf31-d9dbd02db98e 화면이 재생성되면 표시 중이던 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineAddEffect.AddSucceeded, message = DEFAULT_ADD_SUCCEEDED)
    }

    private fun assertFeedbackNotRestored(
        effect: RoutineAddEffect,
        message: String,
    ) {
        val stateRestorationTester = StateRestorationTester(composeRule)
        val viewModel = effectViewModel(effect = effect)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        stateRestorationTester.setContent {
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = remember { routineFormAddTagViewModel() },
                )
            }
        }
        composeRule.waitForIdle()
        clickAdd()
        composeRule.onNodeWithText(message).assertExists()

        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertDoesNotExist()
    }

    @Test
    fun `TC-542954ee-98e3-4295-8373-d8531b747d44 컬러를 바꾸지 않으면 처음 제시한 무작위 컬러로 추가를 요청한다`() {
        val colorA = nextRandomColor
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        nextRandomColor = otherColor(colorA)

        clickAdd()

        val input = slot<RoutineInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = any()) }
        input.captured.color shouldBe colorA.toColorLong()
    }

    @Test
    fun `TC-d96c125b-abf2-4da5-becd-2845439980ab 추가를 처리하는 동안 루틴 추가를 다시 실행해도 한 번만 요청한다`() {
        val gate = CompletableDeferred<Unit>()
        val useCase = mockk<AddRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            gate.await()
            Result.success(Arb.kotlinUuid().next())
        }
        setScreen(viewModel = RoutineAddViewModel(addRoutineUseCase = useCase))
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)

        clickAdd()
        clickAdd()
        gate.complete(Unit)
        composeRule.waitForIdle()

        coVerify(exactly = 1) { useCase(parameter = any()) }
    }

    @Test
    fun `Cmd Enter 단축키를 누르면 루틴 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        val input = slot<RoutineInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = emptySet()) }
        input.captured.title shouldBe DRINK_WATER
    }

    private fun assertKoreanFeedback(
        effect: RoutineAddEffect,
        message: String,
    ) {
        setScreen(viewModel = effectViewModel(effect = effect))

        composeRule.onNodeWithContentDescription(KOREAN_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertExists()
    }

    private fun fillAllInput() {
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.routineFormDescriptionInput().performTextInput(TWO_LITERS)
        composeRule.routineFormPickDate(from = today(), to = FILLED_START_DATE)
        composeRule.routineFormSelectCycle(ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY)
        composeRule.routineFormInputUnit(ROUTINE_FORM_UNIT_MILLILITER)
        composeRule.routineFormInputGoalAmount("2000")
        composeRule.routineFormInputAmountPerRecord("250")
        composeRule.waitForIdle()
    }

    private fun assertAllInputFilled() {
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe DRINK_WATER
        composeRule.routineFormDescriptionInput().routineFormEditableText() shouldBe TWO_LITERS
        assertRuleAndAmountFilled()
    }

    private fun assertRuleAndAmountFilled() {
        composeRule.routineFormDateButton(FILLED_START_DATE).assertExists()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_FORM_UNIT_MILLILITER
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe "2000"
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "250"
    }

    private fun assertInitialInput() {
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormDescriptionInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormDateButton(today()).assertExists()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_EMPTY_UNIT
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "1"
    }

    private fun setScreen(
        viewModel: RoutineAddViewModel = relaxedViewModel(),
        navigateUp: () -> Unit = {},
        tagViewModel: EntityAddTagViewModel = routineFormAddTagViewModel(),
    ) {
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = navigateUp,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun clickAdd() {
        composeRule.onNode(hasContentDescription(DEFAULT_ADD)).performClick()
        composeRule.waitForIdle()
    }

    // 추가 요청을 기록하고, 추가 성립 조건을 판단 순서대로 확인해 먼저 성립하지 않은 이유로 거절하고 아니면 성공을 돌려준다.
    private fun recordingUseCase(inputList: MutableList<RoutineInput> = mutableListOf()): AddRoutineUseCase {
        val useCase = mockk<AddRoutineUseCase>()
        coEvery { useCase(parameter = any()) } answers {
            val input = firstArg<AddRoutineUseCase.Parameter>().input
            val goalAmount = input.goalAmount
            inputList.add(input)
            when {
                input.title.isBlank() -> Result.failure(RoutineTitleBlankException())
                goalAmount != null && goalAmount !in ROUTINE_AMOUNT_RANGE -> Result.failure(RoutineGoalAmountInvalidException())
                input.amountPerRecord !in ROUTINE_AMOUNT_RANGE -> Result.failure(RoutineAmountPerRecordInvalidException())
                else -> Result.success(Arb.kotlinUuid().next())
            }
        }
        return useCase
    }

    private fun relaxedViewModel(): RoutineAddViewModel =
        mockk<RoutineAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineAddUiState())
            every { effect } returns Channel<RoutineAddEffect>().receiveAsFlow()
        }

    private fun effectViewModel(effect: RoutineAddEffect): RoutineAddViewModel {
        val channel = Channel<RoutineAddEffect>(Channel.BUFFERED)

        return mockk<RoutineAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineAddUiState())
            every { this@apply.effect } returns channel.receiveAsFlow()
            every { add(input = any(), tagIdSet = any()) } answers { channel.trySend(effect) }
        }
    }

    private companion object {
        const val RANDOM_COLOR_FILE = "io.github.taetae98coding.diary.library.compose.ui.color.ColorExtKt"
        const val KOREAN_TITLE = "루틴 추가"
        const val DEFAULT_TITLE = "Add Routine"
        const val KOREAN_ADD = "루틴 추가"
        const val DEFAULT_ADD = "Add routine"
        const val KOREAN_ADD_SUCCEEDED = "루틴이 추가되었습니다."
        const val DEFAULT_ADD_SUCCEEDED = "Routine added."
        const val KOREAN_TITLE_BLANK = "제목을 입력해 주세요."
        const val DEFAULT_TITLE_BLANK = "Please enter a title."
        const val KOREAN_GOAL_AMOUNT_INVALID = "목표량에 1 이상의 숫자를 입력하거나 비워 주세요."
        const val KOREAN_AMOUNT_PER_RECORD_INVALID = "한 번에 기록할 양에 1 이상의 숫자를 입력해 주세요."
        const val COLOR_INPUT = "ColorInput"
        const val DRINK_WATER = "물 마시기"
        const val TWO_LITERS = "하루 2L"
        const val TAG_TITLE = "RoutineAddScreenTag"
        val FILLED_START_DATE = LocalDate(2026, 3, 1)

        fun otherColor(color: Color): Color = generateSequence { routineFormColor() }.first { value -> value != color }
    }
}
