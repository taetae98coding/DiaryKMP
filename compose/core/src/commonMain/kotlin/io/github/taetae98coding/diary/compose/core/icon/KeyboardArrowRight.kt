package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.KeyboardArrowRight: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.KeyboardArrowRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(504.0f, 480.0f)
                lineTo(348.0f, 324.0f)
                quadTo(337.0f, 313.0f, 337.0f, 296.0f)
                quadTo(337.0f, 279.0f, 348.0f, 268.0f)
                quadTo(359.0f, 257.0f, 376.0f, 257.0f)
                quadTo(393.0f, 257.0f, 404.0f, 268.0f)
                lineTo(588.0f, 452.0f)
                quadTo(594.0f, 458.0f, 596.5f, 465.0f)
                quadTo(599.0f, 472.0f, 599.0f, 480.0f)
                quadTo(599.0f, 488.0f, 596.5f, 495.0f)
                quadTo(594.0f, 502.0f, 588.0f, 508.0f)
                lineTo(404.0f, 692.0f)
                quadTo(393.0f, 703.0f, 376.0f, 703.0f)
                quadTo(359.0f, 703.0f, 348.0f, 692.0f)
                quadTo(337.0f, 681.0f, 337.0f, 664.0f)
                quadTo(337.0f, 647.0f, 348.0f, 636.0f)
                lineTo(504.0f, 480.0f)
                close()
            }
        }.build()
}
