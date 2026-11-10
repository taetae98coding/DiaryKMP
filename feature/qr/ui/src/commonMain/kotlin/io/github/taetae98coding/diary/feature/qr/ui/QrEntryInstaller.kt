package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.QrActionLauncherFactory
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraPreviewContent
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class QrEntryInstaller(
    private val cameraPreviewContent: QrScanCameraPreviewContent,
    private val qrActionLauncherFactory: QrActionLauncherFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        qrEntry(
            backStack = navigator.backStack,
            cameraPreviewContent = cameraPreviewContent,
            qrActionLauncherFactory = qrActionLauncherFactory,
        )
    }
}
