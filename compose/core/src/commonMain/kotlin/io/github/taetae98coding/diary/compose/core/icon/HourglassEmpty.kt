package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.HourglassEmpty: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.HourglassEmpty",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(320.0f, 800.0f)
                lineTo(640.0f, 800.0f)
                lineTo(640.0f, 680.0f)
                quadTo(640.0f, 614.0f, 593.0f, 567.0f)
                quadTo(546.0f, 520.0f, 480.0f, 520.0f)
                quadTo(414.0f, 520.0f, 367.0f, 567.0f)
                quadTo(320.0f, 614.0f, 320.0f, 680.0f)
                lineTo(320.0f, 800.0f)
                close()
                moveTo(593.0f, 393.0f)
                quadTo(640.0f, 346.0f, 640.0f, 280.0f)
                lineTo(640.0f, 160.0f)
                lineTo(320.0f, 160.0f)
                lineTo(320.0f, 280.0f)
                quadTo(320.0f, 346.0f, 367.0f, 393.0f)
                quadTo(414.0f, 440.0f, 480.0f, 440.0f)
                quadTo(546.0f, 440.0f, 593.0f, 393.0f)
                close()
                moveTo(200.0f, 880.0f)
                quadTo(183.0f, 880.0f, 171.5f, 868.5f)
                quadTo(160.0f, 857.0f, 160.0f, 840.0f)
                quadTo(160.0f, 823.0f, 171.5f, 811.5f)
                quadTo(183.0f, 800.0f, 200.0f, 800.0f)
                lineTo(240.0f, 800.0f)
                lineTo(240.0f, 680.0f)
                quadTo(240.0f, 619.0f, 268.5f, 565.5f)
                quadTo(297.0f, 512.0f, 348.0f, 480.0f)
                quadTo(297.0f, 448.0f, 268.5f, 394.5f)
                quadTo(240.0f, 341.0f, 240.0f, 280.0f)
                lineTo(240.0f, 160.0f)
                lineTo(200.0f, 160.0f)
                quadTo(183.0f, 160.0f, 171.5f, 148.5f)
                quadTo(160.0f, 137.0f, 160.0f, 120.0f)
                quadTo(160.0f, 103.0f, 171.5f, 91.5f)
                quadTo(183.0f, 80.0f, 200.0f, 80.0f)
                lineTo(760.0f, 80.0f)
                quadTo(777.0f, 80.0f, 788.5f, 91.5f)
                quadTo(800.0f, 103.0f, 800.0f, 120.0f)
                quadTo(800.0f, 137.0f, 788.5f, 148.5f)
                quadTo(777.0f, 160.0f, 760.0f, 160.0f)
                lineTo(720.0f, 160.0f)
                lineTo(720.0f, 280.0f)
                quadTo(720.0f, 341.0f, 691.5f, 394.5f)
                quadTo(663.0f, 448.0f, 612.0f, 480.0f)
                quadTo(663.0f, 512.0f, 691.5f, 565.5f)
                quadTo(720.0f, 619.0f, 720.0f, 680.0f)
                lineTo(720.0f, 800.0f)
                lineTo(760.0f, 800.0f)
                quadTo(777.0f, 800.0f, 788.5f, 811.5f)
                quadTo(800.0f, 823.0f, 800.0f, 840.0f)
                quadTo(800.0f, 857.0f, 788.5f, 868.5f)
                quadTo(777.0f, 880.0f, 760.0f, 880.0f)
                lineTo(200.0f, 880.0f)
                close()
            }
        }.build()
}
