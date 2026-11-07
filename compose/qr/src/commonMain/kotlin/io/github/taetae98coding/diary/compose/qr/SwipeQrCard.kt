package io.github.taetae98coding.diary.compose.qr

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeQrCard(
    onEvent: (QrListEvent) -> Unit,
    modifier: Modifier = Modifier,
    qr: Qr? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.qr_list_delete_content_description),
        onDelete = { qr?.let { value -> onEvent(QrListEvent.SwipeDelete(id = value.id)) } },
        modifier = modifier,
        key = qr?.id,
        gesturesEnabled = qr != null,
    ) {
        QrCard(
            onClick = { qr?.let { value -> onEvent(QrListEvent.ClickQr(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            qr = qr,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeQrCardPreview() {
    DiaryTheme {
        SwipeQrCard(
            onEvent = {},
            qr = previewQr(title = "회사 출입"),
        )
    }
}
