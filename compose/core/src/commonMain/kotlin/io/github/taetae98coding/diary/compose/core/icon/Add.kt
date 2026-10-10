package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Add: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(440.0f, 520.0f)
                lineTo(240.0f, 520.0f)
                quadTo(223.0f, 520.0f, 211.5f, 508.5f)
                quadTo(200.0f, 497.0f, 200.0f, 480.0f)
                quadTo(200.0f, 463.0f, 211.5f, 451.5f)
                quadTo(223.0f, 440.0f, 240.0f, 440.0f)
                lineTo(440.0f, 440.0f)
                lineTo(440.0f, 240.0f)
                quadTo(440.0f, 223.0f, 451.5f, 211.5f)
                quadTo(463.0f, 200.0f, 480.0f, 200.0f)
                quadTo(497.0f, 200.0f, 508.5f, 211.5f)
                quadTo(520.0f, 223.0f, 520.0f, 240.0f)
                lineTo(520.0f, 440.0f)
                lineTo(720.0f, 440.0f)
                quadTo(737.0f, 440.0f, 748.5f, 451.5f)
                quadTo(760.0f, 463.0f, 760.0f, 480.0f)
                quadTo(760.0f, 497.0f, 748.5f, 508.5f)
                quadTo(737.0f, 520.0f, 720.0f, 520.0f)
                lineTo(520.0f, 520.0f)
                lineTo(520.0f, 720.0f)
                quadTo(520.0f, 737.0f, 508.5f, 748.5f)
                quadTo(497.0f, 760.0f, 480.0f, 760.0f)
                quadTo(463.0f, 760.0f, 451.5f, 748.5f)
                quadTo(440.0f, 737.0f, 440.0f, 720.0f)
                lineTo(440.0f, 520.0f)
                close()
            }
        }.build()
}
