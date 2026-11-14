package io.github.taetae98coding.diary.compose.core.share

public enum class ShareResult {
    // Android는 공유 시트에서 무엇을 골랐는지 알려 주지 않으므로 공유 시트를 연 것을 COMPLETED로 본다.
    COMPLETED,
    DISMISSED,
    COPIED,
    FAILED,
}
