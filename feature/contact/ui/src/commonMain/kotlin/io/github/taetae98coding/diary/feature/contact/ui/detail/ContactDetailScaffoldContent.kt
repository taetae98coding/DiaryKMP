package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactForm
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormEvent
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState

@Composable
internal fun ContactDetailScaffoldContent(
    onFormEvent: (ContactFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactDetailFormState(),
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is ContactDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            ContactForm(
                onEvent = onFormEvent,
                modifier = Modifier.fillMaxSize(),
                state = state,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun ContactDetailScaffoldContentPreview() {
    DiaryTheme {
        ContactDetailScaffoldContent(onFormEvent = {}, modifier = Modifier.fillMaxSize())
    }
}
