package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DiaryIcons.ArrowDropDown: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DiaryIcons.ArrowDropDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(459.0f, 579.0f)
                lineTo(314.0f, 434.0f)
                quadTo(311.0f, 431.0f, 309.5f, 427.5f)
                quadTo(308.0f, 424.0f, 308.0f, 420.0f)
                quadTo(308.0f, 412.0f, 313.5f, 406.0f)
                quadTo(319.0f, 400.0f, 328.0f, 400.0f)
                lineTo(632.0f, 400.0f)
                quadTo(641.0f, 400.0f, 646.5f, 406.0f)
                quadTo(652.0f, 412.0f, 652.0f, 420.0f)
                quadTo(652.0f, 422.0f, 646.0f, 434.0f)
                lineTo(501.0f, 579.0f)
                quadTo(496.0f, 584.0f, 491.0f, 586.0f)
                quadTo(486.0f, 588.0f, 480.0f, 588.0f)
                quadTo(474.0f, 588.0f, 469.0f, 586.0f)
                quadTo(464.0f, 584.0f, 459.0f, 579.0f)
                close()
            }
        }.build()
}
