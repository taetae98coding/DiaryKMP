package io.github.taetae98coding.diary.compose.core.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.IconPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun GoogleIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    Image(
        imageVector = if (darkTheme) googleDarkImageVector else googleLightImageVector,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

private val googleLightImageVector: ImageVector by lazy {
    googleImageVector(
        name = "Google.Light",
        containerColor = Color.White,
        borderColor = Color(0xFF747775),
    )
}

private val googleDarkImageVector: ImageVector by lazy {
    googleImageVector(
        name = "Google.Dark",
        containerColor = Color(0xFF131314),
        borderColor = Color(0xFF8E918F),
    )
}

private fun googleImageVector(
    name: String,
    containerColor: Color,
    borderColor: Color,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f,
        ).apply {
            googleContainerPath(
                containerColor = containerColor,
                borderColor = borderColor,
            )
            group(
                translationX = 11.0f,
                translationY = 11.0f,
            ) {
                googleLogoPaths()
            }
        }.build()

private fun ImageVector.Builder.googleContainerPath(
    containerColor: Color,
    borderColor: Color,
) {
    path(
        fill = SolidColor(containerColor),
        stroke = SolidColor(borderColor),
        strokeLineWidth = 1.0f,
    ) {
        moveTo(20.0f, 0.5f)
        curveTo(30.7696f, 0.5f, 39.5f, 9.2304f, 39.5f, 20.0f)
        curveTo(39.5f, 30.7696f, 30.7696f, 39.5f, 20.0f, 39.5f)
        curveTo(9.2304f, 39.5f, 0.5f, 30.7696f, 0.5f, 20.0f)
        curveTo(0.5f, 9.2304f, 9.2304f, 0.5f, 20.0f, 0.5f)
        close()
    }
}

private fun ImageVector.Builder.googleLogoPaths() {
    path(fill = SolidColor(Color(0xFF4285F4))) {
        moveTo(17.64f, 9.2045f)
        curveToRelative(0.0f, -0.6381f, -0.0573f, -1.2518f, -0.1636f, -1.8409f)
        horizontalLineTo(9.0f)
        verticalLineToRelative(3.4818f)
        horizontalLineToRelative(4.8436f)
        curveToRelative(-0.2086f, 1.125f, -0.8427f, 2.0782f, -1.7959f, 2.7164f)
        verticalLineToRelative(2.2582f)
        horizontalLineToRelative(2.9086f)
        curveToRelative(1.7018f, -1.5668f, 2.6837f, -3.8741f, 2.6837f, -6.6155f)
        close()
    }
    path(fill = SolidColor(Color(0xFF34A853))) {
        moveTo(9.0f, 18.0f)
        curveToRelative(2.43f, 0.0f, 4.4673f, -0.8059f, 5.9564f, -2.18f)
        lineToRelative(-2.9086f, -2.2582f)
        curveToRelative(-0.8059f, 0.54f, -1.8368f, 0.8591f, -3.0477f, 0.8591f)
        curveToRelative(-2.3441f, 0.0f, -4.3282f, -1.5831f, -5.036f, -3.7104f)
        horizontalLineTo(0.9573f)
        verticalLineToRelative(2.3318f)
        curveTo(2.4382f, 15.9832f, 5.4818f, 18.0f, 9.0f, 18.0f)
        close()
    }
    path(fill = SolidColor(Color(0xFFFBBC05))) {
        moveTo(3.964f, 10.7105f)
        curveToRelative(-0.18f, -0.54f, -0.2827f, -1.1168f, -0.2827f, -1.7105f)
        reflectiveCurveToRelative(0.1027f, -1.1705f, 0.2827f, -1.7105f)
        verticalLineTo(4.9577f)
        horizontalLineTo(0.9573f)
        curveTo(0.3477f, 6.1732f, 0.0f, 7.5477f, 0.0f, 9.0f)
        reflectiveCurveToRelative(0.3477f, 2.8268f, 0.9573f, 4.0423f)
        lineToRelative(3.0068f, -2.3318f)
        close()
    }
    path(fill = SolidColor(Color(0xFFEA4335))) {
        moveTo(9.0f, 3.5795f)
        curveToRelative(1.3214f, 0.0f, 2.5077f, 0.4541f, 3.4405f, 1.346f)
        lineToRelative(2.5814f, -2.5814f)
        curveTo(13.4632f, 0.8918f, 11.4259f, 0.0f, 9.0f, 0.0f)
        curveTo(5.4818f, 0.0f, 2.4382f, 2.0168f, 0.9573f, 4.9577f)
        lineToRelative(3.0068f, 2.3318f)
        curveTo(4.6718f, 5.1627f, 6.6559f, 3.5795f, 9.0f, 3.5795f)
        close()
    }
}

@IconPreview
@Composable
private fun GoogleIconPreview(
    @PreviewParameter(BooleanPreviewParameter::class) darkTheme: Boolean,
) {
    DiaryTheme {
        GoogleIcon(darkTheme = darkTheme)
    }
}
