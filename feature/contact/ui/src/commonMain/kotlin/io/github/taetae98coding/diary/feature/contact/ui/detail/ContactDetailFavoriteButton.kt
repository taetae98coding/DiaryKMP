package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.StarBorderIcon
import io.github.taetae98coding.diary.compose.core.icon.StarIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
internal fun ContactDetailFavoriteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isFavoriteProvider: () -> Boolean = { false },
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            DiaryCrossfade(targetState = FavoriteButtonContent(isInProgress = isInProgressProvider(), isFavorite = isFavoriteProvider())) { content ->
                when {
                    content.isInProgress -> CircularWavyProgressIndicator(modifier = Modifier.size(InProgressIndicatorSize))
                    content.isFavorite -> StarIcon(contentDescription = contentDescription)
                    else -> StarBorderIcon(contentDescription = contentDescription)
                }
            }
        }
    }
}

// 진행 표시와 두 아이콘이 같은 자리에서 서로를 대체하므로 전환 대상을 하나로 묶어 한 번만 교차 전환한다.
private data class FavoriteButtonContent(
    val isInProgress: Boolean,
    val isFavorite: Boolean,
)

private val InProgressIndicatorSize = 24.dp

@ComponentPreview
@Composable
private fun ContactDetailFavoriteButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFavorite: Boolean,
) {
    DiaryTheme {
        Surface {
            ContactDetailFavoriteButton(
                onClick = {},
                isFavoriteProvider = { isFavorite },
            )
        }
    }
}
