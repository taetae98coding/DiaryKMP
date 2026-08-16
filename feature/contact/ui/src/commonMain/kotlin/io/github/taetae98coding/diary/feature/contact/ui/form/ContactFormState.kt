package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber

@Stable
internal class ContactFormState(
    val nameState: ContactNameInputState,
    val descriptionState: DiaryDescriptionInputState,
    val heightState: TextFieldState,
    val footSizeState: TextFieldState,
    val birthdayState: ContactBirthdayInputState,
    val phoneNumberState: ContactPhoneNumberInputState,
    val hostState: SnackbarHostState,
) {
    val detail: ContactDetail
        get() =
            ContactDetail(
                name = nameState.text.toString(),
                description = descriptionState.text.toString(),
                height = heightState.heightOrNull(),
                footSize = footSizeState.footSizeOrNull(),
                birthday = birthdayState.birthday,
                phoneNumberList = phoneNumberState.rowList.map { row -> ContactPhoneNumber(number = row.number) },
            )
}

@Composable
internal fun rememberContactAddFormState(): ContactFormState = rememberContactFormState(initialDetail = ContactDetail.EMPTY)

@Composable
internal fun rememberContactDetailFormState(initialDetail: ContactDetail = ContactDetail.EMPTY): ContactFormState = rememberContactFormState(initialDetail = initialDetail)

@Composable
private fun rememberContactFormState(initialDetail: ContactDetail): ContactFormState {
    val nameState = rememberContactNameInputState(initialText = initialDetail.name)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDetail.description)
    val heightState =
        rememberTextFieldState(
            initialText =
                initialDetail.height
                    ?.inCentimeter
                    ?.toString()
                    .orEmpty(),
        )
    val footSizeState =
        rememberTextFieldState(
            initialText =
                initialDetail.footSize
                    ?.inWholeMillimeter
                    ?.toString()
                    .orEmpty(),
        )
    val birthdayState = rememberContactBirthdayInputState(initialBirthday = initialDetail.birthday)
    val phoneNumberState =
        rememberContactPhoneNumberInputState(
            initialRowList = initialDetail.phoneNumberList.map { phoneNumber -> ContactPhoneNumberRowState(initialNumber = phoneNumber.number) },
        )
    val hostState = remember { SnackbarHostState() }

    return remember(nameState, descriptionState, heightState, footSizeState, birthdayState, phoneNumberState, hostState) {
        ContactFormState(
            nameState = nameState,
            descriptionState = descriptionState,
            heightState = heightState,
            footSizeState = footSizeState,
            birthdayState = birthdayState,
            phoneNumberState = phoneNumberState,
            hostState = hostState,
        )
    }
}
