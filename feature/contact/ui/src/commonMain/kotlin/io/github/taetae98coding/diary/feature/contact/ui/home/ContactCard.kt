@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.icon.StarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_birthday_lunar
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_favorite_content_description
import io.github.taetae98coding.diary.feature.contact.ui.previewContact
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

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
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            ContactCardNameRow(contact = contact)

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

            val birthday = contact?.detail?.birthday

            if (birthday != null) {
                Text(
                    text = birthday.toDisplayText(),
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    style = DiaryTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ContactCardNameRow(
    modifier: Modifier = Modifier,
    contact: Contact? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ContactCardFavoriteDefaults.IconToNameSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 카드가 자식의 접근성 이름을 하나로 합치므로, 아이콘의 이름이 별도 요소가 아니라 카드 이름 맨 앞에 붙는다.
        DiaryScaleFadeVisibility(visible = contact?.isFavorite == true) {
            StarIcon(
                modifier = Modifier.size(ContactCardFavoriteDefaults.IconSize),
                contentDescription = stringResource(Res.string.contact_home_favorite_content_description),
            )
        }

        Text(
            text = contact?.detail?.name ?: LINE_RESERVATION_TEXT,
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1,
            style = DiaryTheme.typography.titleMediumEmphasized,
        )
    }
}

@Composable
private fun ContactBirthday.toDisplayText(): String =
    when (calendar) {
        ContactBirthdayCalendar.SOLAR -> date.toDisplayText()
        ContactBirthdayCalendar.LUNAR -> stringResource(Res.string.contact_home_birthday_lunar, date.toDisplayText())
    }

// 빈 문구는 한 줄 높이를 차지하지 않아 자리 표시 카드가 준비된 카드보다 낮아지므로, 보이지 않는 문자로 한 줄을 남긴다.
private const val LINE_RESERVATION_TEXT: String = "​"

private class ContactCardPreviewParameter : PreviewParameterProvider<Contact?> {
    override val values: Sequence<Contact?> =
        sequenceOf(
            previewContact(
                name = "김철수",
                phoneNumberList = listOf("010-1234-5678"),
                birthday = ContactBirthday(date = LocalDate(1990, 3, 4), calendar = ContactBirthdayCalendar.SOLAR),
                isFavorite = true,
            ),
            previewContact(
                name = "이영희",
                phoneNumberList = emptyList(),
                birthday = ContactBirthday(date = LocalDate(1990, 3, 4), calendar = ContactBirthdayCalendar.LUNAR),
            ),
            previewContact(name = "박민수", phoneNumberList = listOf("010-9876-5432")),
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
