package io.github.taetae98coding.diary.feature.tag.ui.home.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun TagHomeFilterContent(modifier: Modifier = Modifier) {
    val viewModel = metroViewModel<TagHomeFilterViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TagHomeFilterBottomSheetContent(
        uiStateProvider = { uiState },
        onEvent = { event ->
            when (event) {
                is TagHomeFilterBottomSheetEvent.SetTopLevelOnly -> {
                    if (event.isTopLevelOnly) {
                        viewModel.enableTopLevelOnly()
                    } else {
                        viewModel.disableTopLevelOnly()
                    }
                }
            }
        },
        modifier = modifier,
    )
}
