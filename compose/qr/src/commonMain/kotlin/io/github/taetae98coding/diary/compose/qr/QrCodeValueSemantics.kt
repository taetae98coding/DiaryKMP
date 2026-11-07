package io.github.taetae98coding.diary.compose.qr

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

public val QrCodeValueKey: SemanticsPropertyKey<String> = SemanticsPropertyKey(name = "QrCodeValue")

internal var SemanticsPropertyReceiver.qrCodeValue: String by QrCodeValueKey
