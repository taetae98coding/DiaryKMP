package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val DiaryIcons.MoreHoriz: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.MoreHoriz",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(240.0f, 560.0f)
                quadTo(207.0f, 560.0f, 183.5f, 536.5f)
                quadTo(160.0f, 513.0f, 160.0f, 480.0f)
                quadTo(160.0f, 447.0f, 183.5f, 423.5f)
                quadTo(207.0f, 400.0f, 240.0f, 400.0f)
                quadTo(273.0f, 400.0f, 296.5f, 423.5f)
                quadTo(320.0f, 447.0f, 320.0f, 480.0f)
                quadTo(320.0f, 513.0f, 296.5f, 536.5f)
                quadTo(273.0f, 560.0f, 240.0f, 560.0f)
                close()
                moveTo(480.0f, 560.0f)
                quadTo(447.0f, 560.0f, 423.5f, 536.5f)
                quadTo(400.0f, 513.0f, 400.0f, 480.0f)
                quadTo(400.0f, 447.0f, 423.5f, 423.5f)
                quadTo(447.0f, 400.0f, 480.0f, 400.0f)
                quadTo(513.0f, 400.0f, 536.5f, 423.5f)
                quadTo(560.0f, 447.0f, 560.0f, 480.0f)
                quadTo(560.0f, 513.0f, 536.5f, 536.5f)
                quadTo(513.0f, 560.0f, 480.0f, 560.0f)
                close()
                moveTo(720.0f, 560.0f)
                quadTo(687.0f, 560.0f, 663.5f, 536.5f)
                quadTo(640.0f, 513.0f, 640.0f, 480.0f)
                quadTo(640.0f, 447.0f, 663.5f, 423.5f)
                quadTo(687.0f, 400.0f, 720.0f, 400.0f)
                quadTo(753.0f, 400.0f, 776.5f, 423.5f)
                quadTo(800.0f, 447.0f, 800.0f, 480.0f)
                quadTo(800.0f, 513.0f, 776.5f, 536.5f)
                quadTo(753.0f, 560.0f, 720.0f, 560.0f)
                close()
            }
        }.build()
}
