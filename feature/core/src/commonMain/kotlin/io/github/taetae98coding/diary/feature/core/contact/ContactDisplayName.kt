package io.github.taetae98coding.diary.feature.core.contact

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.feature.core.Res
import io.github.taetae98coding.diary.feature.core.contact_name_with_qualifier
import org.jetbrains.compose.resources.stringResource

@Composable
public fun contactDisplayName(
    name: String,
    nameQualifier: String,
): String =
    if (nameQualifier.isEmpty()) {
        name
    } else {
        stringResource(Res.string.contact_name_with_qualifier, name, nameQualifier)
    }

@Composable
public fun ContactListItem.displayName(): String =
    contactDisplayName(
        name = contact.detail.name,
        nameQualifier = nameQualifier,
    )
