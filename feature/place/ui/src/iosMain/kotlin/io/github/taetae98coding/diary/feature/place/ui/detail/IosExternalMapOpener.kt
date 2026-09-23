package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import platform.Foundation.NSBundle
import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication

@Composable
internal actual fun rememberExternalMapOpener(): ExternalMapOpener {
    val appName = remember { NSBundle.mainBundle.bundleIdentifier.orEmpty() }

    return remember(appName) {
        ExternalMapOpener { provider, coordinate, title, address ->
            openFirstAvailable(
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
        DiaryMapProvider.GOOGLE -> listOf(googleMapAppUri(coordinate = coordinate, title = title, address = address), webUri)
    }
}

// iOS는 주소를 열지 못한 것을 예외가 아니라 완료 결과로 알려 주므로, 결과를 받은 뒤에 다음 주소로 넘어간다.
private fun openFirstAvailable(uriList: List<String>) {
    val uri = uriList.firstOrNull() ?: return
    val url = URLWithString(uri)

    if (url == null) {
        openFirstAvailable(uriList = uriList.drop(1))
        return
    }

    UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>(),
        completionHandler = { isOpened ->
            if (!isOpened) {
                openFirstAvailable(uriList = uriList.drop(1))
            }
        },
    )
}
