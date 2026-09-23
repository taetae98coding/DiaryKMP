@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                Text(
                    text = title,
                    modifier =
                        Modifier.padding(
                            start = DiaryTheme.dimens.pickerDialogEdgePadding,
                            top = DiaryTheme.dimens.pickerDialogEdgePadding,
                            end = DiaryTheme.dimens.pickerDialogEdgePadding,
                            bottom = DiaryTheme.dimens.pickerDialogTitleBottomPadding,
                        ),
                    color = AlertDialogDefaults.titleContentColor,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Box(
                    modifier =
                        Modifier
                            .weight(weight = 1F, fill = false)
                            .padding(
                                start = DiaryTheme.dimens.pickerDialogEdgePadding,
                                end = DiaryTheme.dimens.pickerDialogEdgePadding,
                                bottom = DiaryTheme.dimens.pickerDialogContentBottomPadding,
                            ),
                ) {
                    content()
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryPickerDialogPreview() {
    DiaryTheme {
        DiaryPickerDialog(
            title = "태그 선택",
            onDismissRequest = {},
        ) {
            Text(text = "선택할 항목이 들어간다.")
        }
    }
}
