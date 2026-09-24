package io.github.taetae98coding.diary.core.file.impl.file

import io.github.taetae98coding.diary.core.model.file.FileUri
import java.io.File
import java.net.URI

internal fun FileUri.toFile(): File = File(URI(value))

internal fun File.toFileUri(): FileUri = FileUri(toURI().toString())
