package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.CircleFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.CircleFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(480.0f, 880.0f)
                quadTo(397.0f, 880.0f, 324.0f, 848.5f)
                quadTo(251.0f, 817.0f, 197.0f, 763.0f)
                quadTo(143.0f, 709.0f, 111.5f, 636.0f)
                quadTo(80.0f, 563.0f, 80.0f, 480.0f)
                quadTo(80.0f, 397.0f, 111.5f, 324.0f)
                quadTo(143.0f, 251.0f, 197.0f, 197.0f)
                quadTo(251.0f, 143.0f, 324.0f, 111.5f)
                quadTo(397.0f, 80.0f, 480.0f, 80.0f)
                quadTo(563.0f, 80.0f, 636.0f, 111.5f)
                quadTo(709.0f, 143.0f, 763.0f, 197.0f)
                quadTo(817.0f, 251.0f, 848.5f, 324.0f)
                quadTo(880.0f, 397.0f, 880.0f, 480.0f)
                quadTo(880.0f, 563.0f, 848.5f, 636.0f)
                quadTo(817.0f, 709.0f, 763.0f, 763.0f)
                quadTo(709.0f, 817.0f, 636.0f, 848.5f)
                quadTo(563.0f, 880.0f, 480.0f, 880.0f)
                close()
            }
        }.build()
}
