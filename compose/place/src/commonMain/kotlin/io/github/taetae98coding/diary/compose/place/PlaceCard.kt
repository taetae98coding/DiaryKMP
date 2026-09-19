@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.place

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

public const val PLACE_CARD_TEST_TAG: String = "PlaceCard"
public const val PLACE_CARD_COLOR_INDICATOR_TEST_TAG: String = "PlaceCardColorIndicator"

@Composable
public fun PlaceCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    place: Place? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(PLACE_CARD_TEST_TAG),
        enabled = place != null,
    ) {
        Row(
            modifier = Modifier.styleable(style = DiaryTheme.styles.cardContent),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DiaryColorIndicator(
                color = place?.detail?.color?.toColor() ?: Color.Transparent,
                modifier = Modifier.testTag(PLACE_CARD_COLOR_INDICATOR_TEST_TAG),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = place?.detail?.title.orEmpty(),
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    style = DiaryTheme.typography.titleMediumEmphasized,
                )

                val address = place?.detail?.address.orEmpty()

                if (address.isNotEmpty()) {
                    Text(
                        text = address,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        maxLines = 1,
                        style = DiaryTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun PlaceCardPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isLoaded: Boolean,
) {
    val place = remember(isLoaded) { previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780).takeIf { isLoaded } }

    DiaryTheme {
        PlaceCard(
            onClick = {},
            place = place,
        )
    }
}
