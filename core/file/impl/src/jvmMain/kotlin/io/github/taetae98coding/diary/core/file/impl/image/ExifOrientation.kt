package io.github.taetae98coding.diary.core.file.impl.image

import java.awt.image.BufferedImage

internal const val EXIF_ORIENTATION_NORMAL: Int = 1

private const val EXIF_ORIENTATION_MIRROR_HORIZONTAL = 2
private const val EXIF_ORIENTATION_ROTATE_180 = 3
private const val EXIF_ORIENTATION_MIRROR_VERTICAL = 4
private const val EXIF_ORIENTATION_TRANSPOSE = 5
private const val EXIF_ORIENTATION_ROTATE_90 = 6
private const val EXIF_ORIENTATION_TRANSVERSE = 7
private const val EXIF_ORIENTATION_ROTATE_270 = 8

private const val JPEG_MARKER = 0xFF
private const val JPEG_START_OF_IMAGE = 0xD8
private const val JPEG_START_OF_SCAN = 0xDA
private const val JPEG_END_OF_IMAGE = 0xD9
private const val JPEG_APP1 = 0xE1
private const val JPEG_FIRST_SEGMENT_INDEX = 2
private const val JPEG_SEGMENT_HEADER_BYTES = 4
private const val JPEG_SEGMENT_LENGTH_BYTES = 2

private const val TIFF_LITTLE_ENDIAN = 0x4949
private const val TIFF_BIG_ENDIAN = 0x4D4D
private const val TIFF_HEADER_BYTES = 8
private const val TIFF_IFD_OFFSET_INDEX = 4
private const val TIFF_ORIENTATION_TAG = 0x0112
private const val TIFF_ENTRY_BYTES = 12
private const val TIFF_ENTRY_VALUE_INDEX = 8
private const val TIFF_ENTRY_COUNT_BYTES = 2

private val EXIF_HEADER = byteArrayOf(0x45, 0x78, 0x69, 0x66, 0x00, 0x00)

// ImageIO는 EXIF를 해석하지 않아 세워 찍은 사진이 누운 채로 읽힌다. 방향 태그만 직접 읽어 되돌린다.
internal fun ByteArray.readExifOrientation(): Int =
    findExifTiffIndex()
        ?.let { tiffIndex -> readTiffOrientation(tiffIndex = tiffIndex) }
        ?: EXIF_ORIENTATION_NORMAL

internal fun BufferedImage.applyExifOrientation(orientation: Int): BufferedImage {
    if (orientation == EXIF_ORIENTATION_NORMAL) return this

    val isTransposed = orientation >= EXIF_ORIENTATION_TRANSPOSE
    val target =
        BufferedImage(
            if (isTransposed) height else width,
            if (isTransposed) width else height,
            BufferedImage.TYPE_INT_RGB,
        )

    for (y in 0 until height) {
        for (x in 0 until width) {
            val (targetX, targetY) = orientedPoint(orientation = orientation, x = x, y = y)

            target.setRGB(targetX, targetY, getRGB(x, y))
        }
    }

    return target
}

private fun BufferedImage.orientedPoint(
    orientation: Int,
    x: Int,
    y: Int,
): Pair<Int, Int> =
    when (orientation) {
        EXIF_ORIENTATION_MIRROR_HORIZONTAL -> width - 1 - x to y
        EXIF_ORIENTATION_ROTATE_180 -> width - 1 - x to height - 1 - y
        EXIF_ORIENTATION_MIRROR_VERTICAL -> x to height - 1 - y
        EXIF_ORIENTATION_TRANSPOSE -> y to x
        EXIF_ORIENTATION_ROTATE_90 -> height - 1 - y to x
        EXIF_ORIENTATION_TRANSVERSE -> height - 1 - y to width - 1 - x
        EXIF_ORIENTATION_ROTATE_270 -> y to width - 1 - x
        else -> x to y
    }

