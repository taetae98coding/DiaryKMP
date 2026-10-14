package io.github.taetae98coding.diary.compose.core.uri

import androidx.compose.ui.platform.UriHandler

// Android는 받을 앱이 없는 주소를, 데스크톱은 열 수 없는 환경을 예외로 알린다. 결과를 알려 주지 않는 환경에서는 연 것으로 본다.
public fun UriHandler.tryOpenUri(uri: String): Boolean = runCatching { openUri(uri) }.isSuccess
