package io.github.taetae98coding.diary.core.model.file

// uri는 그 폴더를 다시 열 수 있는 플랫폼의 표현이다. android는 문서 트리 content://, ios는 보안 범위 북마크, jvm은 절대 경로다.
// jvm은 절대 경로를 그대로 보여 주므로, 샌드박스에서 다음 실행에도 접근을 얻는 보안 범위 북마크를 bookmark에 따로 둔다.
public data class FileDownloadDirectory(
    val uri: String,
    val name: String,
    val bookmark: String = "",
)
