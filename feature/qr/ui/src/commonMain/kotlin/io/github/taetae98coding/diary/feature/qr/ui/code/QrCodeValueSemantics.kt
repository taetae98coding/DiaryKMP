package io.github.taetae98coding.diary.feature.qr.ui.code

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

internal val QrCodeValueKey: SemanticsPropertyKey<String> = SemanticsPropertyKey(name = "QrCodeValue")

internal var SemanticsPropertyReceiver.qrCodeValue: String by QrCodeValueKey
