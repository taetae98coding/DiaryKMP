package io.github.taetae98coding.diary.compose.core.share

import androidx.core.content.FileProvider

// 같은 FileProvider 클래스를 다른 라이브러리도 선언하면 매니페스트 병합에서 겹치므로 전용 하위 클래스로 둔다.
internal class ShareFileProvider : FileProvider()
