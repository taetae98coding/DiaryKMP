package io.github.taetae98coding.diary.feature.contact.ui.form

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
) {
    // 고르기가 열려 있는 상태는 스펙이 유지를 요구하지 않으므로 저장하지 않는다.
    val pickerDialogState: DialogState = DialogState()

    var birthday: ContactBirthday? by mutableStateOf(birthday)
        private set

    fun selectDate(date: LocalDate) {
        birthday = ContactBirthday(date = date, calendar = birthday?.calendar ?: ContactBirthdayCalendar.SOLAR)
    }

    fun selectCalendar(calendar: ContactBirthdayCalendar) {
        birthday = birthday?.copy(calendar = calendar)
    }

    fun clear() {
        birthday = null
    }

    companion object {
        val Saver: Saver<ContactBirthdayInputState, Any> =
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
                    )
                },
            )

        private const val DATE_KEY = "date"
        private const val CALENDAR_KEY = "calendar"
    }
}

@Composable
internal fun rememberContactBirthdayInputState(initialBirthday: ContactBirthday? = null): ContactBirthdayInputState =
    rememberSaveable(initialBirthday, saver = ContactBirthdayInputState.Saver) {
        ContactBirthdayInputState(birthday = initialBirthday)
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
