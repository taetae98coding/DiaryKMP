package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Sort: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Sort",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(160.0f, 720.0f)
                quadTo(143.0f, 720.0f, 131.5f, 708.5f)
                quadTo(120.0f, 697.0f, 120.0f, 680.0f)
                quadTo(120.0f, 663.0f, 131.5f, 651.5f)
                quadTo(143.0f, 640.0f, 160.0f, 640.0f)
                lineTo(320.0f, 640.0f)
                quadTo(337.0f, 640.0f, 348.5f, 651.5f)
                quadTo(360.0f, 663.0f, 360.0f, 680.0f)
                quadTo(360.0f, 697.0f, 348.5f, 708.5f)
                quadTo(337.0f, 720.0f, 320.0f, 720.0f)
                lineTo(160.0f, 720.0f)
                close()
                moveTo(160.0f, 520.0f)
                quadTo(143.0f, 520.0f, 131.5f, 508.5f)
                quadTo(120.0f, 497.0f, 120.0f, 480.0f)
                quadTo(120.0f, 463.0f, 131.5f, 451.5f)
                quadTo(143.0f, 440.0f, 160.0f, 440.0f)
                lineTo(560.0f, 440.0f)
                quadTo(577.0f, 440.0f, 588.5f, 451.5f)
                quadTo(600.0f, 463.0f, 600.0f, 480.0f)
                quadTo(600.0f, 497.0f, 588.5f, 508.5f)
                quadTo(577.0f, 520.0f, 560.0f, 520.0f)
                lineTo(160.0f, 520.0f)
                close()
                moveTo(160.0f, 320.0f)
                quadTo(143.0f, 320.0f, 131.5f, 308.5f)
                quadTo(120.0f, 297.0f, 120.0f, 280.0f)
                quadTo(120.0f, 263.0f, 131.5f, 251.5f)
                quadTo(143.0f, 240.0f, 160.0f, 240.0f)
                lineTo(800.0f, 240.0f)
                quadTo(817.0f, 240.0f, 828.5f, 251.5f)
                quadTo(840.0f, 263.0f, 840.0f, 280.0f)
                quadTo(840.0f, 297.0f, 828.5f, 308.5f)
                quadTo(817.0f, 320.0f, 800.0f, 320.0f)
                lineTo(160.0f, 320.0f)
                close()
            }
        }.build()
}
