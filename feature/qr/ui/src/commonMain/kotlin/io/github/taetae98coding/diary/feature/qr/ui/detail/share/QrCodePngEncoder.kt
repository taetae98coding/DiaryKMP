package io.github.taetae98coding.diary.feature.qr.ui.detail.share

import io.github.taetae98coding.diary.compose.qr.qrCodePngOf
import io.github.taetae98coding.diary.feature.qr.ui.di.QrImageDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
internal class QrCodePngEncoder(
    @QrImageDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun encode(value: String): ByteArray = withContext(dispatcher) { qrCodePngOf(value = value) }
}
