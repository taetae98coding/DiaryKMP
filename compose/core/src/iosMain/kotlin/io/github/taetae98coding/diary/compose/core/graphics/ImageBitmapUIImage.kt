@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.core.graphics

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

public fun ImageBitmap.toUIImage(scale: Double): UIImage? {
    val bytes =
        Image
            .makeFromBitmap(asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)
            ?.bytes
            ?.takeIf { encoded -> encoded.isNotEmpty() }
            ?: return null

    val nsData =
        bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.convert())
        }

    return UIImage(data = nsData, scale = scale)
}
