package io.github.taetae98coding.diary.core.file.api.datasource

import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.io.RawSink

public interface FileDownloadLocalDataSource {
    public val isDirectorySelectable: Boolean

    // directory가 없으면 플랫폼의 기본 위치에 둔다. nameCandidate(0)부터 차례로 저장 위치에 없는 이름을 골라 새 파일을 만들고
    // write로 내용을 채운 뒤 저장한 이름을 돌려준다. 끝까지 쓰지 못하면 만든 파일을 지운다.
    // 저장 위치를 앱이 다룰 수 없는 플랫폼은 받은 내용을 브라우저에 넘긴다.
    public suspend fun save(
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): FileDownloadResult

    // 폴더 선택 도구가 돌려준 위치를 앱이 계속 쓸 수 있게 시스템에 허락을 받고, 다시 열 수 있는 표현으로 바꾼다.
    public suspend fun retainDirectory(uri: String): FileDownloadDirectory

    public suspend fun releaseDirectory(directory: FileDownloadDirectory)
}
