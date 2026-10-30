package io.github.taetae98coding.diary.data.web

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Single

// 화면이 시작한 되돌리기와 앱 종료 때의 보관이 같은 상태를 보도록 앱 전체에 하나만 둔다.
@Single
internal class WebLoginRestoringState {
    val isRestoring: MutableStateFlow<Boolean> = MutableStateFlow(false)
}
