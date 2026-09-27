package io.github.taetae98coding.diary.feature.qr.ui.code

import io.github.alexzhirkevich.qrose.matrix.qr.QrEncoder
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel

internal val QrCodeErrorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Low

private val qrCodeEncoder = QrEncoder(errorCorrectionLevel = QrErrorCorrection.L)

// 인코더는 담을 수 없는 길이를 판정하는 API를 따로 두지 않고, 가장 큰 QR에도 넘치면 예외를 던진다.
internal fun String.fitsInQrCode(): Boolean =
    try {
        qrCodeEncoder.encode(this)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
