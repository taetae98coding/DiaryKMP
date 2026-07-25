@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable

@Composable
public fun isPaneVisible(role: ThreePaneScaffoldRole): Boolean {
    val sceneScope = LocalListDetailSceneScope.current
    val scaffoldValue = sceneScope?.scaffoldTransitionScope?.scaffoldStateTransition?.targetState

    return scaffoldValue != null && scaffoldValue[role] != PaneAdaptedValue.Hidden
}
