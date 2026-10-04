package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_location_map_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrLocationMap(
    modifier: Modifier = Modifier,
    state: QrContentFormState = rememberQrContentFormState(),
) {
    val contentDescription = stringResource(Res.string.qr_location_map_content_description)

    DiaryMap(
        state = state.mapState,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onSpotClick = state::selectSpotOnMap,
    )
}

@ComponentPreview
@Composable
private fun QrLocationMapPreview() {
    DiaryTheme {
        Surface {
            QrLocationMap()
        }
    }
}
