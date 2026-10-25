package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_item_remove_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_items_label
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemInputRow
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemRemoveButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistAddItemCard(
    onEvent: (ChecklistAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistAddFormState = rememberChecklistAddFormState(),
) {
    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(alignment = Alignment.TopStart),
        ) {
            Text(
                text = stringResource(Res.string.checklist_add_items_label),
                modifier =
                    Modifier.padding(
                        start = DiaryTheme.dimens.cardContentPadding,
                        top = DiaryTheme.dimens.cardContentPadding,
                        end = DiaryTheme.dimens.cardContentPadding,
                    ),
                style = DiaryTheme.typography.labelLarge,
            )

            state.itemList.forEachIndexed { index, item ->
                key(item.key) {
                    ItemRow(
                        onRemove = { onEvent(ChecklistAddScaffoldEvent.ClickItemRemove(key = item.key)) },
                        content = item.content,
                        position = index + 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            ChecklistItemInputRow(
                onAdd = { onEvent(ChecklistAddScaffoldEvent.ClickItemAdd) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(state.itemInputBringIntoViewRequester)
                        .padding(
                            start = DiaryTheme.dimens.cardContentPadding,
                            bottom = DiaryTheme.dimens.cardContentPadding,
                        ),
                state = state.itemInputState,
            )
        }
    }
}

@Composable
private fun ItemRow(
    onRemove: () -> Unit,
    content: String,
    position: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = DiaryTheme.dimens.cardContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = content,
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.bodyLarge,
        )
        ChecklistItemRemoveButton(
            onClick = onRemove,
            contentDescription = stringResource(Res.string.checklist_add_item_remove_button_content_description, position.toString()),
        )
    }
}

@ComponentPreview
@Composable
private fun ChecklistAddItemCardPreview() {
    DiaryTheme {
        Surface {
            ChecklistAddItemCard(
                onEvent = {},
                state =
                    rememberChecklistAddFormState(
                        initialItemList =
                            listOf(
                                ChecklistAddItem(key = "1", content = "여권"),
                                ChecklistAddItem(key = "2", content = "충전기"),
                            ),
                    ),
            )
        }
    }
}
