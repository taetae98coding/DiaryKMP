package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.animation.animateBounds
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_select_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_select_label
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoWebFlexBox(
    onWebClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.selectedWebList.forEach { web ->
            key(web.id) {
                MemoWebChip(
                    web = web,
                    onClick = { onWebClick(web.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onAddClick,
            label = stringResource(Res.string.memo_web_select_label),
            actionLabel = stringResource(Res.string.memo_web_select_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun MemoWebFlexBoxPreview() {
    val webList = remember { listOf(previewWeb(title = "사내 위키", url = "https://wiki.example.com")) }

    DiaryTheme {
        Surface {
            MemoWebFlexBox(
                onWebClick = {},
                onAddClick = {},
                uiStateProvider = { MemoWebInputUiState(selectedWebList = webList) },
            )
        }
    }
}
