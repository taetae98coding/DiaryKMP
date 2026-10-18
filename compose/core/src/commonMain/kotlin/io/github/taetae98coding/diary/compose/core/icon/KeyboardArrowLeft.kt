package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.KeyboardArrowLeft: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.KeyboardArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(432.0f, 480.0f)
                lineTo(588.0f, 636.0f)
                quadTo(599.0f, 647.0f, 599.0f, 664.0f)
                quadTo(599.0f, 681.0f, 588.0f, 692.0f)
                quadTo(577.0f, 703.0f, 560.0f, 703.0f)
                quadTo(543.0f, 703.0f, 532.0f, 692.0f)
                lineTo(348.0f, 508.0f)
                quadTo(342.0f, 502.0f, 339.5f, 495.0f)
                quadTo(337.0f, 488.0f, 337.0f, 480.0f)
                quadTo(337.0f, 472.0f, 339.5f, 465.0f)
                quadTo(342.0f, 458.0f, 348.0f, 452.0f)
                lineTo(532.0f, 268.0f)
                quadTo(543.0f, 257.0f, 560.0f, 257.0f)
                quadTo(577.0f, 257.0f, 588.0f, 268.0f)
                quadTo(599.0f, 279.0f, 599.0f, 296.0f)
                quadTo(599.0f, 313.0f, 588.0f, 324.0f)
                lineTo(432.0f, 480.0f)
                close()
            }
        }.build()
}
