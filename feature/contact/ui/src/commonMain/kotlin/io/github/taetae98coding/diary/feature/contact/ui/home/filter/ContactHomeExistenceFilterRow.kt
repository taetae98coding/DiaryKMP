package io.github.taetae98coding.diary.feature.contact.ui.home.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_birthday_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_existence_all
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_existence_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_existence_exist
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_existence_not_exist
import org.jetbrains.compose.resources.stringResource

internal val contactFilterExistenceList: List<ContactFilterExistence> =
    listOf(
        ContactFilterExistence.EXIST,
        ContactFilterExistence.NOT_EXIST,
        ContactFilterExistence.ALL,
    )

@Composable
internal fun ContactHomeExistenceFilterRow(
    label: String,
    onSelect: (ContactFilterExistence) -> Unit,
    modifier: Modifier = Modifier,
    existenceProvider: () -> ContactFilterExistence = { ContactFilterExistence.ALL },
) {
    val existence = existenceProvider()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = DiaryTheme.typography.bodyLarge,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1F)) {
            contactFilterExistenceList.forEachIndexed { index, value ->
                val valueLabel = value.label()
                val valueContentDescription =
                    stringResource(
                        Res.string.contact_home_filter_existence_content_description,
                        label,
                        valueLabel,
                    )

                SegmentedButton(
                    selected = existence == value,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = contactFilterExistenceList.size),
                    modifier =
                        Modifier.semantics {
                            contentDescription = valueContentDescription
                        },
                    icon = {},
                    label = { Text(text = valueLabel) },
                )
            }
        }
    }
}

@Composable
private fun ContactFilterExistence.label(): String =
    when (this) {
        ContactFilterExistence.ALL -> stringResource(Res.string.contact_home_filter_existence_all)
        ContactFilterExistence.EXIST -> stringResource(Res.string.contact_home_filter_existence_exist)
        ContactFilterExistence.NOT_EXIST -> stringResource(Res.string.contact_home_filter_existence_not_exist)
    }

private class ContactFilterExistencePreviewParameter : PreviewParameterProvider<ContactFilterExistence> {
    override val values: Sequence<ContactFilterExistence> = contactFilterExistenceList.asSequence()
}

@ComponentPreview
@Composable
private fun ContactHomeExistenceFilterRowPreview(
    @PreviewParameter(ContactFilterExistencePreviewParameter::class) existence: ContactFilterExistence,
) {
    DiaryTheme {
        Surface {
            ContactHomeExistenceFilterRow(
                label = stringResource(Res.string.contact_home_filter_birthday_label),
                onSelect = {},
                existenceProvider = { existence },
            )
        }
    }
}
