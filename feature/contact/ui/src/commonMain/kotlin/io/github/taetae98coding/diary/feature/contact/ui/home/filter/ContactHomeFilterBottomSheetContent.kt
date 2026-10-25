@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.home.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_birth_year_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_birthday_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_phone_number_label
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filter_title
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeFilterUiState
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScaffoldEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHomeFilterBottomSheetContent(
    onEvent: (ContactHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> ContactHomeFilterUiState = { ContactHomeFilterUiState() },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.contact_home_filter_title),
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .styleable(style = DiaryTheme.styles.bottomSheetContent),
        ) {
            Column(
                modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetSection),
                verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            ) {
                ContactHomeExistenceFilterRow(
                    label = stringResource(Res.string.contact_home_filter_birthday_label),
                    onSelect = { existence -> onEvent(ContactHomeScaffoldEvent.SetBirthdayExistence(existence = existence)) },
                    modifier = Modifier.fillMaxWidth(),
                    existenceProvider = { uiStateProvider().filter.birthday },
                )

                ContactHomeExistenceFilterRow(
                    label = stringResource(Res.string.contact_home_filter_birth_year_label),
                    onSelect = { existence -> onEvent(ContactHomeScaffoldEvent.SetBirthYearExistence(existence = existence)) },
                    modifier = Modifier.fillMaxWidth(),
                    existenceProvider = { uiStateProvider().filter.birthYear },
                )

                ContactHomeExistenceFilterRow(
                    label = stringResource(Res.string.contact_home_filter_phone_number_label),
                    onSelect = { existence -> onEvent(ContactHomeScaffoldEvent.SetPhoneNumberExistence(existence = existence)) },
                    modifier = Modifier.fillMaxWidth(),
                    existenceProvider = { uiStateProvider().filter.phoneNumber },
                )
            }
        }
    }
}

private class ContactFilterPreviewParameter : PreviewParameterProvider<ContactFilter> {
    override val values: Sequence<ContactFilter> =
        sequenceOf(
            ContactFilter(),
            ContactFilter(birthday = ContactFilterExistence.EXIST, birthYear = ContactFilterExistence.EXIST, phoneNumber = ContactFilterExistence.NOT_EXIST),
        )
}

@ComponentPreview
@Composable
private fun ContactHomeFilterBottomSheetContentPreview(
    @PreviewParameter(ContactFilterPreviewParameter::class) filter: ContactFilter,
) {
    DiaryTheme {
        Surface {
            ContactHomeFilterBottomSheetContent(
                onEvent = {},
                uiStateProvider = { ContactHomeFilterUiState(filter = filter) },
            )
        }
    }
}
