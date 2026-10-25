package io.github.taetae98coding.diary.feature.login.ui.credential

import java.awt.Desktop
import java.net.URI

// 브라우저를 열 수 없는 환경이면 UnsupportedOperationException으로 알려진다.
internal fun openInSystemBrowser(url: String) {
    Desktop.getDesktop().browse(URI(url))
}
