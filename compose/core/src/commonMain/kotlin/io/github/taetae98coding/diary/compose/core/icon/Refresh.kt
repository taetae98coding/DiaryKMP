package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.Refresh: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(480.0f, 800.0f)
                quadTo(346.0f, 800.0f, 253.0f, 707.0f)
                quadTo(160.0f, 614.0f, 160.0f, 480.0f)
                quadTo(160.0f, 346.0f, 253.0f, 253.0f)
                quadTo(346.0f, 160.0f, 480.0f, 160.0f)
                quadTo(549.0f, 160.0f, 612.0f, 188.5f)
                quadTo(675.0f, 217.0f, 720.0f, 270.0f)
                lineTo(720.0f, 200.0f)
                quadTo(720.0f, 183.0f, 731.5f, 171.5f)
                quadTo(743.0f, 160.0f, 760.0f, 160.0f)
                quadTo(777.0f, 160.0f, 788.5f, 171.5f)
                quadTo(800.0f, 183.0f, 800.0f, 200.0f)
                lineTo(800.0f, 400.0f)
                quadTo(800.0f, 417.0f, 788.5f, 428.5f)
                quadTo(777.0f, 440.0f, 760.0f, 440.0f)
                lineTo(560.0f, 440.0f)
                quadTo(543.0f, 440.0f, 531.5f, 428.5f)
                quadTo(520.0f, 417.0f, 520.0f, 400.0f)
                quadTo(520.0f, 383.0f, 531.5f, 371.5f)
                quadTo(543.0f, 360.0f, 560.0f, 360.0f)
                lineTo(688.0f, 360.0f)
                quadTo(656.0f, 304.0f, 600.5f, 272.0f)
                quadTo(545.0f, 240.0f, 480.0f, 240.0f)
                quadTo(380.0f, 240.0f, 310.0f, 310.0f)
                quadTo(240.0f, 380.0f, 240.0f, 480.0f)
                quadTo(240.0f, 580.0f, 310.0f, 650.0f)
                quadTo(380.0f, 720.0f, 480.0f, 720.0f)
                quadTo(548.0f, 720.0f, 604.5f, 685.5f)
                quadTo(661.0f, 651.0f, 692.0f, 593.0f)
                quadTo(700.0f, 579.0f, 714.5f, 573.5f)
                quadTo(729.0f, 568.0f, 744.0f, 573.0f)
                quadTo(760.0f, 578.0f, 767.0f, 594.0f)
                quadTo(774.0f, 610.0f, 766.0f, 624.0f)
                quadTo(725.0f, 704.0f, 649.0f, 752.0f)
                quadTo(573.0f, 800.0f, 480.0f, 800.0f)
                close()
            }
        }.build()
}
