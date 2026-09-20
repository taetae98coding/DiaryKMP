package io.github.taetae98coding.diary.app

import androidx.lifecycle.Lifecycle

// 웹은 탭이 보여도 페이지에 포커스가 없으면 STARTED에 머무르므로, 사용자가 앱으로 돌아온 시점인 RESUMED를 계기로 삼는다.
internal actual val syncMinActiveState: Lifecycle.State = Lifecycle.State.RESUMED
