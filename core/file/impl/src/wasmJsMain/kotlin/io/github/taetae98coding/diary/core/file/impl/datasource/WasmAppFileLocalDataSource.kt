package io.github.taetae98coding.diary.core.file.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource

/**
 * 브라우저에는 앱 전용 파일 저장소가 없다. 이 구현을 쓰는 기능은 웹에서 실행되지 않는다.
 */
@Inject
@ContributesBinding(AppScope::class)
internal class WasmAppFileLocalDataSource : AppFileLocalDataSource {
    override suspend fun exists(
        directory: String,
        name: String,
    ): Boolean = false

    override suspend fun resolve(
        directory: String,
        name: String,
    ): String = throw UnsupportedOperationException("App file storage is not available on the web")

    override suspend fun delete(
        directory: String,
        name: String,
    ): Unit = throw UnsupportedOperationException("App file storage is not available on the web")
}
