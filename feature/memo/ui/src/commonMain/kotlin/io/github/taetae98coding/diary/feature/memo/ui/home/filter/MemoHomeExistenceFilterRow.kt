package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_date_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_existence_content_description
import org.jetbrains.compose.resources.stringResource

internal val MEMO_EXISTENCE_FILTER_ORDER =
    listOf(
        MemoFilterExistence.EXIST,
        MemoFilterExistence.NOT_EXIST,
        MemoFilterExistence.ALL,
    )

@Composable
internal fun MemoHomeExistenceFilterRow(
    label: String,
    onSelect: (MemoFilterExistence) -> Unit,
    modifier: Modifier = Modifier,
    existenceProvider: () -> MemoFilterExistence = { MemoFilterExistence.ALL },
) {
    val existence = existenceProvider()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = DiaryTheme.typography.bodyLarge,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1F)) {
            MEMO_EXISTENCE_FILTER_ORDER.forEachIndexed { index, value ->
                val valueLabel = value.label()
                val valueContentDescription =
                    stringResource(
                        Res.string.memo_home_filter_existence_content_description,
                        label,
                        valueLabel,
                    )

                SegmentedButton(
                    selected = existence == value,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = MEMO_EXISTENCE_FILTER_ORDER.size),
                    modifier =
                        Modifier.semantics {
                            contentDescription = valueContentDescription
                        },
                    icon = {},
                    label = { Text(text = valueLabel) },
                )
            }
        }
    }
}

private class MemoFilterExistencePreviewParameter : PreviewParameterProvider<MemoFilterExistence> {
    override val values: Sequence<MemoFilterExistence> = MEMO_EXISTENCE_FILTER_ORDER.asSequence()
}

@ComponentPreview
@Composable
private fun MemoHomeExistenceFilterRowPreview(
    @PreviewParameter(MemoFilterExistencePreviewParameter::class) existence: MemoFilterExistence,
) {
    DiaryTheme {
        Surface {
            MemoHomeExistenceFilterRow(
                label = stringResource(Res.string.memo_home_filter_date_label),
                onSelect = {},
                existenceProvider = { existence },
            )
        }
    }
}
