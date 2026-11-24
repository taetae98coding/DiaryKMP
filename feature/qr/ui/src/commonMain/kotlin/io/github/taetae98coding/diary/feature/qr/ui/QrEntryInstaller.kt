package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.QrActionLauncherFactory
import io.github.taetae98coding.diary.feature.qr.ui.detail.share.QrCodePngEncoder
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraPreviewContent

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding = binding<ScreenEntryInstaller>())
internal class QrEntryInstaller(
    private val cameraPreviewContent: QrScanCameraPreviewContent,
    private val qrActionLauncherFactory: QrActionLauncherFactory,
    private val contentSharerFactory: ContentSharerFactory,
    private val qrCodePngEncoder: QrCodePngEncoder,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        qrEntry(
            backStack = navigator.backStack,
            cameraPreviewContent = cameraPreviewContent,
            qrActionLauncherFactory = qrActionLauncherFactory,
            contentSharerFactory = contentSharerFactory,
            qrCodePngEncoder = qrCodePngEncoder,
        )
    }
}
