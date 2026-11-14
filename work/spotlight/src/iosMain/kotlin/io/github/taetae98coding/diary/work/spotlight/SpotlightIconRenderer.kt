@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.work.spotlight

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSLock
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageSymbolConfiguration

// 같은 종류와 색의 아이콘은 같은 그림이므로 한 번 그린 PNG를 다시 쓴다.
internal class SpotlightIconRenderer {
    private val cache = mutableMapOf<Pair<SpotlightIcon, Long>, NSData?>()
    private val lock = NSLock()

    fun render(
        icon: SpotlightIcon,
        color: Long,
    ): NSData? {
        val key = icon to color

        lock.lock()
        try {
            return cache.getOrPut(key) { draw(icon = icon, color = color) }
        } finally {
            lock.unlock()
        }
    }

    private fun draw(
        icon: SpotlightIcon,
        color: Long,
    ): NSData? {
        val configuration = UIImageSymbolConfiguration.configurationWithPointSize(pointSize = ICON_POINT_SIZE)
        val symbol = UIImage.systemImageNamed(name = icon.symbolName, withConfiguration = configuration) ?: return null
        val tinted = symbol.imageWithTintColor(color = color.toUIColor(), renderingMode = UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
        val format = UIGraphicsImageRendererFormat().also { format -> format.opaque = false }
        val image =
            tinted.size.useContents {
                val width = width
                val height = height

                UIGraphicsImageRenderer(size = tinted.size, format = format).imageWithActions { _ ->
                    tinted.drawInRect(CGRectMake(x = 0.0, y = 0.0, width = width, height = height))
                }
            }

        return UIImagePNGRepresentation(image)
    }

    private companion object {
        private const val ICON_POINT_SIZE: Double = 64.0
        private const val COLOR_COMPONENT_MAX: Double = 255.0
        private const val BYTE_MASK: Long = 0xFF
        private const val ALPHA_SHIFT: Int = 24
        private const val RED_SHIFT: Int = 16
        private const val GREEN_SHIFT: Int = 8

        private val SpotlightIcon.symbolName: String
            get() =
                when (this) {
                    SpotlightIcon.MEMO -> "note.text"
                    SpotlightIcon.TAG -> "tag"
                    SpotlightIcon.PLACE -> "mappin.and.ellipse"
                    SpotlightIcon.WEB -> "globe"
                    SpotlightIcon.CONTACT -> "person.crop.circle"
                    SpotlightIcon.CHECKLIST -> "checklist"
                    SpotlightIcon.QR -> "qrcode"
                    SpotlightIcon.MUSIC -> "music.note"
                    SpotlightIcon.DDAY -> "hourglass"
                    SpotlightIcon.ROUTINE -> "repeat"
                }

        private fun Long.toUIColor(): UIColor =
            UIColor.colorWithRed(
                red = (shr(RED_SHIFT) and BYTE_MASK) / COLOR_COMPONENT_MAX,
                green = (shr(GREEN_SHIFT) and BYTE_MASK) / COLOR_COMPONENT_MAX,
                blue = (this and BYTE_MASK) / COLOR_COMPONENT_MAX,
                alpha = (shr(ALPHA_SHIFT) and BYTE_MASK) / COLOR_COMPONENT_MAX,
            )
    }
}
