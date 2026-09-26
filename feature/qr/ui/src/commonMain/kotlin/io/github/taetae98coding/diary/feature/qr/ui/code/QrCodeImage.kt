@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.code

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun QrCodeImage(
    modifier: Modifier = Modifier,
    valueProvider: () -> String = { "" },
) {
    val value = valueProvider()

    Box(
        modifier =
            modifier
                .styleable(style = DiaryTheme.styles.qrImage)
                .semantics { qrCodeValue = value },
    ) {
        if (value.isNotEmpty()) {
            Image(
                painter =
                    rememberQrCodePainter(
                        data = value,
                        darkBrush = QrBrush.solid(QrCodeImageDefaults.ModuleColor),
                        errorCorrectionLevel = QrCodeErrorCorrectionLevel,
                    ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun QrCodeImagePreview() {
    DiaryTheme {
        QrCodeImage(
            modifier = Modifier.size(200.dp),
            valueProvider = { "https://example.com" },
        )
    }
}
