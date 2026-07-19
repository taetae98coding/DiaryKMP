package io.github.taetae98coding.diary.core.datastore.impl

internal fun interface SettingPathResolver {
    fun resolve(name: String): String
}
