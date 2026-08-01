package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_date_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_existence_all
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_existence_exist
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_existence_not_exist
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_place_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_tag_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoHomeExistenceFilterSection(
    onEvent: (MemoHomeFilterBottomSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoHomeFilterUiState = { MemoHomeFilterUiState() },
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        MemoHomeExistenceFilterRow(
            label = stringResource(Res.string.memo_home_filter_date_label),
            onSelect = { existence -> onEvent(MemoHomeFilterBottomSheetEvent.SetDateExistence(existence = existence)) },
            modifier = Modifier.fillMaxWidth(),
            existenceProvider = { uiStateProvider().existence.date },
        )

        MemoHomeExistenceFilterRow(
            label = stringResource(Res.string.memo_home_filter_tag_label),
            onSelect = { existence -> onEvent(MemoHomeFilterBottomSheetEvent.SetTagExistence(existence = existence)) },
            modifier = Modifier.fillMaxWidth(),
            existenceProvider = { uiStateProvider().existence.tag },
        )

        MemoHomeExistenceFilterRow(
            label = stringResource(Res.string.memo_home_filter_place_label),
            onSelect = { existence -> onEvent(MemoHomeFilterBottomSheetEvent.SetPlaceExistence(existence = existence)) },
            modifier = Modifier.fillMaxWidth(),
            existenceProvider = { uiStateProvider().existence.place },
        )
    }
}

@Composable
internal fun MemoFilterExistence.label(): String =
    when (this) {
        MemoFilterExistence.ALL -> stringResource(Res.string.memo_home_filter_existence_all)
        MemoFilterExistence.EXIST -> stringResource(Res.string.memo_home_filter_existence_exist)
        MemoFilterExistence.NOT_EXIST -> stringResource(Res.string.memo_home_filter_existence_not_exist)
    }

private class MemoExistenceFilterPreviewParameter : PreviewParameterProvider<MemoExistenceFilter> {
    override val values: Sequence<MemoExistenceFilter> =
        sequenceOf(
            MemoExistenceFilter(),
            MemoExistenceFilter(
                date = MemoFilterExistence.EXIST,
                tag = MemoFilterExistence.NOT_EXIST,
                place = MemoFilterExistence.ALL,
            ),
        )
}

@ComponentPreview
@Composable
private fun MemoHomeExistenceFilterSectionPreview(
    @PreviewParameter(MemoExistenceFilterPreviewParameter::class) existence: MemoExistenceFilter,
) {
    DiaryTheme {
        Surface {
            MemoHomeExistenceFilterSection(
                onEvent = {},
                uiStateProvider = { MemoHomeFilterUiState(existence = existence) },
            )
        }
    }
}
