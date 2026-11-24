package io.github.taetae98coding.diary.data.web

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow

// 화면이 시작한 되돌리기와 앱 종료 때의 보관이 같은 상태를 보도록 앱 전체에 하나만 둔다.
@Inject
@SingleIn(AppScope::class)
internal class WebLoginRestoringState {
    val isRestoring: MutableStateFlow<Boolean> = MutableStateFlow(false)
}
