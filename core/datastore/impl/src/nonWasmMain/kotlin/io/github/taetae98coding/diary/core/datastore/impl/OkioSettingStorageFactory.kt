package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioStorage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.FileSystem
import okio.Path.Companion.toPath

@Inject
@ContributesBinding(AppScope::class)
internal class OkioSettingStorageFactory(
    private val pathResolver: SettingPathResolver,
) : SettingStorageFactory {
    override fun <T> create(
        serializer: SettingSerializer<T>,
        name: String,
    ): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = { pathResolver.resolve(name).toPath() },
        )
}
