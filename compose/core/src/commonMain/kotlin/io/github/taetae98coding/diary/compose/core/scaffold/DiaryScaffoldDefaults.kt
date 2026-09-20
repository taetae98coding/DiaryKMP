package io.github.taetae98coding.diary.compose.core.scaffold

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

public object DiaryScaffoldDefaults {
    public val contentWindowInsets: WindowInsets
        @Composable
        get() = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.safeDrawing)
}
