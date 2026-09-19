package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapProvider

@Composable
internal actual fun rememberExternalMapOpener(): ExternalMapOpener {
    val uriHandler = LocalUriHandler.current
    val appName = LocalContext.current.packageName

    return remember(uriHandler, appName) {
        ExternalMapOpener { provider, coordinate, title, address ->
            uriHandler.openFirstAvailable(
                uriList =
                    externalMapUriList(
                        provider = provider,
                        coordinate = coordinate,
                        title = title,
                        address = address,
                        appName = appName,
                    ),
            )
        }
    }
}

// Google 지도 앱은 웹 지도 주소를 앱 링크로 가로채므로 Android에서는 별도 앱 주소를 두지 않는다.
private fun externalMapUriList(
    provider: DiaryMapProvider,
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
    appName: String,
): List<String> {
    val webUri = externalMapWebUri(provider = provider, coordinate = coordinate, title = title, address = address)

    return when (provider) {
        DiaryMapProvider.NAVER -> listOf(naverMapAppUri(coordinate = coordinate, title = title, appName = appName), webUri)
        DiaryMapProvider.GOOGLE -> listOf(webUri)
    }
}

// 여는 방법이 없는 주소는 예외로 알려지므로, 열지 못한 주소는 건너뛰고 모두 열지 못해도 실패를 전파하지 않는다.
private fun UriHandler.openFirstAvailable(uriList: List<String>) {
    for (uri in uriList) {
        if (runCatching { openUri(uri) }.isSuccess) return
    }
}
