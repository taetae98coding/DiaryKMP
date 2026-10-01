package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactAddFormState
import kotlin.uuid.Uuid

@Composable
internal fun ContactAddScreen(
    navigateUp: () -> Unit,
    addedResultRequestKey: Uuid?,
    componentVisibleProvider: () -> ContactAddScaffoldComponentVisible,
    viewModel: ContactAddViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberContactAddFormState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RequestFocusEffect(focusRequester = state.nameState.focusRequester)
    ContactAddScreenEffect(
        addedResultRequestKey = addedResultRequestKey,
        effect = viewModel.effect,
        state = state,
    )

    ContactAddScaffold(
        onEvent = { event ->
            when (event) {
                is ContactAddScaffoldEvent.ClickNavigateUp -> navigateUp()
                is ContactAddScaffoldEvent.ClickAdd -> viewModel.add(detail = state.detail)
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
