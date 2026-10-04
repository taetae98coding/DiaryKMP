package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.web_detail_update_succeeded_message
import io.github.taetae98coding.diary.feature.web.ui.web_header_name_blank_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoadWebPageEffect(
    pageViewModel: WebDetailPageViewModel,
    state: WebDetailScaffoldState,
) {
    LaunchedEffect(state, pageViewModel) {
        snapshotFlow { state.viewMode }
            .filter { viewMode -> viewMode == WebDetailViewMode.RESPONSE }
            .collect { pageViewModel.load() }
    }
}

@Composable
internal fun WebDetailScreenEffect(
    formState: WebFormState,
    pageViewModel: WebDetailPageViewModel,
    navigateUp: () -> Unit,
    effect: Flow<WebDetailEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.web_detail_update_succeeded_message)
    val headerNameBlankMessage = stringResource(Res.string.web_header_name_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is WebDetailEffect.UpdateSucceeded -> {
                pageViewModel.refresh()
                coroutineScope.launch { formState.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is WebDetailEffect.HeaderNameBlank -> {
                coroutineScope.launch { formState.snackbarHostState.showImmediate(message = headerNameBlankMessage) }
            }

            is WebDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
