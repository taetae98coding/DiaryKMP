package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.previewSearchedPlace

@Composable
internal fun PlaceSearchRow(
    place: SearchedPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(PlaceSearchRowDefaults.CornerSize))
                .clickable(role = Role.Button, onClick = onClick)
                .minimumInteractiveComponentSize()
                .padding(PlaceSearchRowDefaults.Padding),
    ) {
        Text(
            text = place.name,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (place.address.isNotBlank()) {
            Text(
                text = place.address,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun PlaceSearchRowPreview() {
    val place = remember { previewSearchedPlace(name = "서울시청", latitude = 37.5665, longitude = 126.9780) }

    DiaryTheme {
        Surface {
            PlaceSearchRow(
                place = place,
                onClick = {},
            )
        }
    }
}
