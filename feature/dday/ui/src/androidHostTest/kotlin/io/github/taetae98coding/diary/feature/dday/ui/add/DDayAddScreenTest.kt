package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_DESCRIPTION_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_END_DATE_CLEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_END_DATE_NOT_SET
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_CLAMP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_SKIP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_BABY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_COUPLE
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_CUSTOM
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_EXAM
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_REPEAT_ADD
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_REPEAT_BLANK
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TITLE_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_YEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayActionNameList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayAddRepeatOf
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDateButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDescriptionInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayedColorHex
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEditableText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEndDateNotSetButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayFormAddTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInputRepeatNumber
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayIsCountsBaseDayOn
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOpenDatePicker
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOpenEndDatePickerNotSet
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOverflowPolicyButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickDate
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickerSelection
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayProgressIndicator
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRemoveRepeat
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddaySelectOverflowPolicy
import io.github.taetae98coding.diary.feature.dday.ui.form.ddaySelectRepeatUnit
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayToggleCountsBaseDay
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTypePickerDate
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    // 무작위 컬러가 내는 값을 테스트가 정한다. 화면은 다시 그릴 때마다 무작위 컬러를 다시 꺼내므로 호출 순서가 아니라 지금 내도록 정한 값을 돌려준다.
    private var nextRandomColor: Color = Color.Unspecified

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        nextRandomColor = testColor()
        mockkStatic(RANDOM_COLOR_FILE)
        every { randomColor(random = any()) } answers { nextRandomColor }
    }

    @After
    fun tearDown() {
        unmockkStatic(RANDOM_COLOR_FILE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-2809c50f-df59-4dfc-923f-6b599367900d 한국어 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(KOREAN_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-2809c50f-df59-4dfc-923f-6b599367900d 기본 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(DEFAULT_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-4dbc1d1a-26a9-44dd-a4ca-3245dd47d761 화면에는 정해진 입력과 동작만 둔다`() {
        setScreen()
        composeRule.ddayAddRepeatOf(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        val color = composeRule.ddayDisplayedColorHex()

        val actionNameList = composeRule.ddayActionNameList().map { name -> if (name.startsWith(color)) COLOR_INPUT else name }

        actionNameList.sorted() shouldBe
            listOf(
                DDAY_FORM_DEFAULT_NAVIGATE_UP,
                DDAY_FORM_DEFAULT_PRESET_COUPLE,
                DDAY_FORM_DEFAULT_PRESET_EXAM,
                DDAY_FORM_DEFAULT_PRESET_BABY,
                DDAY_FORM_DEFAULT_PRESET_CUSTOM,
                DDAY_FORM_DEFAULT_TITLE_LABEL,
                DDAY_FORM_DEFAULT_DESCRIPTION_LABEL,
                DEFAULT_DESCRIPTION_INPUT_TAB,
                DEFAULT_DESCRIPTION_PREVIEW_TAB,
                COLOR_INPUT,
                ddayDisplayText(today()),
                DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL,
                DDAY_FORM_DEFAULT_END_DATE_NOT_SET,
                DDAY_FORM_DEFAULT_OVERFLOW_CLAMP,
                DDAY_FORM_DEFAULT_REPEAT_ADD,
                "Interval 1 number",
                "Interval 1 unit",
                "Remove interval 1",
                DDAY_FORM_DEFAULT_TAG_LABEL,
                DEFAULT_ADD,
            ).sorted()
    }

    @Test
    fun `TC-1a9d4a15-0516-4ce2-8479-6066ed9f342c 처음 들어오면 제목과 설명이 비고 기준일은 오늘, 나머지는 직접 입력 값이며 제목 입력에 초점이 있다`() {
        setScreen()

        composeRule.ddayTitleInput().ddayEditableText() shouldBe ""
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe ""
        composeRule.ddayDisplayedColorHex() shouldBe nextRandomColor.toHexString()
        composeRule.ddayDateButton(today()).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList().shouldBeEmpty()
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
        composeRule.ddayTitleInput().assertIsFocused()
    }

    @Test
    fun `TC-7f0b7860-b79c-42ea-91e9-51239823f196 진입할 때마다 컬러는 무작위로 제시된다`() {
        val colorA = nextRandomColor
        val colorB = generateSequence { testColor() }.first { color -> color != colorA }
        var isEntered by mutableStateOf(true)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            if (isEntered) {
                DDayFormTestTheme {
                    DDayAddScreen(
                        navigateUp = { isEntered = false },
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                        addViewModel = remember { relaxedViewModel() },
                        tagViewModel = remember { ddayFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.ddayDisplayedColorHex() shouldBe colorA.toHexString()

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()
        nextRandomColor = colorB
        composeRule.runOnIdle { isEntered = true }
        composeRule.waitForIdle()

        composeRule.ddayDisplayedColorHex() shouldBe colorB.toHexString()
    }

    @Test
    fun `TC-7111288b-02d3-460e-9537-e6d873d9a098 추가에 성공하면 화면에 머문 채 입력을 처음 상태로 되돌리고 성공 피드백을 제공한다`() {
        val colorA = nextRandomColor
        val colorB = generateSequence { testColor() }.first { color -> color != colorA }
        val viewModel = effectViewModel(effect = DDayAddEffect.AddSucceeded)
        var navigateUpCount = 0
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })
        fillAllInput()
        nextRandomColor = colorB

        clickAdd()

        verify(exactly = 1) {
            viewModel.add(
                input =
                    DDayInput(
                        title = MARRIAGE,
                        description = US,
                        color = colorA.toColorLong(),
                        baseDate = FILLED_BASE_DATE,
                        countsBaseDay = false,
                        repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY)),
                        endDate = FILLED_END_DATE,
                        overflowPolicy = DDayOverflowPolicy.SKIP,
                    ),
                tagIdSet = emptySet(),
            )
        }
        composeRule.ddayTitleInput().ddayEditableText() shouldBe ""
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe ""
        composeRule.ddayDisplayedColorHex() shouldBe colorB.toHexString()
        composeRule.ddayDateButton(today()).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList().shouldBeEmpty()
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
        composeRule.ddayTitleInput().assertIsFocused()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
        navigateUpCount shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-7111288b-02d3-460e-9537-e6d873d9a098 한국어 환경에서 추가 성공 피드백을 제공한다`() {
        setScreen(viewModel = effectViewModel(effect = DDayAddEffect.AddSucceeded))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        composeRule.onNodeWithContentDescription(KOREAN_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_ADD_SUCCEEDED).assertExists()
    }

    @Test
    fun `TC-e1f5c8d7-8a37-41ec-9f30-b9b9e03a90c3 추가를 처리하는 동안 진행 상태를 표시한다`() {
        val gate = CompletableDeferred<Unit>()
        val useCase = mockk<AddDDayUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            gate.await()
            Result.success(Arb.kotlinUuid().next())
        }
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = useCase))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        clickAdd()

        composeRule.onNode(ddayProgressIndicator(), useUnmergedTree = true).assertExists()
        gate.complete(Unit)
        composeRule.waitForIdle()
        composeRule.onNode(ddayProgressIndicator(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-bec3b684-290d-416a-aae4-7c0dfc96e733 저장에 실패하면 안내 없이 작성 내용을 유지하고 진행 상태만 해제한다`() {
        val useCase = mockk<AddDDayUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
        val tag = Arb.tag(title = TAG_TITLE).next()
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = useCase), tagViewModel = ddayFormAddTagViewModel(tagList = listOf(tag)))
        fillAllInput()

        clickAdd()

        assertAllInputFilled()
        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNode(ddayProgressIndicator(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertDoesNotExist()

        clickAdd()

        coVerify(exactly = 2) { useCase(parameter = any()) }
    }

    @Test
    fun `TC-564c7cda-ef2e-4e2c-b11e-ee155493a9c9 제목이 비어 있으면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "")
    }

    @Test
    fun `TC-564c7cda-ef2e-4e2c-b11e-ee155493a9c9 제목이 공백뿐이면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "   ")
    }

    private fun assertTitleBlank(title: String) {
        val useCase = mockk<AddDDayUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(DDayTitleBlankException())
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = useCase))
        composeRule.ddayTitleInput().performTextReplacement(title)
        composeRule.ddayDescriptionInput().performTextInput(US)
        composeRule.ddayPickDate(from = today(), to = FILLED_BASE_DATE)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        pickEndDate(FILLED_END_DATE)
        composeRule.ddayDescriptionInput().performClick()
        composeRule.waitForIdle()

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertExists()
        composeRule.ddayTitleInput().assertIsFocused()
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe US
        composeRule.ddayDateButton(FILLED_BASE_DATE).assertExists()
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayDateButton(FILLED_END_DATE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-564c7cda-ef2e-4e2c-b11e-ee155493a9c9 한국어 환경에서 제목 미입력 피드백을 제공한다`() {
        setScreen(viewModel = effectViewModel(effect = DDayAddEffect.TitleBlank))

        composeRule.onNodeWithContentDescription(KOREAN_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_TITLE_BLANK).assertExists()
    }

    @Test
    fun `TC-d41f448c-17eb-496c-8c34-5f2d75d50f9c 숫자가 빈 일 간격이 있으면 반복 간격의 숫자 입력을 알리고 초점을 옮기지 않는다`() {
        assertRepeatBlank(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    @Test
    fun `TC-d41f448c-17eb-496c-8c34-5f2d75d50f9c 0 개월 간격이 있으면 반복 간격의 숫자 입력을 알리고 초점을 옮기지 않는다`() {
        assertRepeatBlank(number = "0", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
    }

    @Test
    fun `TC-d41f448c-17eb-496c-8c34-5f2d75d50f9c 00 년 간격이 있으면 반복 간격의 숫자 입력을 알리고 초점을 옮기지 않는다`() {
        assertRepeatBlank(number = "00", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
    }

    private fun assertRepeatBlank(
        number: String,
        unitLabel: String,
    ) {
        val useCase = recordingUseCase()
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = useCase))
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = number, unitLabel = unitLabel)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.waitForIdle()
        composeRule.ddayTitleInput().assertIsFocused()

        clickAdd()

        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertExists()
        composeRule.ddayTitleInput().assertIsFocused()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, number to unitLabel)
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-d41f448c-17eb-496c-8c34-5f2d75d50f9c 한국어 환경에서 반복 간격 미입력 피드백을 제공한다`() {
        setScreen(viewModel = effectViewModel(effect = DDayAddEffect.RepeatBlank))

        composeRule.onNodeWithContentDescription(KOREAN_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_REPEAT_BLANK).assertExists()
    }

    @Test
    fun `TC-fe5a7317-8e74-4d21-bb4e-935169227c32 빈 간격에 1 년을 입력하면 고친 반복 간격으로 추가한다`() {
        val inputList =
            assertRepeatFixed {
                composeRule.ddayRepeatList().size shouldBe 2
                composeRule.ddaySelectRepeatUnit(position = 2, unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
                composeRule.ddayInputRepeatNumber(position = 2, number = "1")
            }

        inputList.last().repeatList.toRepeatSetOrNull() shouldBe setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12))
    }

    @Test
    fun `TC-fe5a7317-8e74-4d21-bb4e-935169227c32 빈 간격을 삭제하면 남은 반복 간격으로 추가한다`() {
        val inputList = assertRepeatFixed { composeRule.ddayRemoveRepeat(position = 2) }

        inputList.last().repeatList.toRepeatSetOrNull() shouldBe setOf(DateTimeUnit.DayBased(days = 100))
    }

    private fun assertRepeatFixed(fix: () -> Unit): List<DDayInput> {
        val inputList = mutableListOf<DDayInput>()
        val useCase = recordingUseCase(inputList = inputList)
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = useCase))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        clickAdd()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertExists()

        fix()
        clickAdd()

        inputList.size shouldBe 2
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
        return inputList
    }

    @Test
    fun `TC-8a7e7400-dedf-4926-a3f4-c0c92cdc1cfa 제목과 반복 간격이 함께 성립하지 않으면 제목 미입력만 알린다`() {
        setScreen(viewModel = DDayAddViewModel(addDDayUseCase = recordingUseCase()))
        composeRule.ddayAddRepeatOf(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayDescriptionInput().performClick()
        composeRule.waitForIdle()

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertExists()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertDoesNotExist()
        composeRule.ddayTitleInput().assertIsFocused()
    }

    @Test
    fun `TC-a5b979e2-1447-4846-a7c0-c86d3e0a12d6 뒤로가기를 선택하면 돌아가는 동작이 한 번 실행되고 추가를 요청하지 않는다`() {
        val viewModel = relaxedViewModel()
        var navigateUpCount = 0
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.add(input = any(), tagIdSet = any()) }
    }

    @Test
    fun `TC-11513348-5a0d-4704-b56e-1a5c20801aea 화면이 재생성되어도 모든 작성 내용을 유지한다`() {
        val stateRestorationTester = StateRestorationTester(composeRule)
        val viewModel = relaxedViewModel()
        val tagAddRequestKey = Arb.kotlinUuid().next()
        stateRestorationTester.setContent {
            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = remember { ddayFormAddTagViewModel() },
                )
            }
        }
        composeRule.waitForIdle()
        fillRestorationInput()
        val color = composeRule.ddayDisplayedColorHex()
        // 복원 뒤의 컬러가 무작위 컬러를 다시 꺼낸 값이 아님을 드러내도록 다음 무작위 컬러를 바꿔 둔다.
        nextRandomColor = generateSequence { testColor() }.first { value -> value.toHexString() != color }

        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertRestorationInput(color = color)
    }

    @Test
    fun `TC-11513348-5a0d-4704-b56e-1a5c20801aea 백그라운드에 다녀와도 모든 작성 내용을 유지한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val viewModel = relaxedViewModel()
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DDayFormTestTheme {
                    DDayAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                        addViewModel = viewModel,
                        tagViewModel = remember { ddayFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        fillRestorationInput()
        val color = composeRule.ddayDisplayedColorHex()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertRestorationInput(color = color)
    }

    private fun fillRestorationInput() {
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayDescriptionInput().performTextInput(US)
        composeRule.ddayPickDate(from = today(), to = FILLED_BASE_DATE)
        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayAddRepeatOf(number = "1", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayAddRepeatOf(number = "0100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        pickEndDate(FILLED_END_DATE)
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)
        composeRule.waitForIdle()
    }

    private fun assertRestorationInput(color: String) {
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe US
        composeRule.ddayDisplayedColorHex() shouldBe color
        composeRule.ddayDateButton(FILLED_BASE_DATE).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe
            listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "0100" to DDAY_FORM_DEFAULT_UNIT_DAY, "" to DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayDateButton(FILLED_END_DATE).assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH).assertExists()
    }

    @Test
    fun `TC-f810b476-7ee9-4a9a-a942-b36a5ce481a9 기준일을 고르는 중에 화면이 재생성되어도 고르기가 열린 채 남고 확인하지 않은 날짜는 반영되지 않는다`() {
        assertPickerRestored(isBaseDatePicker = true, isRecreate = true)
    }

    @Test
    fun `TC-f810b476-7ee9-4a9a-a942-b36a5ce481a9 종료일을 고르는 중에 화면이 재생성되어도 고르기가 열린 채 남고 확인하지 않은 날짜는 반영되지 않는다`() {
        assertPickerRestored(isBaseDatePicker = false, isRecreate = true)
    }

    @Test
    fun `TC-f810b476-7ee9-4a9a-a942-b36a5ce481a9 기준일을 고르는 중에 백그라운드에 다녀와도 고르기가 열린 채 남고 확인하지 않은 날짜는 반영되지 않는다`() {
        assertPickerRestored(isBaseDatePicker = true, isRecreate = false)
    }

    @Test
    fun `TC-f810b476-7ee9-4a9a-a942-b36a5ce481a9 종료일을 고르는 중에 백그라운드에 다녀와도 고르기가 열린 채 남고 확인하지 않은 날짜는 반영되지 않는다`() {
        assertPickerRestored(isBaseDatePicker = false, isRecreate = false)
    }

    private fun assertPickerRestored(
        isBaseDatePicker: Boolean,
        isRecreate: Boolean,
    ) {
        val baseDate = LocalDate(2026, 1, 15)
        val endDate = LocalDate(2026, 12, 31)
        val stateRestorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val viewModel = relaxedViewModel()
        val tagAddRequestKey = Arb.kotlinUuid().next()
        stateRestorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DDayFormTestTheme {
                    DDayAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                        addViewModel = viewModel,
                        tagViewModel = remember { ddayFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.ddayPickDate(from = today(), to = baseDate)
        pickEndDate(endDate)
        composeRule.ddayOpenDatePicker(if (isBaseDatePicker) baseDate else endDate)
        composeRule.ddayPickerSelection(if (isBaseDatePicker) baseDate else endDate).assertExists()
        composeRule.ddayTypePickerDate(LocalDate(2026, 6, 1))

        if (isRecreate) {
            stateRestorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DATE_PICKER_CONFIRM).assertExists()
        composeRule.onNodeWithText(DATE_PICKER_CANCEL).performClick()
        composeRule.waitForIdle()
        composeRule.ddayDateButton(baseDate).assertExists()
        composeRule.ddayDateButton(endDate).assertExists()
        composeRule.ddayDateButton(LocalDate(2026, 6, 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-ed519bd2-337f-4902-9d8f-6716edaaa0b7 화면이 재생성되면 표시 중이던 제목 미입력 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = DDayAddEffect.TitleBlank, message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-ed519bd2-337f-4902-9d8f-6716edaaa0b7 화면이 재생성되면 표시 중이던 반복 간격 미입력 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = DDayAddEffect.RepeatBlank, message = DDAY_FORM_DEFAULT_REPEAT_BLANK)
    }

    @Test
    fun `TC-ed519bd2-337f-4902-9d8f-6716edaaa0b7 화면이 재생성되면 표시 중이던 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = DDayAddEffect.AddSucceeded, message = DEFAULT_ADD_SUCCEEDED)
    }

    private fun assertFeedbackNotRestored(
        effect: DDayAddEffect,
        message: String,
    ) {
        val stateRestorationTester = StateRestorationTester(composeRule)
        val viewModel = effectViewModel(effect = effect)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        stateRestorationTester.setContent {
            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = remember { ddayFormAddTagViewModel() },
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
    fun `TC-65382e16-3e45-44f7-badb-0bcb01cb8fac 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다`() {
        var launchCount by mutableIntStateOf(0)
        val viewModelList = mutableListOf<DDayAddViewModel>()
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 화면 상태를 들고 있던 객체도 새로 만들어지므로, 새 키로 저장 상태 없이 새 인스턴스와 함께 화면을 다시 구성한다.
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            key(launchCount) {
                val viewModel =
                    remember {
                        (if (launchCount == 0) effectViewModel(effect = DDayAddEffect.AddSucceeded) else relaxedViewModel()).also(viewModelList::add)
                    }

                DDayFormTestTheme {
                    DDayAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                        addViewModel = viewModel,
                        tagViewModel = remember { ddayFormAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        // 추가에 성공하면 입력을 되돌리므로, 피드백을 띄운 뒤에 다시 입력해 피드백과 입력이 함께 남은 상태를 만든다.
        // 날짜 고르기는 다이얼로그 전환에 시간이 걸려 그동안 피드백이 사라지므로, 피드백 뒤에는 글자와 간격만 입력한다.
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        clickAdd()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayDescriptionInput().performTextInput(US)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        viewModelList.size shouldBe 2
        composeRule.ddayTitleInput().ddayEditableText() shouldBe ""
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe ""
        composeRule.ddayDateButton(today()).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList().shouldBeEmpty()
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-556a005f-4972-4879-8200-0816860676e3 컬러를 바꾸지 않으면 처음 제시한 무작위 컬러로 추가를 요청한다`() {
        val colorA = nextRandomColor
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        nextRandomColor = generateSequence { testColor() }.first { color -> color != colorA }

        clickAdd()

        val input = slot<DDayInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = any()) }
        input.captured.color shouldBe colorA.toColorLong()
    }

    @Test
    fun `TC-ae452255-0216-45c8-99e4-ca93f790a2cb 프리셋으로 채운 디데이는 같은 값을 직접 입력한 디데이와 같은 정보로 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        val inputList = mutableListOf<DDayInput>()
        every { viewModel.add(input = capture(inputList), tagIdSet = any()) } returns Unit
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_COUPLE).performClick()
        composeRule.waitForIdle()
        clickAdd()

        composeRule.ddayRemoveRepeat(position = 2)
        composeRule.ddayRemoveRepeat(position = 1)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = "1", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
        clickAdd()

        inputList.size shouldBe 2
        inputList[0] shouldBe inputList[1]
    }

    @Test
    fun `TC-587ea8c5-4c2e-4b8b-b05e-84f4bcffa84f 종료일을 고르면 입력한 제목과 설명, 화면의 값과 고른 종료일로 추가를 요청한다`() {
        assertInputRecorded(endDate = FILLED_END_DATE, isCleared = false)
    }

    @Test
    fun `TC-587ea8c5-4c2e-4b8b-b05e-84f4bcffa84f 종료일을 고르지 않으면 종료일 없이 추가를 요청한다`() {
        assertInputRecorded(endDate = null, isCleared = false)
    }

    @Test
    fun `TC-587ea8c5-4c2e-4b8b-b05e-84f4bcffa84f 종료일을 골랐다가 지우면 종료일 없이 추가를 요청한다`() {
        assertInputRecorded(endDate = FILLED_END_DATE, isCleared = true)
    }

    private fun assertInputRecorded(
        endDate: LocalDate?,
        isCleared: Boolean,
    ) {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(SPACED_TITLE)
        composeRule.ddayDescriptionInput().performTextInput(MULTILINE_DESCRIPTION)
        val color = composeRule.ddayDisplayedColorHex()
        composeRule.ddayPickDate(from = today(), to = FILLED_BASE_DATE)
        composeRule.ddayToggleCountsBaseDay()
        if (endDate != null) pickEndDate(endDate)
        if (isCleared) {
            composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).performClick()
            composeRule.waitForIdle()
        }
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)

        clickAdd()

        val input = slot<DDayInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = any()) }
        input.captured shouldBe
            DDayInput(
                title = SPACED_TITLE,
                description = MULTILINE_DESCRIPTION,
                color = nextRandomColor.toColorLong(),
                baseDate = FILLED_BASE_DATE,
                countsBaseDay = false,
                repeatList = emptyList(),
                endDate = if (isCleared) null else endDate,
                overflowPolicy = DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY,
            )
        nextRandomColor.toHexString() shouldBe color
    }

    @Test
    fun `TC-70087104-c956-42aa-9ecc-4c524ee03233 TC-e8560a53-6c64-47ff-aafa-ace26c47474b 제목만 입력하면 빈 설명, 간격과 종료일 없이 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(EXAM)

        clickAdd()

        val input = slot<DDayInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = emptySet()) }
        input.captured.title shouldBe EXAM
        input.captured.description shouldBe ""
        input.captured.repeatList.shouldBeEmpty()
        input.captured.repeatList.toRepeatSetOrNull() shouldBe emptySet()
        input.captured.endDate shouldBe null
    }

    @Test
    fun `TC-8d8b4fed-809e-43f8-bb9f-7e4dd295e4c1 반복 간격은 단위에 따라 일 수나 개월 수로 기록하고 앞의 0은 무시한다`() {
        assertRecordedRepeat(
            caseList =
                listOf(
                    listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY) to setOf(DateTimeUnit.DayBased(days = 100)),
                    listOf("3" to DDAY_FORM_DEFAULT_UNIT_MONTH) to setOf(DateTimeUnit.MonthBased(months = 3)),
                    listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR) to setOf(DateTimeUnit.MonthBased(months = 12)),
                    listOf("2" to DDAY_FORM_DEFAULT_UNIT_YEAR) to setOf(DateTimeUnit.MonthBased(months = 24)),
                    listOf("0100" to DDAY_FORM_DEFAULT_UNIT_DAY) to setOf(DateTimeUnit.DayBased(days = 100)),
                    listOf("012" to DDAY_FORM_DEFAULT_UNIT_MONTH) to setOf(DateTimeUnit.MonthBased(months = 12)),
                    listOf("99999" to DDAY_FORM_DEFAULT_UNIT_YEAR) to setOf(DateTimeUnit.MonthBased(months = 1_199_988)),
                ),
        )
    }

    @Test
    fun `TC-d94b8985-c09a-4b87-a558-09755b30f6b5 기록하는 값이 같은 간격은 하나로 합친다`() {
        assertRecordedRepeat(
            caseList =
                listOf(
                    listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "12" to DDAY_FORM_DEFAULT_UNIT_MONTH) to setOf(DateTimeUnit.MonthBased(months = 12)),
                    listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "0100" to DDAY_FORM_DEFAULT_UNIT_DAY) to setOf(DateTimeUnit.DayBased(days = 100)),
                    List(3) { "100" to DDAY_FORM_DEFAULT_UNIT_DAY } to setOf(DateTimeUnit.DayBased(days = 100)),
                    listOf("2" to DDAY_FORM_DEFAULT_UNIT_YEAR, "24" to DDAY_FORM_DEFAULT_UNIT_MONTH, "100" to DDAY_FORM_DEFAULT_UNIT_DAY) to
                        setOf(DateTimeUnit.MonthBased(months = 24), DateTimeUnit.DayBased(days = 100)),
                    listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "100" to DDAY_FORM_DEFAULT_UNIT_MONTH) to
                        setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 100)),
                    listOf("12" to DDAY_FORM_DEFAULT_UNIT_DAY, "12" to DDAY_FORM_DEFAULT_UNIT_MONTH) to
                        setOf(DateTimeUnit.DayBased(days = 12), DateTimeUnit.MonthBased(months = 12)),
                ),
        )
    }

    // 한 화면에서 간격을 모두 지우고 다시 입력하며 표의 행마다 추가를 요청해, 요청한 입력이 기록될 간격 모음을 확인한다.
    private fun assertRecordedRepeat(caseList: List<Pair<List<Pair<String, String>>, Set<DateTimeUnit.DateBased>>>) {
        val viewModel = relaxedViewModel()
        val inputList = mutableListOf<DDayInput>()
        every { viewModel.add(input = capture(inputList), tagIdSet = any()) } returns Unit
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        caseList.forEach { (repeatList, _) ->
            repeat(composeRule.ddayRepeatList().size) { composeRule.ddayRemoveRepeat(position = 1) }
            repeatList.forEach { (number, unit) -> composeRule.ddayAddRepeatOf(number = number, unitLabel = unit) }
            clickAdd()
        }

        inputList.map { input -> input.repeatList.toRepeatSetOrNull() } shouldBe caseList.map { (_, record) -> record }
    }

    @Test
    fun `Cmd Enter 단축키를 누르면 디데이 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        val input = slot<DDayInput>()
        verify(exactly = 1) { viewModel.add(input = capture(input), tagIdSet = emptySet()) }
        input.captured.title shouldBe MARRIAGE
    }

    private fun pickEndDate(date: LocalDate) {
        composeRule.ddayOpenEndDatePickerNotSet()
        composeRule.ddayTypePickerDate(date)
        composeRule.onNodeWithText(DATE_PICKER_CONFIRM).performClick()
        composeRule.waitForIdle()
    }

    private fun fillAllInput() {
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayDescriptionInput().performTextInput(US)
        composeRule.ddayPickDate(from = today(), to = FILLED_BASE_DATE)
        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        pickEndDate(FILLED_END_DATE)
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_SKIP)
        composeRule.waitForIdle()
    }

    private fun assertAllInputFilled() {
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe US
        composeRule.ddayDateButton(FILLED_BASE_DATE).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayDateButton(FILLED_END_DATE).assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_SKIP).assertExists()
    }

    private fun setScreen(
        viewModel: DDayAddViewModel = relaxedViewModel(),
        navigateUp: () -> Unit = {},
        tagViewModel: EntityAddTagViewModel = ddayFormAddTagViewModel(),
    ) {
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = navigateUp,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
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

    // 추가 요청을 기록하고, 제목이 비었거나 기록할 수 없는 간격이 있으면 그 이유로 거절하고 아니면 성공을 돌려준다.
    private fun recordingUseCase(inputList: MutableList<DDayInput> = mutableListOf()): AddDDayUseCase {
        val useCase = mockk<AddDDayUseCase>()
        coEvery { useCase(parameter = any()) } answers {
            val input = firstArg<AddDDayUseCase.Parameter>().input
            inputList.add(input)
            when {
                input.title.isBlank() -> Result.failure(DDayTitleBlankException())
                input.repeatList.toRepeatSetOrNull() == null -> Result.failure(DDayRepeatBlankException())
                else -> Result.success(Arb.kotlinUuid().next())
            }
        }
        return useCase
    }

    private fun relaxedViewModel(): DDayAddViewModel =
        mockk<DDayAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(DDayAddUiState())
            every { effect } returns Channel<DDayAddEffect>().receiveAsFlow()
        }

    private fun effectViewModel(effect: DDayAddEffect): DDayAddViewModel {
        val channel = Channel<DDayAddEffect>(Channel.BUFFERED)

        return mockk<DDayAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(DDayAddUiState())
            every { this@apply.effect } returns channel.receiveAsFlow()
            every { add(input = any(), tagIdSet = any()) } answers { channel.trySend(effect) }
        }
    }

    private companion object {
        const val RANDOM_COLOR_FILE = "io.github.taetae98coding.diary.library.compose.ui.color.ColorExtKt"
        const val KOREAN_TITLE = "디데이 추가"
        const val DEFAULT_TITLE = "Add D-Day"
        const val KOREAN_ADD = "디데이 추가"
        const val DEFAULT_ADD = "Add D-Day"
        const val KOREAN_ADD_SUCCEEDED = "디데이가 추가되었습니다."
        const val DEFAULT_ADD_SUCCEEDED = "D-Day added."
        const val KOREAN_TITLE_BLANK = "제목을 입력해 주세요."
        const val DEFAULT_TITLE_BLANK = "Please enter a title."
        const val KOREAN_REPEAT_BLANK = "반복 간격에 1 이상의 숫자를 입력해 주세요."
        const val DEFAULT_DESCRIPTION_INPUT_TAB = "Input"
        const val DEFAULT_DESCRIPTION_PREVIEW_TAB = "Preview"
        const val DATE_PICKER_CONFIRM = "OK"
        const val DATE_PICKER_CANCEL = "Cancel"
        const val COLOR_INPUT = "ColorInput"
        const val MARRIAGE = "결혼"
        const val TAG_TITLE = "DDayAddScreenTag"
        const val EXAM = "시험"
        const val US = "우리"
        const val SPACED_TITLE = " 결혼 "
        const val MULTILINE_DESCRIPTION = "우리\n둘"
        const val RGB_MASK = 0xFFFFFF
        const val ALPHA_MASK = 0xFF000000.toInt()
        val FILLED_BASE_DATE = LocalDate(2025, 1, 15)
        val FILLED_END_DATE = LocalDate(2030, 1, 15)

        fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        fun testColor(): Color = Color(color = ALPHA_MASK or Arb.int(0..RGB_MASK).next())
    }
}
