package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

public data class DiaryWebSession(
    val isRestoring: Boolean = false,
)

/**
 * URL을 여는 [DiaryWebView]가 읽는 앱 전체의 세션이다. 앱 루트가 갱신하고, 갱신하기 전에는 기본값으로 동작한다.
 * Coil의 `SingletonImageLoader`처럼 화면 트리 밖에 두어, 세션을 쓰지 않는 중간 화면이 세션을 전달하지 않게 한다.
 */
public object SingletonDiaryWebSession {
    private var session by mutableStateOf(DiaryWebSession())

    public fun get(): DiaryWebSession = session

    public fun set(session: DiaryWebSession) {
        this.session = session
    }
}
