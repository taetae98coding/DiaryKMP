package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Logout: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Logout",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(200.0f, 840.0f)
                quadTo(167.0f, 840.0f, 143.5f, 816.5f)
                quadTo(120.0f, 793.0f, 120.0f, 760.0f)
                lineTo(120.0f, 200.0f)
                quadTo(120.0f, 167.0f, 143.5f, 143.5f)
                quadTo(167.0f, 120.0f, 200.0f, 120.0f)
                lineTo(440.0f, 120.0f)
                quadTo(457.0f, 120.0f, 468.5f, 131.5f)
                quadTo(480.0f, 143.0f, 480.0f, 160.0f)
                quadTo(480.0f, 177.0f, 468.5f, 188.5f)
                quadTo(457.0f, 200.0f, 440.0f, 200.0f)
                lineTo(200.0f, 200.0f)
                quadTo(200.0f, 200.0f, 200.0f, 200.0f)
                quadTo(200.0f, 200.0f, 200.0f, 200.0f)
                lineTo(200.0f, 760.0f)
                quadTo(200.0f, 760.0f, 200.0f, 760.0f)
                quadTo(200.0f, 760.0f, 200.0f, 760.0f)
                lineTo(440.0f, 760.0f)
                quadTo(457.0f, 760.0f, 468.5f, 771.5f)
                quadTo(480.0f, 783.0f, 480.0f, 800.0f)
                quadTo(480.0f, 817.0f, 468.5f, 828.5f)
                quadTo(457.0f, 840.0f, 440.0f, 840.0f)
                lineTo(200.0f, 840.0f)
                close()
                moveTo(687.0f, 520.0f)
                lineTo(400.0f, 520.0f)
                quadTo(383.0f, 520.0f, 371.5f, 508.5f)
                quadTo(360.0f, 497.0f, 360.0f, 480.0f)
                quadTo(360.0f, 463.0f, 371.5f, 451.5f)
                quadTo(383.0f, 440.0f, 400.0f, 440.0f)
                lineTo(687.0f, 440.0f)
                lineTo(612.0f, 365.0f)
                quadTo(601.0f, 354.0f, 601.0f, 338.0f)
                quadTo(601.0f, 322.0f, 612.0f, 310.0f)
                quadTo(623.0f, 298.0f, 640.0f, 297.5f)
                quadTo(657.0f, 297.0f, 669.0f, 309.0f)
                lineTo(812.0f, 452.0f)
                quadTo(824.0f, 464.0f, 824.0f, 480.0f)
                quadTo(824.0f, 496.0f, 812.0f, 508.0f)
                lineTo(669.0f, 651.0f)
                quadTo(657.0f, 663.0f, 640.5f, 662.5f)
                quadTo(624.0f, 662.0f, 612.0f, 650.0f)
                quadTo(601.0f, 638.0f, 601.5f, 621.5f)
                quadTo(602.0f, 605.0f, 613.0f, 594.0f)
                lineTo(687.0f, 520.0f)
                close()
            }
        }.build()
}
