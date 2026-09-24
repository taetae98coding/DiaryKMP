package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Factory

@Factory
internal class SystemAppFileLocalDataSource(
    @param:AppFileDirectory private val rootDirectory: String,
    @param:FileDispatcher private val dispatcher: CoroutineDispatcher,
) : AppFileLocalDataSource {
    override suspend fun exists(
        directory: String,
        name: String,
    ): Boolean =
        withContext(dispatcher) {
            SystemFileSystem.exists(resolvePath(directory = directory, name = name))
        }

    override suspend fun resolve(
        directory: String,
        name: String,
    ): String =
        withContext(dispatcher) {
            val path = resolvePath(directory = directory, name = name)
            path.parent?.let { parent -> SystemFileSystem.createDirectories(parent) }

            SystemFileSystem.resolve(path.parent ?: path).let { resolvedParent -> Path(resolvedParent, name).toString() }
        }

    override suspend fun delete(
        directory: String,
        name: String,
    ) {
        withContext(dispatcher) {
            SystemFileSystem.delete(resolvePath(directory = directory, name = name), mustExist = false)
        }
    }

    private fun resolvePath(
        directory: String,
        name: String,
    ): Path = Path(Path(rootDirectory, directory), name)
}
