package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Undo: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Undo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(320.0f, 760.0f)
                quadTo(303.0f, 760.0f, 291.5f, 748.5f)
                quadTo(280.0f, 737.0f, 280.0f, 720.0f)
                quadTo(280.0f, 703.0f, 291.5f, 691.5f)
                quadTo(303.0f, 680.0f, 320.0f, 680.0f)
                lineTo(564.0f, 680.0f)
                quadTo(627.0f, 680.0f, 673.5f, 640.0f)
                quadTo(720.0f, 600.0f, 720.0f, 540.0f)
                quadTo(720.0f, 480.0f, 673.5f, 440.0f)
                quadTo(627.0f, 400.0f, 564.0f, 400.0f)
                lineTo(312.0f, 400.0f)
                lineTo(388.0f, 476.0f)
                quadTo(399.0f, 487.0f, 399.0f, 504.0f)
                quadTo(399.0f, 521.0f, 388.0f, 532.0f)
                quadTo(377.0f, 543.0f, 360.0f, 543.0f)
                quadTo(343.0f, 543.0f, 332.0f, 532.0f)
                lineTo(188.0f, 388.0f)
                quadTo(182.0f, 382.0f, 179.5f, 375.0f)
                quadTo(177.0f, 368.0f, 177.0f, 360.0f)
                quadTo(177.0f, 352.0f, 179.5f, 345.0f)
                quadTo(182.0f, 338.0f, 188.0f, 332.0f)
                lineTo(332.0f, 188.0f)
                quadTo(343.0f, 177.0f, 360.0f, 177.0f)
                quadTo(377.0f, 177.0f, 388.0f, 188.0f)
                quadTo(399.0f, 199.0f, 399.0f, 216.0f)
                quadTo(399.0f, 233.0f, 388.0f, 244.0f)
                lineTo(312.0f, 320.0f)
                lineTo(564.0f, 320.0f)
                quadTo(661.0f, 320.0f, 730.5f, 383.0f)
                quadTo(800.0f, 446.0f, 800.0f, 540.0f)
                quadTo(800.0f, 634.0f, 730.5f, 697.0f)
                quadTo(661.0f, 760.0f, 564.0f, 760.0f)
                lineTo(320.0f, 760.0f)
                close()
            }
        }.build()
}
