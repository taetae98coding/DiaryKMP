package io.github.taetae98coding.diary.core.file.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.library.foundation.SecurityScopedBookmark

// 샌드박스 앱은 열기 창으로 고른 폴더를 그 실행에서만 쓸 수 있어, 다음 실행에도 접근을 얻는 북마크로 바꿔 둔다.
internal interface DirectoryBookmark {
    fun create(path: String): String

    fun startAccessing(bookmark: String): AutoCloseable
}

@Inject
@ContributesBinding(AppScope::class)
internal class MacDirectoryBookmark : DirectoryBookmark {
    override fun create(path: String): String = SecurityScopedBookmark.create(path = path)

    override fun startAccessing(bookmark: String): AutoCloseable = SecurityScopedBookmark.startAccessing(bookmark = bookmark)
}
