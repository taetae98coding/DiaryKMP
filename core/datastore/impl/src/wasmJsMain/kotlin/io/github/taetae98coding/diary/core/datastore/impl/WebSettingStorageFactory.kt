package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.WebLocalStorage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
internal class WebSettingStorageFactory : SettingStorageFactory {
    override fun <T> create(
        serializer: SettingSerializer<T>,
        name: String,
    ): Storage<T> =
        WebLocalStorage(
            serializer = serializer,
            name = name,
        )
}
