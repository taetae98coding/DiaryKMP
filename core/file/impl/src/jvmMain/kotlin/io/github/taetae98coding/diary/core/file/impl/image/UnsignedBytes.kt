package io.github.taetae98coding.diary.core.file.impl.image

private const val BYTE_MASK = 0xFF
private const val BYTE_BITS = 8

internal fun ByteArray.uByte(index: Int): Int = this[index].toInt() and BYTE_MASK

internal fun ByteArray.uShort(
    index: Int,
    isLittleEndian: Boolean,
): Int =
    if (isLittleEndian) {
        uByte(index) or (uByte(index + 1) shl BYTE_BITS)
    } else {
        (uByte(index) shl BYTE_BITS) or uByte(index + 1)
    }

internal fun ByteArray.uInt(
    index: Int,
    isLittleEndian: Boolean,
): Int =
    if (isLittleEndian) {
        uShort(index = index, isLittleEndian = true) or (uShort(index = index + 2, isLittleEndian = true) shl BYTE_BITS * 2)
    } else {
        (uShort(index = index, isLittleEndian = false) shl BYTE_BITS * 2) or uShort(index = index + 2, isLittleEndian = false)
    }
