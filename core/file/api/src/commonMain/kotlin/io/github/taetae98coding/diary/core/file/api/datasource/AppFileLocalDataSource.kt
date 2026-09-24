package io.github.taetae98coding.diary.core.file.api.datasource

public interface AppFileLocalDataSource {
    public suspend fun exists(
        directory: String,
        name: String,
    ): Boolean

    // 돌려준 경로에 곧바로 쓸 수 있도록 상위 디렉터리가 없으면 만들어 둔다.
    public suspend fun resolve(
        directory: String,
        name: String,
    ): String

    public suspend fun delete(
        directory: String,
        name: String,
    )
}
