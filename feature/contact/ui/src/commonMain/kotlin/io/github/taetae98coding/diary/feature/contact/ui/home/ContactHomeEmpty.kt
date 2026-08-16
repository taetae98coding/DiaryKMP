package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHomeEmpty(modifier: Modifier = Modifier) {
    DiaryEmptyBox(
        title = stringResource(Res.string.contact_home_empty_title),
        modifier = modifier,
        description = stringResource(Res.string.contact_home_empty_description),
        icon = { ContactIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    )
}

@ScreenPreview
@Composable
private fun ContactHomeEmptyPreview() {
    DiaryTheme {
        ContactHomeEmpty(modifier = Modifier.fillMaxSize())
    }
}
