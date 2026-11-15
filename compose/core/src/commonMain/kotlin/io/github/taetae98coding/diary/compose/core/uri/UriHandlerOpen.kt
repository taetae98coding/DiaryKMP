package io.github.taetae98coding.diary.compose.core.uri

import androidx.compose.ui.platform.UriHandler

// 열지 못한 것을 알려 주는 방식이 실행 환경마다 다르다. iOS는 연 뒤에 완료 결과로 알려 주므로 결과를 기다릴 수 있게 suspend로 둔다.
public expect suspend fun UriHandler.tryOpenUri(uri: String): Boolean
