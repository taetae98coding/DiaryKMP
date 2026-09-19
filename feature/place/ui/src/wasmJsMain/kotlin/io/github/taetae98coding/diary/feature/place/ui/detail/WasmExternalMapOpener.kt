package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

@Composable
internal actual fun rememberExternalMapOpener(): ExternalMapOpener {
    val uriHandler = LocalUriHandler.current

    return remember(uriHandler) {
        ExternalMapOpener { provider, coordinate, title, address ->
            val uri = externalMapWebUri(provider = provider, coordinate = coordinate, title = title, address = address)

            runCatching { uriHandler.openUri(uri) }
        }
    }
}
