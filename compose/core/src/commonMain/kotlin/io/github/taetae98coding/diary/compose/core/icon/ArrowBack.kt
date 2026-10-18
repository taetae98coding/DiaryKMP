package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.ArrowBack: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(313.0f, 520.0f)
                lineTo(509.0f, 716.0f)
                quadTo(521.0f, 728.0f, 520.5f, 744.0f)
                quadTo(520.0f, 760.0f, 508.0f, 772.0f)
                quadTo(496.0f, 783.0f, 480.0f, 783.5f)
                quadTo(464.0f, 784.0f, 452.0f, 772.0f)
                lineTo(188.0f, 508.0f)
                quadTo(182.0f, 502.0f, 179.5f, 495.0f)
                quadTo(177.0f, 488.0f, 177.0f, 480.0f)
                quadTo(177.0f, 472.0f, 179.5f, 465.0f)
                quadTo(182.0f, 458.0f, 188.0f, 452.0f)
                lineTo(452.0f, 188.0f)
                quadTo(463.0f, 177.0f, 479.5f, 177.0f)
                quadTo(496.0f, 177.0f, 508.0f, 188.0f)
                quadTo(520.0f, 200.0f, 520.0f, 216.5f)
                quadTo(520.0f, 233.0f, 508.0f, 245.0f)
                lineTo(313.0f, 440.0f)
                lineTo(760.0f, 440.0f)
                quadTo(777.0f, 440.0f, 788.5f, 451.5f)
                quadTo(800.0f, 463.0f, 800.0f, 480.0f)
                quadTo(800.0f, 497.0f, 788.5f, 508.5f)
                quadTo(777.0f, 520.0f, 760.0f, 520.0f)
                lineTo(313.0f, 520.0f)
                close()
            }
        }.build()
}
