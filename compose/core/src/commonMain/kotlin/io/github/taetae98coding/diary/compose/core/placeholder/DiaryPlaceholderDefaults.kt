@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.placeholder

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object DiaryPlaceholderDefaults {
    public val IconSize: Dp = 48.dp

    public val ContainerSize: Dp = 96.dp

    public val containerShape: Shape
        @Composable
        get() = MaterialShapes.Cookie9Sided.toShape()
}
