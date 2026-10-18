package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Bolt: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Bolt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(422.0f, 728.0f)
                lineTo(629.0f, 480.0f)
                lineTo(469.0f, 480.0f)
                lineTo(498.0f, 253.0f)
                lineTo(313.0f, 520.0f)
                lineTo(452.0f, 520.0f)
                lineTo(422.0f, 728.0f)
                close()
                moveTo(360.0f, 600.0f)
                lineTo(236.0f, 600.0f)
                quadTo(212.0f, 600.0f, 200.5f, 578.5f)
                quadTo(189.0f, 557.0f, 203.0f, 537.0f)
                lineTo(502.0f, 107.0f)
                quadTo(512.0f, 93.0f, 528.0f, 87.5f)
                quadTo(544.0f, 82.0f, 561.0f, 88.0f)
                quadTo(578.0f, 94.0f, 586.0f, 109.0f)
                quadTo(594.0f, 124.0f, 592.0f, 141.0f)
                lineTo(560.0f, 400.0f)
                lineTo(715.0f, 400.0f)
                quadTo(741.0f, 400.0f, 751.5f, 423.0f)
                quadTo(762.0f, 446.0f, 745.0f, 466.0f)
                lineTo(416.0f, 860.0f)
                quadTo(405.0f, 873.0f, 389.0f, 877.0f)
                quadTo(373.0f, 881.0f, 358.0f, 874.0f)
                quadTo(343.0f, 867.0f, 334.5f, 852.5f)
                quadTo(326.0f, 838.0f, 328.0f, 821.0f)
                lineTo(360.0f, 600.0f)
                close()
                moveTo(471.0f, 490.0f)
                lineTo(471.0f, 490.0f)
                lineTo(471.0f, 490.0f)
                lineTo(471.0f, 490.0f)
                lineTo(471.0f, 490.0f)
                lineTo(471.0f, 490.0f)
                close()
            }
        }.build()
}
