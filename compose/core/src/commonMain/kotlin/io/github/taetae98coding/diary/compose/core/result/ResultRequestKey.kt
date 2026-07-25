package io.github.taetae98coding.diary.compose.core.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.uuid.Uuid

private val ResultRequestKeySaver: Saver<Uuid, String> =
    Saver(
        save = { key -> key.toString() },
        restore = { saved -> Uuid.parse(saved) },
    )

@Composable
public fun rememberResultRequestKey(): Uuid = rememberSaveable(saver = ResultRequestKeySaver) { Uuid.random() }
