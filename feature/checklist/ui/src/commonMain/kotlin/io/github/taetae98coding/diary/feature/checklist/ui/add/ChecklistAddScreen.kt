package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import kotlinx.coroutines.launch

@Composable
internal fun ChecklistAddScreen(
    navigateUp: () -> Unit,
    viewModel: ChecklistAddViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = rememberChecklistAddFormState()

    DiaryTitleInputFocusEffect(state = state.titleState)
    ChecklistAddScreenEffect(
        effect = viewModel.effect,
        state = state,
    )

    ChecklistAddScaffold(
        onEvent = { event ->
            when (event) {
                is ChecklistAddScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is ChecklistAddScaffoldEvent.ClickAdd -> {
                    viewModel.add(detail = state.detail, itemContentList = state.itemContentList)
                }

                is ChecklistAddScaffoldEvent.ClickItemAdd -> {
                    if (state.addItem()) {
                        coroutineScope.launch {
                            // 새 행이 배치된 뒤에야 밀려난 항목 입력의 자리를 알 수 있다.
                            withFrameNanos { }
                            state.itemInputBringIntoViewRequester.bringIntoView()
                        }
                    }
                }

                is ChecklistAddScaffoldEvent.ClickItemRemove -> {
                    state.removeItem(key = event.key)
                }
            }
        },
        modifier = modifier,
        state = state,
        uiStateProvider = { uiState },
    )
}
