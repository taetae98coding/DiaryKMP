@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.card

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeImage
import io.github.taetae98coding.diary.feature.qr.ui.previewQr

internal const val QR_CARD_TEST_TAG: String = "QrCard"

@Composable
internal fun QrCard(
    modifier: Modifier = Modifier,
    qr: Qr? = null,
) {
    Card(
        modifier =
            modifier
                .testTag(QR_CARD_TEST_TAG)
                .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.cardLineSpacing),
        ) {
            QrCodeImage(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F),
                valueProvider = { qr?.detail?.value.orEmpty() },
            )
            Text(
                text = qr?.detail?.title.orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )
        }
    }
}

private class QrCardPreviewParameter : PreviewParameterProvider<Qr?> {
    override val values: Sequence<Qr?> =
        sequenceOf(
            previewQr(title = "회사 출입"),
            null,
        )
}

@ComponentPreview
@Composable
private fun QrCardPreview(
    @PreviewParameter(QrCardPreviewParameter::class) qr: Qr?,
) {
    DiaryTheme {
        QrCard(qr = qr)
    }
}
