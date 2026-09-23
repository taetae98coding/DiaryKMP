package io.github.taetae98coding.diary.compose.core.text

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import io.github.taetae98coding.diary.compose.core.input.withMarkdownHardLineBreak
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryMarkdown(
    content: String,
    modifier: Modifier = Modifier,
) {
    Markdown(
        content = content.withMarkdownHardLineBreak(),
        modifier = modifier,
        typography = diaryMarkdownTypography(),
    )
}

@Composable
private fun diaryMarkdownTypography(): MarkdownTypography =
    markdownTypography(
        h1 = DiaryTheme.typography.displayLarge.copy(fontSize = DiaryMarkdownDefaults.H1FontSize, lineHeight = DiaryMarkdownDefaults.H1LineHeight),
        h2 = DiaryTheme.typography.displayMedium.copy(fontSize = DiaryMarkdownDefaults.H2FontSize, lineHeight = DiaryMarkdownDefaults.H2LineHeight),
        h3 = DiaryTheme.typography.displaySmall.copy(fontSize = DiaryMarkdownDefaults.H3FontSize, lineHeight = DiaryMarkdownDefaults.H3LineHeight),
        h4 = DiaryTheme.typography.headlineMedium.copy(fontSize = DiaryMarkdownDefaults.H4FontSize, lineHeight = DiaryMarkdownDefaults.H4LineHeight),
        h5 = DiaryTheme.typography.headlineSmall.copy(fontSize = DiaryMarkdownDefaults.H5FontSize, lineHeight = DiaryMarkdownDefaults.H5LineHeight),
        h6 = DiaryTheme.typography.titleLarge.copy(fontSize = DiaryMarkdownDefaults.H6FontSize, lineHeight = DiaryMarkdownDefaults.H6LineHeight),
    )

@ComponentPreview
@Composable
private fun DiaryMarkdownPreview() {
    DiaryTheme {
        Surface {
            DiaryMarkdown(content = "# 제목\n본문 첫 줄\n본문 둘째 줄")
        }
    }
}
