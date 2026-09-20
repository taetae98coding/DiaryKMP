package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_generating_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiGeneratingPage(modifier: Modifier = Modifier) {
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

@ComponentPreview
@Composable
private fun MemoGeminiGeneratingPagePreview() {
    DiaryTheme {
        Surface {
            MemoGeminiGeneratingPage()
        }
    }
}
