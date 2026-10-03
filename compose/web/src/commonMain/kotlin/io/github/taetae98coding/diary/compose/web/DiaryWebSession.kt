package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * [failureId]는 실패를 구분하는 번호라 같은 번호는 한 번만 알리며, 마지막 결과가 실패가 아니면 null이다.
 */
public data class DiaryWebSession(
    val isPreparing: Boolean = false,
    val importCount: Int = 0,
    val failureId: Int? = null,
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
