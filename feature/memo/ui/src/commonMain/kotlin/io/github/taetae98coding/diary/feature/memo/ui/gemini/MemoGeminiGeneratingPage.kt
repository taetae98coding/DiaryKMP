package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.text.DiaryMarkdown
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_description_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_generating_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_title_label
import io.github.taetae98coding.diary.feature.memo.ui.previewMemoDraftProgress
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiGeneratingPage(
    modifier: Modifier = Modifier,
    progressProvider: () -> MemoDraftProgress = { MemoDraftProgress.EMPTY },
) {
    val progress = progressProvider()

    if (progress.title.isEmpty() && progress.description.isEmpty()) {
        MemoGeminiGeneratingWaiting(modifier = modifier)
    } else {
        MemoGeminiGeneratingProgress(
            progress = progress,
            modifier = modifier,
        )
    }
}

@Composable
private fun MemoGeminiGeneratingWaiting(modifier: Modifier = Modifier) {
    val message = stringResource(Res.string.memo_gemini_generating_message)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DiaryLoadingBox(contentDescription = message)
        Text(text = message)
    }
}

@Composable
private fun MemoGeminiGeneratingProgress(
    progress: MemoDraftProgress,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(MemoGeminiGeneratingPageDefaults.ProgressSize),
                strokeWidth = MemoGeminiGeneratingPageDefaults.ProgressStrokeWidth,
            )
            Text(text = stringResource(Res.string.memo_gemini_generating_message))
        }

        if (progress.title.isNotEmpty()) {
            MemoGeminiGeneratingField(label = stringResource(Res.string.memo_gemini_title_label)) {
                Text(
                    text = progress.title,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = MemoGeminiResultPageDefaults.TITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (progress.description.isNotEmpty()) {
            MemoGeminiGeneratingField(label = stringResource(Res.string.memo_gemini_description_label)) {
                DiaryMarkdown(
                    content = progress.description,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MemoGeminiGeneratingField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = DiaryTheme.typography.labelLarge,
        )
        content()
    }
}

private class MemoDraftProgressPreviewParameter : PreviewParameterProvider<MemoDraftProgress> {
    override val values: Sequence<MemoDraftProgress> =
        sequenceOf(
            MemoDraftProgress.EMPTY,
            previewMemoDraftProgress(),
        )
}

@ComponentPreview
@Composable
private fun MemoGeminiGeneratingPagePreview(
    @PreviewParameter(MemoDraftProgressPreviewParameter::class) progress: MemoDraftProgress,
) {
    DiaryTheme {
        Surface {
            MemoGeminiGeneratingPage(progressProvider = { progress })
        }
    }
}
