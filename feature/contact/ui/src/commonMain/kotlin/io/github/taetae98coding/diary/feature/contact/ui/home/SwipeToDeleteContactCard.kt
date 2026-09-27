package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_delete_content_description
import io.github.taetae98coding.diary.feature.contact.ui.previewContact
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwipeToDeleteContactCard(
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    contact: Contact? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.contact_home_delete_content_description),
        onDelete = onDelete,
        modifier = modifier,
        key = contact?.id,
        gesturesEnabled = contact != null,
    ) {
        ContactCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            contact = contact,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeleteContactCardPreview() {
    DiaryTheme {
        SwipeToDeleteContactCard(
            onClick = {},
            onDelete = {},
            contact = previewContact(name = "김철수", phoneNumberList = listOf("010-1234-5678")),
        )
    }
}
