package io.github.taetae98coding.diary.compose.core.text

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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

// Material 기본 제목은 카드와 대화상자 안에서 지나치게 커서 글자 크기와 줄 높이만 줄인다.
@Composable
private fun diaryMarkdownTypography(): MarkdownTypography =
    markdownTypography(
        h1 = DiaryTheme.typography.displayLarge.copy(fontSize = 28.sp, lineHeight = 36.sp),
        h2 = DiaryTheme.typography.displayMedium.copy(fontSize = 24.sp, lineHeight = 32.sp),
        h3 = DiaryTheme.typography.displaySmall.copy(fontSize = 22.sp, lineHeight = 30.sp),
        h4 = DiaryTheme.typography.headlineMedium.copy(fontSize = 20.sp, lineHeight = 28.sp),
        h5 = DiaryTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 26.sp),
        h6 = DiaryTheme.typography.titleLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
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
