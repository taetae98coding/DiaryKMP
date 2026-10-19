package io.github.taetae98coding.diary.core.model.file

// uri는 그 폴더를 다시 열 수 있는 플랫폼의 표현이다. android는 문서 트리 content://, ios는 보안 범위 북마크, jvm은 절대 경로다.
public data class FileDownloadDirectory(
    val uri: String,
    val name: String,
)
