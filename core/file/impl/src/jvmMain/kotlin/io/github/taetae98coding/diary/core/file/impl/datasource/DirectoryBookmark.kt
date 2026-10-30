package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.library.foundation.SecurityScopedBookmark
import org.koin.core.annotation.Factory

// 샌드박스 앱은 열기 창으로 고른 폴더를 그 실행에서만 쓸 수 있어, 다음 실행에도 접근을 얻는 북마크로 바꿔 둔다.
internal interface DirectoryBookmark {
    fun create(path: String): String

    fun startAccessing(bookmark: String): AutoCloseable
}

@Factory
internal class MacDirectoryBookmark : DirectoryBookmark {
    override fun create(path: String): String = SecurityScopedBookmark.create(path = path)

    override fun startAccessing(bookmark: String): AutoCloseable = SecurityScopedBookmark.startAccessing(bookmark = bookmark)
}
