package io.github.taetae98coding.diary.feature.qr.ui.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.previewQr
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_delete_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwipeToDeleteQrCard(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    qr: Qr? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.qr_home_delete_content_description),
        onDelete = onDelete,
        modifier = modifier,
        key = qr?.id,
        gesturesEnabled = qr != null,
    ) {
        QrCard(
            modifier = Modifier.fillMaxWidth(),
            qr = qr,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeleteQrCardPreview() {
    DiaryTheme {
        SwipeToDeleteQrCard(
            onDelete = {},
            qr = previewQr(title = "회사 출입"),
        )
    }
}
