package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_description_input_tab_input_content_description
import io.github.taetae98coding.diary.compose.core.diary_description_input_tab_preview_content_description
import io.github.taetae98coding.diary.compose.core.icon.EditIcon
import io.github.taetae98coding.diary.compose.core.icon.MarkdownIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryDescriptionInputPageIcon(
    page: DiaryDescriptionInputPage,
    modifier: Modifier = Modifier,
) {
    when (page) {
        DiaryDescriptionInputPage.Input ->
            EditIcon(
                modifier = modifier,
                contentDescription = stringResource(Res.string.diary_description_input_tab_input_content_description),
            )

        DiaryDescriptionInputPage.Preview ->
            MarkdownIcon(
                modifier = modifier,
                contentDescription = stringResource(Res.string.diary_description_input_tab_preview_content_description),
            )
    }
}

@ComponentPreview
@Composable
private fun DiaryDescriptionInputPageIconPreview() {
    DiaryTheme {
        Surface {
            DiaryDescriptionInputPageIcon(page = DiaryDescriptionInputPage.Preview)
        }
    }
}
