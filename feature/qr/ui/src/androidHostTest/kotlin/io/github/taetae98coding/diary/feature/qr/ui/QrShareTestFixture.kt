package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.compose.core.share.AndroidContentSharerFactory
import io.github.taetae98coding.diary.feature.qr.ui.detail.share.QrCodePngEncoder
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal fun testContentSharerFactory(): AndroidContentSharerFactory = AndroidContentSharerFactory(dispatcher = UnconfinedTestDispatcher())

internal fun testQrCodePngEncoder(): QrCodePngEncoder = QrCodePngEncoder(dispatcher = UnconfinedTestDispatcher())
