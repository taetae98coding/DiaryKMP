package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.contact.ui.previewContact

internal const val CONTACT_CARD_TEST_TAG: String = "ContactCard"

@Composable
internal fun ContactCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contact: Contact? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(CONTACT_CARD_TEST_TAG),
        enabled = contact != null,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = contact?.detail?.name ?: LINE_RESERVATION_TEXT,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )

            val phoneNumber =
                contact
                    ?.detail
                    ?.phoneNumberList
                    ?.firstOrNull()
                    ?.number

            if (contact == null || phoneNumber != null) {
                Text(
                    text = phoneNumber ?: LINE_RESERVATION_TEXT,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    style = DiaryTheme.typography.bodySmall,
                )
            }
        }
    }
}

// 빈 문구는 한 줄 높이를 차지하지 않아 자리 표시 카드가 준비된 카드보다 낮아지므로, 보이지 않는 문자로 한 줄을 남긴다.
private const val LINE_RESERVATION_TEXT: String = "​"

private class ContactCardPreviewParameter : PreviewParameterProvider<Contact?> {
    override val values: Sequence<Contact?> =
        sequenceOf(
            previewContact(name = "김철수", phoneNumberList = listOf("010-1234-5678")),
            previewContact(name = "이영희", phoneNumberList = emptyList()),
            null,
        )
}

@ComponentPreview
@Composable
private fun ContactCardPreview(
    @PreviewParameter(ContactCardPreviewParameter::class) contact: Contact?,
) {
    DiaryTheme {
        ContactCard(
            onClick = {},
            contact = contact,
        )
    }
}
