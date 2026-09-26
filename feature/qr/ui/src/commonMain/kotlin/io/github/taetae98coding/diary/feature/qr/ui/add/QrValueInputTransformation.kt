package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode

internal object QrValueInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val value = asCharSequence().toString()

        if (value.isNotEmpty() && !value.fitsInQrCode()) {
            revertAllChanges()
        }
    }
}
