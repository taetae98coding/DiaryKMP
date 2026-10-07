package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.ArrowDropUp: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.ArrowDropUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(328.0f, 560.0f)
                quadTo(319.0f, 560.0f, 313.5f, 554.0f)
                quadTo(308.0f, 548.0f, 308.0f, 540.0f)
                quadTo(308.0f, 538.0f, 314.0f, 526.0f)
                lineTo(459.0f, 381.0f)
                quadTo(464.0f, 376.0f, 469.0f, 374.0f)
                quadTo(474.0f, 372.0f, 480.0f, 372.0f)
                quadTo(486.0f, 372.0f, 491.0f, 374.0f)
                quadTo(496.0f, 376.0f, 501.0f, 381.0f)
                lineTo(646.0f, 526.0f)
                quadTo(649.0f, 529.0f, 650.5f, 532.5f)
                quadTo(652.0f, 536.0f, 652.0f, 540.0f)
                quadTo(652.0f, 548.0f, 646.5f, 554.0f)
                quadTo(641.0f, 560.0f, 632.0f, 560.0f)
                lineTo(328.0f, 560.0f)
                close()
            }
        }.build()
}
