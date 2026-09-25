package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal const val NAME_INPUT_INDEX = 0
internal const val DESCRIPTION_INPUT_INDEX = 1
internal const val HEIGHT_INPUT_INDEX = 2
internal const val FOOT_SIZE_INPUT_INDEX = 3
internal const val HOMETOWN_INPUT_INDEX = 4
internal const val PHONE_NUMBER_INPUT_INDEX = 5
internal const val INPUT_COUNT_WITHOUT_PHONE_NUMBER = 5

internal const val TYPED_NAME = "ContactName"
internal const val TYPED_DESCRIPTION = "ContactDescription"
internal const val TYPED_HEIGHT = "175.5"
internal const val TYPED_FOOT_SIZE = "250"
internal const val TYPED_HOMETOWN = "강원도 춘천시"
internal const val TYPED_FIRST_PHONE_NUMBER = "010-1234-5678"
internal const val TYPED_SECOND_PHONE_NUMBER = "02-987-6543"

internal const val DEFAULT_TITLE = "Add Contact"
internal const val DEFAULT_NAME_LABEL = "Name"
internal const val DEFAULT_HEIGHT_LABEL = "Height (cm)"
internal const val DEFAULT_FOOT_SIZE_LABEL = "Shoe size (mm)"
internal const val DEFAULT_BIRTHDAY_LABEL = "Birthday"
internal const val DEFAULT_BIRTHDAY_NOT_SET = "Not set"
internal const val DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION = "Clear birthday"
internal const val DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION = "Birthday calendar"
internal const val DEFAULT_BIRTHDAY_CALENDAR_SOLAR = "Solar"
internal const val DEFAULT_BIRTHDAY_CALENDAR_LUNAR = "Lunar"
internal const val DEFAULT_HOMETOWN_LABEL = "Hometown"
internal const val DEFAULT_PHONE_NUMBER_LABEL = "Phone numbers"
internal const val DEFAULT_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION = "Add phone number"
internal const val DEFAULT_PHONE_NUMBER_REMOVE_BUTTON_DESCRIPTION = "Remove phone number"
internal const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add contact"
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Contact added."
internal const val DEFAULT_NAME_BLANK_MESSAGE = "Please enter a name."
internal const val DEFAULT_PHONE_NUMBER_BLANK_MESSAGE = "Please enter a phone number."
internal const val DEFAULT_DATE_PICKER_CONFIRM = "OK"
internal const val DEFAULT_DATE_PICKER_CANCEL = "Cancel"

internal const val KOREAN_TITLE = "연락처 추가"
internal const val KOREAN_NAME_LABEL = "이름"
internal const val KOREAN_HEIGHT_LABEL = "키 (cm)"
internal const val KOREAN_FOOT_SIZE_LABEL = "신발 사이즈 (mm)"
internal const val KOREAN_BIRTHDAY_LABEL = "생일"
internal const val KOREAN_BIRTHDAY_NOT_SET = "선택 안 함"
internal const val KOREAN_HOMETOWN_LABEL = "고향"
internal const val KOREAN_PHONE_NUMBER_LABEL = "전화번호"
internal const val KOREAN_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION = "전화번호 추가"
internal const val KOREAN_ADD_BUTTON_DESCRIPTION = "연락처 추가"
internal const val KOREAN_ADD_SUCCEEDED_MESSAGE = "연락처가 추가되었습니다."
internal const val KOREAN_NAME_BLANK_MESSAGE = "이름을 입력해 주세요."
internal const val KOREAN_PHONE_NUMBER_BLANK_MESSAGE = "전화번호를 입력해 주세요."

internal fun screenTestViewModel(
    effect: Flow<ContactAddEffect> = emptyFlow(),
    uiState: ContactAddUiState = ContactAddUiState(),
): ContactAddViewModel {
    val viewModel = mockk<ContactAddViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun effectViewModel(effect: ContactAddEffect): ContactAddViewModel {
    val channel = Channel<ContactAddEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = channel.receiveAsFlow())
    every { viewModel.add(detail = any()) } answers { channel.trySend(effect).getOrThrow() }
    return viewModel
}

internal fun ComposeContentTestRule.setContactAddScreen(
    viewModel: ContactAddViewModel = screenTestViewModel(),
    navigateUp: () -> Unit = {},
    componentVisible: ContactAddScaffoldComponentVisible = ContactAddScaffoldComponentVisible(),
    resultEventBus: ResultEventBus = ResultEventBus(),
) {
    setContent {
        ContactAddScreenTestTheme(resultEventBus = resultEventBus) {
            ContactAddScreen(
                navigateUp = navigateUp,
                componentVisibleProvider = { componentVisible },
                viewModel = viewModel,
            )
        }
    }
}

