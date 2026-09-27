package io.github.taetae98coding.diary.core.model.file

import kotlin.uuid.Uuid

public sealed interface FileUploadEvent {
    public data class Succeeded(
        val fileId: Uuid,
    ) : FileUploadEvent

    public data class SucceededOnFileAdd(
        val fileId: Uuid,
    ) : FileUploadEvent

    public data object TooLarge : FileUploadEvent

    public data object Failed : FileUploadEvent
}
