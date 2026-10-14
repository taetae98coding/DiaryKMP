package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.domain.qr.content.QrAction

@Stable
internal interface QrActionLauncher {
    fun isSupported(action: QrAction): Boolean

    suspend fun launch(action: QrAction): Boolean
}

@Composable
internal expect fun rememberQrActionLauncher(): QrActionLauncher
