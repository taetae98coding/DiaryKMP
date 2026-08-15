package io.github.taetae98coding.diary.compose.web

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web

public const val WEB_CARD_TEST_TAG: String = "WebCard"

@Composable
public fun WebCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    web: Web? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(WEB_CARD_TEST_TAG),
        enabled = web != null,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = web?.detail?.title.orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )
            Text(
                text = web?.detail?.url.orEmpty(),
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                style = DiaryTheme.typography.bodySmall,
            )
        }
    }
}

private class WebCardPreviewParameter : PreviewParameterProvider<Web?> {
    override val values: Sequence<Web?> =
        sequenceOf(
            previewWeb(title = "웹 제목", url = "https://developer.android.com"),
            null,
        )
}

@ComponentPreview
@Composable
private fun WebCardPreview(
    @PreviewParameter(WebCardPreviewParameter::class) web: Web?,
) {
    DiaryTheme {
        WebCard(
            onClick = {},
            web = web,
        )
    }
}
