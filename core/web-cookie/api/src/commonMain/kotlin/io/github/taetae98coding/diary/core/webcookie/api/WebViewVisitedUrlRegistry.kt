package io.github.taetae98coding.diary.core.webcookie.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// 웹 표시 수단을 그리는 화면은 Koin 그래프 밖에 있어, 직접 연 주소를 앱 전체에 하나인 이 목록에 남긴다.
public object WebViewVisitedUrlRegistry {
    private val urlSet = MutableStateFlow(emptySet<String>())

    public fun record(url: String) {
        urlSet.update { set -> set + url }
    }

    public fun read(): Set<String> = urlSet.value
}