/**
 * ContactAdd 화면은 추가 결과를 [LocalResultEventBus]로 알리므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun ContactAddScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

internal fun ComposeContentTestRule.nameInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[NAME_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.heightInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEIGHT_INPUT_INDEX]

internal fun ComposeContentTestRule.footSizeInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[FOOT_SIZE_INPUT_INDEX]

internal fun ComposeContentTestRule.hometownInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HOMETOWN_INPUT_INDEX]

internal fun ComposeContentTestRule.phoneNumberInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[PHONE_NUMBER_INPUT_INDEX + row]

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.phoneNumberRowCount(): Int = onAllNodesWithContentDescription(DEFAULT_PHONE_NUMBER_REMOVE_BUTTON_DESCRIPTION).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.addPhoneNumberRow() {
    onNodeWithContentDescription(DEFAULT_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.removePhoneNumberRow(row: Int = 0) {
    onAllNodesWithContentDescription(DEFAULT_PHONE_NUMBER_REMOVE_BUTTON_DESCRIPTION)[row].performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickAdd() {
    onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.fillAllInput() {
    nameInput().performTextInput(TYPED_NAME)
    descriptionInput().performTextInput(TYPED_DESCRIPTION)
    footSizeInput().performTextInput(TYPED_FOOT_SIZE)
    hometownInput().performTextInput(TYPED_HOMETOWN)
    selectBirthday()
    addPhoneNumberRow()
    phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
    heightInput().performTextInput(TYPED_HEIGHT)
    waitForIdle()
}

internal fun ComposeContentTestRule.selectBirthday() {
    onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).performClick()
    waitForIdle()
    onNodeWithText(DEFAULT_DATE_PICKER_CONFIRM).performClick()
    waitForIdle()
}

// 오늘이 아닌 날짜를 골라 생일의 날짜만 바뀌는 경우를 만든다.
internal fun ComposeContentTestRule.reselectBirthday(): String {
    val target = reselectBirthdayDate()

    onNodeWithText(todayDisplayText()).performClick()
    waitForIdle()
    onNodeWithText(dayCellText(target)).performClick()
    waitForIdle()
    onNodeWithText(DEFAULT_DATE_PICKER_CONFIRM).performClick()
    waitForIdle()

    return displayText(year = target.year, monthNumber = target.month.number, day = target.day)
}

// 다이얼로그가 여는 오늘의 달 안에서 오늘과 다른 날짜를 고른다. 다른 달의 날짜 칸은 열린 달에 없어 고를 수 없으므로,
// 그 달의 1일을 고르고 오늘이 1일이면 2일을 고른다. 어느 달이든 두 날짜는 모두 있다.
internal fun reselectBirthdayDate(): LocalDate {
    val today = today()

    return LocalDate(year = today.year, month = today.month, day = if (today.day == FIRST_DAY_OF_MONTH) FIRST_DAY_OF_MONTH + 1 else FIRST_DAY_OF_MONTH)
}

private const val FIRST_DAY_OF_MONTH = 1

// 날짜 선택 다이얼로그의 날짜 칸은 기본 환경에서 요일과 월 이름을 모두 쓴 문구로 읽힌다.
internal fun dayCellText(date: LocalDate): String = "${DEFAULT_DAY_OF_WEEK_NAMES[date.dayOfWeek.isoDayNumber - 1]}, ${DEFAULT_FULL_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

private val DEFAULT_DAY_OF_WEEK_NAMES =
    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val DEFAULT_FULL_MONTH_NAMES =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

internal fun ComposeContentTestRule.selectBirthdayCalendar(label: String) {
    onNodeWithText(label).performClick()
    waitForIdle()
}

// 기본 환경의 날짜 표시 형식과 월 이름을 고정 값으로 두어 고른 생일의 문구를 검증한다.
internal fun todayDisplayText(): String {
    val today = today()

    return displayText(year = today.year, monthNumber = today.month.number, day = today.day)
}

internal fun today(): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

private fun displayText(
    year: Int,
    monthNumber: Int,
    day: Int,
): String = "${DEFAULT_MONTH_NAMES[monthNumber - 1]} $day, $year"

private val DEFAULT_MONTH_NAMES =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
