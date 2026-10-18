package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Edit: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Edit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(200.0f, 760.0f)
                lineTo(257.0f, 760.0f)
                lineTo(648.0f, 369.0f)
                lineTo(591.0f, 312.0f)
                lineTo(200.0f, 703.0f)
                lineTo(200.0f, 760.0f)
                close()
                moveTo(160.0f, 840.0f)
                quadTo(143.0f, 840.0f, 131.5f, 828.5f)
                quadTo(120.0f, 817.0f, 120.0f, 800.0f)
                lineTo(120.0f, 703.0f)
                quadTo(120.0f, 687.0f, 126.0f, 672.5f)
                quadTo(132.0f, 658.0f, 143.0f, 647.0f)
                lineTo(648.0f, 143.0f)
                quadTo(660.0f, 132.0f, 674.5f, 126.0f)
                quadTo(689.0f, 120.0f, 705.0f, 120.0f)
                quadTo(721.0f, 120.0f, 736.0f, 126.0f)
                quadTo(751.0f, 132.0f, 762.0f, 144.0f)
                lineTo(817.0f, 200.0f)
                quadTo(829.0f, 211.0f, 834.5f, 226.0f)
                quadTo(840.0f, 241.0f, 840.0f, 256.0f)
                quadTo(840.0f, 272.0f, 834.5f, 286.5f)
                quadTo(829.0f, 301.0f, 817.0f, 313.0f)
                lineTo(313.0f, 817.0f)
                quadTo(302.0f, 828.0f, 287.5f, 834.0f)
                quadTo(273.0f, 840.0f, 257.0f, 840.0f)
                lineTo(160.0f, 840.0f)
                close()
                moveTo(760.0f, 256.0f)
                lineTo(760.0f, 256.0f)
                lineTo(704.0f, 200.0f)
                lineTo(704.0f, 200.0f)
                lineTo(760.0f, 256.0f)
                close()
                moveTo(619.0f, 341.0f)
                lineTo(591.0f, 312.0f)
                lineTo(591.0f, 312.0f)
                lineTo(648.0f, 369.0f)
                lineTo(648.0f, 369.0f)
                lineTo(619.0f, 341.0f)
                close()
            }
        }.build()
}
