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
public fun AppleIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    Image(
        imageVector = if (darkTheme) appleDarkImageVector else appleLightImageVector,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

private val appleLightImageVector: ImageVector by lazy {
    appleImageVector(
        name = "Apple.Light",
        containerColor = Color.White,
        borderColor = Color(0xFF747775),
        logoColor = Color.Black,
    )
}

private val appleDarkImageVector: ImageVector by lazy {
    appleImageVector(
        name = "Apple.Dark",
        containerColor = Color.Black,
        borderColor = Color(0xFF8E918F),
        logoColor = Color.White,
    )
}

private fun appleImageVector(
    name: String,
    containerColor: Color,
    borderColor: Color,
    logoColor: Color,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f,
        ).apply {
            appleContainerPath(
                containerColor = containerColor,
                borderColor = borderColor,
            )
            group(
                scaleX = 0.75f,
                scaleY = 0.75f,
                translationX = 11.0f,
                translationY = 11.0f,
            ) {
                appleLogoPath(logoColor = logoColor)
            }
        }.build()

private fun ImageVector.Builder.appleContainerPath(
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

private fun ImageVector.Builder.appleLogoPath(logoColor: Color) {
    path(fill = SolidColor(logoColor)) {
        moveTo(12.152f, 6.896f)
        curveToRelative(-0.948f, 0.0f, -2.415f, -1.078f, -3.96f, -1.04f)
        curveToRelative(-2.04f, 0.027f, -3.91f, 1.183f, -4.961f, 3.014f)
        curveToRelative(-2.117f, 3.675f, -0.546f, 9.103f, 1.519f, 12.09f)
        curveToRelative(1.013f, 1.454f, 2.208f, 3.09f, 3.792f, 3.039f)
        curveToRelative(1.52f, -0.065f, 2.09f, -0.987f, 3.935f, -0.987f)
        curveToRelative(1.831f, 0.0f, 2.35f, 0.987f, 3.96f, 0.948f)
        curveToRelative(1.637f, -0.026f, 2.676f, -1.48f, 3.676f, -2.948f)
        curveToRelative(1.156f, -1.688f, 1.636f, -3.325f, 1.662f, -3.415f)
        curveToRelative(-0.09f, -0.026f, -3.194f, -1.234f, -3.22f, -4.883f)
        curveToRelative(-0.026f, -3.052f, 2.494f, -4.507f, 2.61f, -4.572f)
        curveToRelative(-1.428f, -2.104f, -3.65f, -2.35f, -4.494f, -2.402f)
        curveToRelative(-2.078f, -0.156f, -3.83f, 1.156f, -4.52f, 1.156f)
        close()
        moveTo(15.53f, 3.83f)
        curveToRelative(0.843f, -1.012f, 1.4f, -2.427f, 1.245f, -3.83f)
        curveToRelative(-1.207f, 0.052f, -2.674f, 0.805f, -3.583f, 1.818f)
        curveToRelative(-0.806f, 0.896f, -1.4f, 2.338f, -1.245f, 3.714f)
        curveToRelative(1.35f, 0.104f, 2.74f, -0.688f, 3.583f, -1.702f)
        close()
    }
}

@IconPreview
@Composable
private fun AppleIconPreview(
    @PreviewParameter(BooleanPreviewParameter::class) darkTheme: Boolean,
) {
    DiaryTheme {
        AppleIcon(darkTheme = darkTheme)
    }
}
