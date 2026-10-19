package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
internal interface DirectoryPicker {
    fun open()
}

// 고른 결과는 open을 부른 코루틴이 아니라 onPick으로 전달한다. 선택 도구가 열린 동안 화면이 다시 만들어지면
// 기다리던 코루틴은 사라지지만, 다시 만들어진 화면의 onPick은 결과를 받을 수 있기 때문이다.
@Composable
internal expect fun rememberDirectoryPicker(onPick: (uri: String) -> Unit): DirectoryPicker
