package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

@Stable
internal class ContactPhoneNumberRowState(
    initialNumber: String,
) {
    val numberState: TextFieldState = TextFieldState(initialText = initialNumber)

    val number: String
        get() = numberState.text.toString()
}

@Stable
internal class ContactPhoneNumberInputState(
    initialRowList: List<ContactPhoneNumberRowState>,
) {
    val rowList: SnapshotStateList<ContactPhoneNumberRowState> = initialRowList.toMutableStateList()

    fun add() {
        rowList.add(ContactPhoneNumberRowState(initialNumber = ""))
    }

    fun remove(row: ContactPhoneNumberRowState) {
        rowList.remove(row)
    }

    fun clear() {
        rowList.clear()
    }

    companion object {
        val Saver: Saver<ContactPhoneNumberInputState, List<String>> =
            Saver(
                save = { state -> state.rowList.map { row -> row.number } },
                restore = { saved ->
                    ContactPhoneNumberInputState(
                        initialRowList = saved.map { number -> ContactPhoneNumberRowState(initialNumber = number) },
                    )
                },
            )
    }
}

@Composable
internal fun rememberContactPhoneNumberInputState(initialRowList: List<ContactPhoneNumberRowState> = emptyList()): ContactPhoneNumberInputState =
    rememberSaveable(saver = ContactPhoneNumberInputState.Saver) {
        ContactPhoneNumberInputState(initialRowList = initialRowList)
    }
