package io.github.taetae98coding.diary.compose.contact

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeContactCard(
    onEvent: (ContactListEvent) -> Unit,
    modifier: Modifier = Modifier,
    contact: ContactListItem? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.contact_list_delete_content_description),
        onDelete = { contact?.let { value -> onEvent(ContactListEvent.SwipeDelete(id = value.contact.id)) } },
        modifier = modifier,
        key = contact?.contact?.id,
        gesturesEnabled = contact != null,
    ) {
        ContactCard(
            onClick = { contact?.let { value -> onEvent(ContactListEvent.ClickContact(id = value.contact.id)) } },
            modifier = Modifier.fillMaxWidth(),
            contact = contact,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeContactCardPreview() {
    DiaryTheme {
        SwipeContactCard(
            onEvent = {},
            contact = previewContact(name = "김철수", phoneNumberList = listOf("010-1234-5678")),
        )
    }
}