private fun ByteArray.findExifTiffIndex(): Int? =
    jpegSegmentIndices()
        .firstOrNull { index -> uByte(index + 1) == JPEG_APP1 && hasExifHeader(index = index + JPEG_SEGMENT_HEADER_BYTES) }
        ?.let { index -> index + JPEG_SEGMENT_HEADER_BYTES + EXIF_HEADER.size }

// 세그먼트 길이를 읽어야 다음 세그먼트 위치를 알 수 있어 앞에서부터 순서대로만 훑을 수 있다.
private fun ByteArray.jpegSegmentIndices(): Sequence<Int> =
    generateSequence(JPEG_FIRST_SEGMENT_INDEX.takeIf { hasJpegStart() }) { index ->
        val segmentLength = uShort(index = index + JPEG_SEGMENT_LENGTH_BYTES, isLittleEndian = false)

        (index + JPEG_SEGMENT_LENGTH_BYTES + segmentLength).takeIf { segmentLength >= JPEG_SEGMENT_LENGTH_BYTES }
    }.takeWhile { index -> index + JPEG_SEGMENT_HEADER_BYTES <= size && uByte(index) == JPEG_MARKER && !hasImageDataMarker(index) }

private fun ByteArray.hasJpegStart(): Boolean = size >= JPEG_SEGMENT_HEADER_BYTES && uByte(0) == JPEG_MARKER && uByte(1) == JPEG_START_OF_IMAGE

private fun ByteArray.hasImageDataMarker(index: Int): Boolean = uByte(index + 1) == JPEG_START_OF_SCAN || uByte(index + 1) == JPEG_END_OF_IMAGE

private fun ByteArray.hasExifHeader(index: Int): Boolean = index + EXIF_HEADER.size <= size && EXIF_HEADER.indices.all { offset -> this[index + offset] == EXIF_HEADER[offset] }

private fun ByteArray.readTiffOrientation(tiffIndex: Int): Int {
    val isLittleEndian = tiffByteOrder(tiffIndex = tiffIndex) ?: return EXIF_ORIENTATION_NORMAL
    val ifdIndex = tiffIndex + uInt(index = tiffIndex + TIFF_IFD_OFFSET_INDEX, isLittleEndian = isLittleEndian)
    val orientation =
        tiffEntryIndices(ifdIndex = ifdIndex, isLittleEndian = isLittleEndian)
            .firstOrNull { entryIndex -> uShort(index = entryIndex, isLittleEndian = isLittleEndian) == TIFF_ORIENTATION_TAG }
            ?.let { entryIndex -> uShort(index = entryIndex + TIFF_ENTRY_VALUE_INDEX, isLittleEndian = isLittleEndian) }
            ?: EXIF_ORIENTATION_NORMAL

    return if (orientation in EXIF_ORIENTATION_NORMAL..EXIF_ORIENTATION_ROTATE_270) orientation else EXIF_ORIENTATION_NORMAL
}

private fun ByteArray.tiffByteOrder(tiffIndex: Int): Boolean? =
    if (tiffIndex + TIFF_HEADER_BYTES > size) {
        null
    } else {
        when (uShort(index = tiffIndex, isLittleEndian = false)) {
            TIFF_LITTLE_ENDIAN -> true
            TIFF_BIG_ENDIAN -> false
            else -> null
        }
    }

private fun ByteArray.tiffEntryIndices(
    ifdIndex: Int,
    isLittleEndian: Boolean,
): Sequence<Int> {
    if (ifdIndex < 0 || ifdIndex + TIFF_ENTRY_COUNT_BYTES > size) return emptySequence()

    return (0 until uShort(index = ifdIndex, isLittleEndian = isLittleEndian))
        .asSequence()
        .map { entry -> ifdIndex + TIFF_ENTRY_COUNT_BYTES + entry * TIFF_ENTRY_BYTES }
        .takeWhile { entryIndex -> entryIndex + TIFF_ENTRY_BYTES <= size }
}
