package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.Res
import io.github.taetae98coding.diary.compose.list.list_sort_content_description
import io.github.taetae98coding.diary.core.model.list.ListSort
import org.jetbrains.compose.resources.stringResource

private val LABEL_SPACING = 4.dp

@Composable
public fun DiaryListSortBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    trailing: @Composable (() -> Unit)? = null,
) {
    val contentDescription = stringResource(Res.string.list_sort_content_description)

    Row(
        modifier = modifier.padding(horizontal = DiaryTheme.dimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onClick,
            modifier =
                Modifier
                    .weight(weight = 1F, fill = false)
                    .semantics { this.contentDescription = contentDescription },
            shape = CircleShape,
        ) {
            val sort = sortProvider()

            ListSortIcon(
                sort = sort,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.width(LABEL_SPACING))
            Text(
                text = stringResource(listSortLabel(sort = sort)),
                modifier = Modifier.weight(weight = 1F, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(LABEL_SPACING))
            DropDownIcon(modifier = Modifier.size(ButtonDefaults.IconSize))
        }

        trailing?.invoke()
    }
}

@ComponentPreview
@Composable
private fun DiaryListSortBarPreview(
    @PreviewParameter(ListSortPreviewParameter::class) sort: ListSort,
) {
    DiaryTheme {
        Surface {
            DiaryListSortBar(
                onClick = {},
                sortProvider = { sort },
            )
        }
    }
}
