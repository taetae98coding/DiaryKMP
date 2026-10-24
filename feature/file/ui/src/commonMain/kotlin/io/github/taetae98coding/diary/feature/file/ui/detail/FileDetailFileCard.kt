package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import io.github.taetae98coding.diary.compose.core.format.toDisplayText
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentContainer
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file.toDisplayText
import io.github.taetae98coding.diary.feature.file.ui.file.toFileSize
import io.github.taetae98coding.diary.feature.file.ui.file_detail_name_label
import io.github.taetae98coding.diary.feature.file.ui.file_detail_supporting_detail_format
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.text.input.TextFieldState as ComposeTextFieldState

@Composable
internal fun FileDetailFileCard(
    file: DiaryFile,
    modifier: Modifier = Modifier,
    nameState: ComposeTextFieldState = rememberTextFieldState(),
    nameFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(DiaryTheme.dimens.cardContentPadding),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            FileDetailNameInput(
                state = nameState,
                focusRequester = nameFocusRequester,
                modifier = Modifier.fillMaxWidth(),
            )
            FileDetailFileInfo(file = file)
        }
    }
}

@Composable
private fun FileDetailNameInput(
    state: ComposeTextFieldState,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        state = state,
        modifier = modifier.focusRequester(focusRequester),
        label = { Text(text = stringResource(Res.string.file_detail_name_label)) },
        leadingIcon = { FileIcon() },
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
        onKeyboardAction = { keyboardController?.hide() },
        lineLimits = TextFieldLineLimits.SingleLine,
        colors =
            TextFieldDefaults
                .colors()
                .transparentIndicator()
                .transparentContainer(),
    )
}

@Composable
private fun FileDetailFileInfo(
    file: DiaryFile,
    modifier: Modifier = Modifier,
) {
    val createdAt = remember(file.createdAt) { file.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) }

    Text(
        text =
            stringResource(
                Res.string.file_detail_supporting_detail_format,
                file.size.toFileSize().toDisplayText(),
                createdAt.date.toDisplayText(),
                createdAt.time.toDisplayText(),
            ),
        modifier = modifier,
        color = DiaryTheme.colorScheme.onSurfaceVariant,
        style = DiaryTheme.typography.bodyMedium,
    )
}

@ComponentPreview
@Composable
private fun FileDetailFileCardPreview() {
    val file = previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679)

    DiaryTheme {
        Surface {
            FileDetailFileCard(
                file = file,
                modifier = Modifier.fillMaxWidth(),
                nameState = rememberTextFieldState(initialText = file.name),
            )
        }
    }
}
