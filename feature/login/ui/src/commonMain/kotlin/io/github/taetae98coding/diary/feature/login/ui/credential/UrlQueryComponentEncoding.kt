package io.github.taetae98coding.diary.feature.login.ui.credential

private const val HEX_RADIX = 16
private const val BYTE_MASK = 0xFF

private val unreservedCharacters =
    (('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '.', '_', '~')).map { it.code.toByte() }.toSet()

internal fun String.encodeUrlQueryComponent(): String =
    buildString {
        for (byte in this@encodeUrlQueryComponent.encodeToByteArray()) {
            if (byte in unreservedCharacters) {
                append(byte.toInt().toChar())
            } else {
                append('%')
                append((byte.toInt() and BYTE_MASK).toString(HEX_RADIX).uppercase().padStart(2, '0'))
            }
        }
    }
