package io.github.taetae98coding.diary.compose.qr

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import io.github.alexzhirkevich.qrose.ImageFormat
import io.github.alexzhirkevich.qrose.toByteArray

// 이미지 인코딩이라 호출한 스레드를 막는다. 부르는 쪽이 주입받은 dispatcher로 옮긴다.
public fun qrCodePngOf(value: String): ByteArray {
    val matrix = qrCodeEncoder.encode(value)
    val size = QrCodeImageDefaults.PNG_SIZE
    val moduleSize = size / (matrix.width + QrCodeImageDefaults.PNG_QUIET_ZONE_MODULE_COUNT * 2)
    val origin = ((size - moduleSize * matrix.width) / 2).toFloat()
    val bitmap = ImageBitmap(width = size, height = size)
    val canvas = Canvas(bitmap)
    val backgroundPaint = Paint().apply { color = QrCodeImageDefaults.PngBackgroundColor }
    val modulePaint = Paint().apply { color = QrCodeImageDefaults.ModuleColor }

    canvas.drawRect(left = 0F, top = 0F, right = size.toFloat(), bottom = size.toFloat(), paint = backgroundPaint)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            if (matrix[x, y]) {
                val offset = Offset(x = origin + x * moduleSize, y = origin + y * moduleSize)
                canvas.drawRect(rect = Rect(offset = offset, size = Size(moduleSize.toFloat(), moduleSize.toFloat())), paint = modulePaint)
            }
        }
    }

    return bitmap.toByteArray(format = ImageFormat.PNG)
}
