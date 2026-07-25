package io.github.taetae98coding.diary.compose.core.scaffold

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

public object DiaryScaffoldDefaults {
    // 키보드와 시스템 바를 모두 피해야 하는 입력 화면의 Scaffold 여백이다.
    public val contentWindowInsets: WindowInsets
        @Composable
        get() = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.safeDrawing)
}
