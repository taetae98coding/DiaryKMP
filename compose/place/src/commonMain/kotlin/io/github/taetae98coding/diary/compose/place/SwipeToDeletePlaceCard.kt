package io.github.taetae98coding.diary.compose.place

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeToDeletePlaceCard(
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    place: Place? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.place_list_delete_content_description),
        onDelete = onDelete,
        modifier = modifier,
        key = place?.id,
        gesturesEnabled = place != null,
    ) {
        PlaceCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            place = place,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeletePlaceCardPreview() {
    DiaryTheme {
        SwipeToDeletePlaceCard(
            onClick = {},
            onDelete = {},
            place = previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780),
        )
    }
}
