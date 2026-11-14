package io.github.taetae98coding.diary.work.spotlight

import kotlin.uuid.Uuid

internal fun String.withId(id: Uuid): String = replace("{id}", id.toString())
