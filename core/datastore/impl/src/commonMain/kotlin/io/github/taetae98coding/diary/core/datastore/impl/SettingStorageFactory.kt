package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.Storage

internal interface SettingStorageFactory {
    fun <T> create(
        serializer: SettingSerializer<T>,
        name: String,
    ): Storage<T>
}
