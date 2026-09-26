package io.github.taetae98coding.diary.feature.qr.ui.scan

import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode

internal class QrScanReader(
    private val onRead: (String) -> Unit,
) {
    private var isRead = false

    fun read(value: String) {
        if (isRead || value.isEmpty() || !value.fitsInQrCode()) return

        isRead = true
        onRead(value)
    }
}
