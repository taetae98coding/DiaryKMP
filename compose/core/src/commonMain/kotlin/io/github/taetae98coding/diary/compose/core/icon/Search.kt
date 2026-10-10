package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Search: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(380.0f, 640.0f)
                quadTo(271.0f, 640.0f, 195.5f, 564.5f)
                quadTo(120.0f, 489.0f, 120.0f, 380.0f)
                quadTo(120.0f, 271.0f, 195.5f, 195.5f)
                quadTo(271.0f, 120.0f, 380.0f, 120.0f)
                quadTo(489.0f, 120.0f, 564.5f, 195.5f)
                quadTo(640.0f, 271.0f, 640.0f, 380.0f)
                quadTo(640.0f, 424.0f, 626.0f, 463.0f)
                quadTo(612.0f, 502.0f, 588.0f, 532.0f)
                lineTo(812.0f, 756.0f)
                quadTo(823.0f, 767.0f, 823.0f, 784.0f)
                quadTo(823.0f, 801.0f, 812.0f, 812.0f)
                quadTo(801.0f, 823.0f, 784.0f, 823.0f)
                quadTo(767.0f, 823.0f, 756.0f, 812.0f)
                lineTo(532.0f, 588.0f)
                quadTo(502.0f, 612.0f, 463.0f, 626.0f)
                quadTo(424.0f, 640.0f, 380.0f, 640.0f)
                close()
                moveTo(380.0f, 560.0f)
                quadTo(455.0f, 560.0f, 507.5f, 507.5f)
                quadTo(560.0f, 455.0f, 560.0f, 380.0f)
                quadTo(560.0f, 305.0f, 507.5f, 252.5f)
                quadTo(455.0f, 200.0f, 380.0f, 200.0f)
                quadTo(305.0f, 200.0f, 252.5f, 252.5f)
                quadTo(200.0f, 305.0f, 200.0f, 380.0f)
                quadTo(200.0f, 455.0f, 252.5f, 507.5f)
                quadTo(305.0f, 560.0f, 380.0f, 560.0f)
                close()
            }
        }.build()
}
