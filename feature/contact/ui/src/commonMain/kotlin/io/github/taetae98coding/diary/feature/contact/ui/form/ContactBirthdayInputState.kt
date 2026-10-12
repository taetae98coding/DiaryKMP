package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import kotlinx.datetime.LocalDate

@Stable
internal class ContactBirthdayInputState(
    birthday: ContactBirthday?,
    val birthYearState: TextFieldState =
        TextFieldState(
            initialText =
                birthday
                    ?.date
                    ?.year
                    ?.toString()
                    .orEmpty(),
        ),
) {
    val pickerDialogState: DialogState = DialogState()

    var birthday: ContactBirthday? by mutableStateOf(birthday)
        private set

    // 입력이 네 자리의 0~9 숫자만 남기므로 비어 있지 않은 값은 항상 정수로 읽을 수 있다.
    val birthYear: Int?
        get() = birthYearState.text.toString().toIntOrNull()

    fun selectDate(date: LocalDate) {
        birthday = ContactBirthday(date = date, calendar = birthday?.calendar ?: ContactBirthdayCalendar.SOLAR)
        birthYearState.setTextAndPlaceCursorAtEnd(date.year.toString())
    }

    fun selectCalendar(calendar: ContactBirthdayCalendar) {
        birthday = birthday?.copy(calendar = calendar)
    }

    fun clear() {
        birthday = null
        birthYearState.clearText()
    }

    companion object {
        private const val DATE_KEY = "date"
        private const val CALENDAR_KEY = "calendar"

        // 태어난 연도 입력은 자기 상태를 따로 저장하므로, 복원할 때 이미 복원된 그 상태를 다시 연결한다.
        fun saver(birthYearState: TextFieldState): Saver<ContactBirthdayInputState, Any> =
            mapSaver(
                save = { state ->
                    mapOf(
                        DATE_KEY to state.birthday?.date?.toEpochDays(),
                        CALENDAR_KEY to state.birthday?.calendar?.name,
                    )
                },
                restore = { saved ->
                    val epochDay = saved[DATE_KEY] as Long?
                    val calendarName = saved[CALENDAR_KEY] as String?

                    ContactBirthdayInputState(
                        birthday =
                            if (epochDay == null || calendarName == null) {
                                null
                            } else {
                                ContactBirthday(
                                    date = LocalDate.fromEpochDays(epochDay),
                                    calendar = ContactBirthdayCalendar.valueOf(calendarName),
                                )
                            },
                        birthYearState = birthYearState,
                    )
                },
            )
    }
}

@Composable
internal fun rememberContactBirthdayInputState(
    initialBirthday: ContactBirthday? = null,
    initialBirthYear: Int? = initialBirthday?.date?.year,
): ContactBirthdayInputState {
    val birthYearState = rememberTextFieldState(initialText = initialBirthYear?.toString().orEmpty())

    return rememberSaveable(birthYearState, saver = ContactBirthdayInputState.saver(birthYearState = birthYearState)) {
        ContactBirthdayInputState(birthday = initialBirthday, birthYearState = birthYearState)
    }
}

internal fun LocalDate?.toEpochDayOrUnset(): Long = this?.toEpochDays() ?: UNSET_EPOCH_DAY

internal fun Long.toBirthdayDateOrNull(): LocalDate? =
    if (this == UNSET_EPOCH_DAY) {
        null
    } else {
        LocalDate.fromEpochDays(this)
    }

// 고르지 않은 생일을 어느 날짜보다도 이전으로 다루어 값이 바뀌는 방향을 정한다.
internal const val UNSET_EPOCH_DAY: Long = Long.MIN_VALUE
