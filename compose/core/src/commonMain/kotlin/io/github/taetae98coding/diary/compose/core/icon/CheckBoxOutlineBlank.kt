package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.CheckBoxOutlineBlank: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.CheckBoxOutlineBlank",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(200.0f, 840.0f)
                quadTo(167.0f, 840.0f, 143.5f, 816.5f)
                quadTo(120.0f, 793.0f, 120.0f, 760.0f)
                lineTo(120.0f, 200.0f)
                quadTo(120.0f, 167.0f, 143.5f, 143.5f)
                quadTo(167.0f, 120.0f, 200.0f, 120.0f)
                lineTo(760.0f, 120.0f)
                quadTo(793.0f, 120.0f, 816.5f, 143.5f)
                quadTo(840.0f, 167.0f, 840.0f, 200.0f)
                lineTo(840.0f, 760.0f)
                quadTo(840.0f, 793.0f, 816.5f, 816.5f)
                quadTo(793.0f, 840.0f, 760.0f, 840.0f)
                lineTo(200.0f, 840.0f)
                close()
                moveTo(200.0f, 760.0f)
                lineTo(760.0f, 760.0f)
                quadTo(760.0f, 760.0f, 760.0f, 760.0f)
                quadTo(760.0f, 760.0f, 760.0f, 760.0f)
                lineTo(760.0f, 200.0f)
                quadTo(760.0f, 200.0f, 760.0f, 200.0f)
                quadTo(760.0f, 200.0f, 760.0f, 200.0f)
                lineTo(200.0f, 200.0f)
                quadTo(200.0f, 200.0f, 200.0f, 200.0f)
                quadTo(200.0f, 200.0f, 200.0f, 200.0f)
                lineTo(200.0f, 760.0f)
                quadTo(200.0f, 760.0f, 200.0f, 760.0f)
                quadTo(200.0f, 760.0f, 200.0f, 760.0f)
                close()
            }
        }.build()
}
